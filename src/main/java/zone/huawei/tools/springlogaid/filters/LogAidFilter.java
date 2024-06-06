package zone.huawei.tools.springlogaid.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.ServletRequestPathUtils;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;
import zone.huawei.tools.springlogaid.model.requests.CachedHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;

@Order(value = Integer.MIN_VALUE)
public class LogAidFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(LogAidFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (MDC.get(AidConstants.MDC_REQUEST_ID_KEY) == null) {
            MDC.put(AidConstants.MDC_REQUEST_ID_KEY, UUID.randomUUID().toString());
        }
        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            String traceHeader = httpServletRequest.getHeader(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY);
            if (traceHeader != null) {
                MDC.put(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, traceHeader);
                LTH.addLogKey(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, traceHeader);
            }
            boolean printRequest = !isExcludeRequest(httpServletRequest);
            boolean enabled = printRequest && ENABLE_FILTER_GLOBAL;
            if (AidConstants.isGlobalMode() || isActivatedRequest(httpServletRequest)) {
                LTH.clear();
                LTH.enable();
                if (printRequest)
                    enabled = true;
            }
            if (enabled) {
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

    private boolean isExcludeRequest(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        for (Pattern uriPattern : FILTER_EXCLUDE_URI_PATTERNS) {
            if (uriPattern.matcher(requestURI).matches()){
                return true;
            }
        }
        return false;
    }

    private boolean isActivatedRequest(HttpServletRequest request){
        ServletRequestPathUtils.parseAndCache(request);
        for (RequestMappingInfo requestMappingInfo : ACTIVATED_REQUEST_MAPPING) {
            if (requestMappingInfo.getMatchingCondition(request)!=null){
                return true;
            }
        }
        return false;
    }

    private void traceRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()).append("===========INBOUND REQUEST START============").append(System.lineSeparator());
        String requestBody = Optional.of(request.getInputStream()).map((inputStream) -> {
            try {
                return new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("LOG AID :: An exception occurred while reading the request body, due to: {}", e.toString());
                return "LOG AID :: An exception occurred while reading the request body";
            }
        }).orElse(null);
        if (!AidConstants.InboundRequest.PRINT_REQUEST_BODY && StringUtils.hasText(requestBody)) {
            requestBody = "LOG AID :: Print requestBody flag is disabled!";
        }
        sb.append(toCurl(request, requestBody));
        sb.append("===========INBOUND REQUEST END============").append(System.lineSeparator());
        logger.info(sb.toString());
        LTH.setInboundRequest(sb);
    }

    private StringBuilder toCurl(HttpServletRequest request, String requestBody) {
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
        if (HttpMethod.POST.name().equals(method) || HttpMethod.PUT.name().equals(method)) {
            if (StringUtils.hasText(requestBody)) {
                curl.append("--data-raw '").append(requestBody).append("'");
            }
        }
        curl.append(System.lineSeparator());
        return curl;
    }

}
