package zone.huawei.tools.springlogaid.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.ServletRequestPathUtils;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;
import zone.huawei.tools.springlogaid.model.requests.CachedHttpServletRequest;
import zone.huawei.tools.springlogaid.service.RequestFileService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;

@Order(value = Integer.MIN_VALUE)
public class LogAidFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(LogAidFilter.class);

    @Autowired(required = false)
    private RequestFileService requestFileService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        LTH.clear();
        if (MDC.get(AidConstants.MDC_REQUEST_ID_KEY) == null) {
            MDC.put(AidConstants.MDC_REQUEST_ID_KEY, UUID.randomUUID().toString());
        }
        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            String traceHeader = httpServletRequest.getHeader(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY);
            if (traceHeader != null) {
                MDC.put(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, traceHeader);
                LTH.addLogKey(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, traceHeader);
            }
            boolean activatedRequest = isActivatedRequest(httpServletRequest);
            boolean printRequest = !isExcludeRequest(httpServletRequest);
            boolean enabled = printRequest && (ENABLE_INBOUND_REQUEST_GLOBAL || activatedRequest);
            if (AidConstants.isGlobalMode() || (ENABLE && activatedRequest)) {
                LTH.clear();
                LTH.enable();
            }
            if (enabled || LTH.isEnabled()) {
                if (enabled)
                    LTH.enableInboundRequest();
                HttpServletRequest requestWrapper = httpServletRequest;
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                try {
                    LTH.setStopWatch(stopWatch);
                    requestWrapper = new CachedHttpServletRequest(httpServletRequest);
                    traceRequest(requestWrapper);
                } catch (Exception e) {
                    logger.warn("LOG AID :: Failed to tracing external request in log aid,due to :{},StackTrace:{}", e, e.getStackTrace());
                }
                filterChain.doFilter(requestWrapper, servletResponse);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isExcludeRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        for (Pattern uriPattern : FILTER_EXCLUDE_URI_PATTERNS) {
            if (uriPattern.matcher(requestURI).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isActivatedRequest(HttpServletRequest request) {
        if (!ENABLE_TRACKING_REQUEST) {
            return true;
        }
        ServletRequestPathUtils.parseAndCache(request);
        for (RequestMappingInfo requestMappingInfo : ACTIVATED_REQUEST_MAPPING) {
            if (requestMappingInfo.getMatchingCondition(request) != null) {
                LTH.enableOutboundRequest();
                return true;
            }
        }
        return false;
    }

    private void traceRequest(HttpServletRequest request) throws IOException, ServletException {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()).append("===========INBOUND REQUEST START============").append(System.lineSeparator());
        sb.append(toCurl(request));
        sb.append("===========INBOUND REQUEST END============").append(System.lineSeparator());
        LTH.setInboundRequest(sb);
        if (LTH.isInboundRequestEnabled())
            logger.info(sb.toString());
    }

    private StringBuilder toCurl(HttpServletRequest request) throws ServletException, IOException {
        StringBuilder curl = new StringBuilder();
        curl.append("curl --location --request ");
        String method = request.getMethod();
        curl.append(method);
        curl.append(" '").append(request.getRequestURL()).append("' \\").append(System.lineSeparator());
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String header = request.getHeader(headerName);
                curl.append("--header '").append(headerName).append(": ").append(header).append("' \\").append(System.lineSeparator());
            }
        }
        if (request instanceof CachedHttpServletRequest cachedHttpServletRequest){
            if (cachedHttpServletRequest.isMultipart()){
                recordMultipartRequest(curl,cachedHttpServletRequest);
            }else {
                if (HttpMethod.POST.name().equals(method) || HttpMethod.PUT.name().equals(method)) {
                    String requestBody = getRequestBody(cachedHttpServletRequest);
                    if (StringUtils.hasText(requestBody)) {
                        curl.append("--data-raw '").append(requestBody).append("'");
                    }
                }
            }
        }
        curl.append(System.lineSeparator());
        return curl;
    }

    private void recordMultipartRequest(StringBuilder curl, CachedHttpServletRequest request) throws ServletException, IOException {
        Collection<Part> parts = request.getParts();
        Iterator<Part> iterator = parts.iterator();
        while (iterator.hasNext()){
            Part part = iterator.next();
            String headerValue = part.getHeader("Content-Disposition");
            ContentDisposition disposition = ContentDisposition.parse(headerValue);
            String name = disposition.getName();
            String filename = disposition.getFilename();
            if (filename!=null){
                if (requestFileService!=null){
                    MockMultipartFile multipartFile = new MockMultipartFile(filename, filename, part.getContentType(), part.getInputStream());
                    String record = requestFileService.handleRequestFileAndRecord(request, multipartFile);
                    if (record!=null){
                        record = record.replaceAll("\\\\","\\\\\\\\");
                    }
                    curl.append("--form '").append(name).append("=@\"/").append(record).append("\"'");
                } else {
                    curl.append("--form '").append(name).append("=@\"/").append("filePath/").append(filename).append("\"'");
                }
            }else {
                curl.append("--form '").append(name).append("=\"").append(request.getParameter(name).replaceAll("\\\\","\\\\\\\\").replaceAll("\"","\\\\\"")).append("\"'");
                String type = part.getContentType();
                if (StringUtils.hasText(type)){
                    curl.append(";type=").append(type);
                }
                curl.append("'");
            }
            if (iterator.hasNext()){
                curl.append(" \\").append(System.lineSeparator());
            }
        }
    }

    private String getRequestBody(CachedHttpServletRequest request){
        if (!AidConstants.InboundRequest.PRINT_REQUEST_BODY && request.getCachedBody().length > 0) {
            return "LOG AID :: Print requestBody flag is disabled!";
        }

        return Optional.of(request.getInputStream()).map((inputStream) -> {
            try {
                return new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("LOG AID :: An exception occurred while reading the request body, due to: {}", e.toString());
                return "LOG AID :: An exception occurred while reading the request body";
            }
        }).orElse(null);
    }

}
