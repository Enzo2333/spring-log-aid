package zone.huawei.tools.springlogaid.http;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static zone.huawei.tools.springlogaid.constants.AidConstants.ENABLE_TRACKING_REQUEST;

/**
 * Please use BufferingClientHttpRequestFactory when instantiating RestTemplate,
 * otherwise this interceptor will make your request body and response body disappear.
 * example:{
 * ClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory)
 * RestTemplate restTemplate = new RestTemplate(bufferingRequestFactory)
 * restTemplate.getInterceptors().add(new RestTemplateInterceptor(new RestTemplateInterceptor.Builder()))
 * }
 */

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RestTemplateInterceptor.class);

    private final boolean isPrintRequestBody;

    private final boolean isPrintResponseBody;

    private final boolean requestMode;

    private final boolean hideCurl;

    private final Consumer<HttpHeaders> headersConsumer;

    private final Set<String> passToNextRequestHeaderName;

    private RestTemplateInterceptor(Builder builder) {
        this.isPrintRequestBody = builder.isPrintRequestBody;
        this.isPrintResponseBody = builder.isPrintResponseBody;
        this.headersConsumer = builder.headersConsumer;
        this.passToNextRequestHeaderName = builder.passToNextRequestHeaderName;
        this.requestMode = builder.requestMode;
        this.hideCurl = builder.hideCurl;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request,@NonNull  byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        if (!isEnabled()) {
            return execution.execute(request, body);
        }
        addRequestHeaders(request);
        StringBuilder requestInfo = new StringBuilder();
        LTH.setOutboundRequestInfo(requestInfo);
        StopWatch stopWatch = new StopWatch();
        try {
            traceRequest(request, body, requestInfo);
        } catch (Exception e) {
            requestInfo.append("Failed to tracing request in log aid,due to :").append(e).append(System.lineSeparator());
        }
        ClientHttpResponse response = null;
        try {
            stopWatch.start();
            response = execution.execute(request, body);
            stopWatch.stop();
        } catch (IOException e) {
            logger.info(requestInfo.toString());
            throw e;
        }
        requestInfo.insert(0, new StringBuilder(System.lineSeparator()).append("Request takes time: ").append(stopWatch.getTotalTimeSeconds()).append(" S").append(System.lineSeparator()));
        try {
            traceResponse(response, request, requestInfo);
        } catch (IOException e) {
            requestInfo.append("Failed to tracing response in log aid,due to :").append(e).append(System.lineSeparator());
            if (LTH.isOutboundRequestEnabled())
                logger.info(requestInfo.toString());
        }
        return response;
    }

    private boolean isEnabled() {
        if (this.requestMode) {
            return LTH.isEnabled() || LTH.isOutboundRequestEnabled();
        } else {
            return true;
        }
    }

    private void addRequestHeaders(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        headers.add(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, UUID.randomUUID().toString());
        if (!CollectionUtils.isEmpty(this.passToNextRequestHeaderName) && RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            for (String headerName : this.passToNextRequestHeaderName) {
                if (!headers.containsKey(headerName)) {
                    headers.add(headerName, httpServletRequest.getHeader(headerName));
                }
            }
        }
        if (this.headersConsumer != null)
            this.headersConsumer.accept(headers);
    }

    private void traceRequest(HttpRequest request, byte[] body, StringBuilder requestInfo) {
        StringBuilder sb = new StringBuilder();
        String requestBody;
        sb.append("LogRequestId:").append(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)).append(System.lineSeparator());
        sb.append("===========OUTBOUND REQUEST START============").append(System.lineSeparator());
        sb.append("Request time: ").append(LocalDateTime.now()).append(System.lineSeparator());
        sb.append("Curl: ").append(System.lineSeparator());
        requestBody = Optional.ofNullable(body).map((bodyBytes) -> new String(bodyBytes, StandardCharsets.UTF_8)).orElse(null);
        if (!this.isPrintRequestBody && StringUtils.hasText(requestBody)) {
            requestBody = "Print requestBody flag of log aid is disabled!";
        }
        sb.append(toCurl(request, requestBody));
        sb.append("===========OUTBOUND REQUEST END============").append(System.lineSeparator());
        requestInfo.append(sb);
    }

    private StringBuilder toCurl(HttpRequest request, String requestBody) {
        StringBuilder curl = new StringBuilder();
        curl.append(System.lineSeparator());
        curl.append("curl --location --request ");
        HttpMethod method = request.getMethod();
        curl.append(method);

        curl.append(" '").append(request.getURI()).append("' \\").append(System.lineSeparator());

        HttpHeaders headers = request.getHeaders();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (String value : header.getValue()) {
                curl.append("--header '").append(header.getKey()).append(": ").append(value).append("' \\").append(System.lineSeparator());
            }
        }
        if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) {
            if (requestBody != null) {
                curl.append("--data-raw '").append(requestBody).append("'");
            }
        }
        curl.append(System.lineSeparator()).append(System.lineSeparator());
        return curl;
    }

    protected void traceResponse(ClientHttpResponse response, HttpRequest request, StringBuilder requestInfo) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("===========OUTBOUND REQUEST RESPONSE START============").append(System.lineSeparator());
        sb.append("Response time: ").append(LocalDateTime.now()).append(System.lineSeparator());
        sb.append("Status code  : ").append(response.getStatusCode()).append(System.lineSeparator());
        sb.append("Status text  : ").append(response.getStatusText()).append(System.lineSeparator());
        sb.append("Headers      : ").append(response.getHeaders()).append(System.lineSeparator());
        if (this.isPrintResponseBody) {
            sb.append("Body         : ").append(IOUtils.toString(response.getBody(), StandardCharsets.UTF_8)).append(System.lineSeparator());
        } else {
            sb.append("Body         : ").append("body be masked, because of Print ResponseBody flag is disabled in log aid!").append(System.lineSeparator());
        }
        sb.append("===========OUTBOUND REQUEST RESPONSE END============").append(System.lineSeparator());
        if (response.getStatusCode().toString().startsWith("4") || response.getStatusCode().toString().startsWith("5")) {
            LTH.setNextTrackURL(request.getURI().toString());
            LTH.setNextTrackRequestId(request.getHeaders().getFirst(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY));
        }
        requestInfo.append(sb);
        logger.info(requestInfo.toString());
    }

    public static final class Builder {
        private boolean isPrintRequestBody;

        private boolean isPrintResponseBody;

        private boolean requestMode;

        private boolean hideCurl;

        private Consumer<HttpHeaders> headersConsumer;

        private Set<String> passToNextRequestHeaderName = new HashSet<>();

        public Builder() {
            this.isPrintRequestBody = true;
            this.isPrintResponseBody = true;
            this.requestMode = false;
            this.hideCurl = false;
        }

        public static Builder of() {
            return new Builder();
        }

        public Builder afterSettingHeaders(Consumer<HttpHeaders> headersConsumer) {
            this.headersConsumer = headersConsumer;
            return this;
        }

        public Builder usePrivateMode() {
            if (!ENABLE_TRACKING_REQUEST) {
                throw new UnsupportedOperationException("LOG AID :: You can not use Private Mode in RestTemplateInterceptor.class, Because Request Mode is not enable, Please consider set the scope = OperatingMode.Request in @EnableOutboundRequestConfig or @EnableLogAid annotation in your Configuration class to enable it!");
            }
            this.requestMode = true;
            return this;
        }

        public Builder hideCurl() {
            this.hideCurl = true;
            return this;
        }

        public Builder setIsPrintRequestBody(boolean isPrintRequestBody) {
            this.isPrintRequestBody = isPrintRequestBody;
            return this;
        }

        public Builder setIsPrintResponseBody(boolean isPrintResponseBody) {
            this.isPrintResponseBody = isPrintResponseBody;
            return this;
        }

        public Builder addPassingToNextRequestHeaderName(String... passToNextRequestHeaderName) {
            this.passToNextRequestHeaderName.addAll(Arrays.asList(passToNextRequestHeaderName));
            return this;
        }

        public Builder addPassingToNextRequestHeaderName(List<String> passToNextRequestHeaderName) {
            this.passToNextRequestHeaderName.addAll(passToNextRequestHeaderName);
            return this;
        }

        public Builder usePropertiesConfig() throws UnsupportedOperationException {
            if (AidConstants.CONFIG_ENABLED) {
                this.passToNextRequestHeaderName = AidConstants.OutboundRequest.PASSING_HEADERS;
                this.isPrintRequestBody = AidConstants.OutboundRequest.PRINT_REQUEST_BODY;
                this.isPrintResponseBody = AidConstants.OutboundRequest.PRINT_RESPONSE_BODY;
            } else {
                throw new UnsupportedOperationException("LOG AID :: You can not use Properties Config in RestTemplateInterceptor.class, Because Outbound Request Config is not enable, Please consider add the @EnableOutboundRequestConfig annotation in your Configuration class to enable it!");
            }
            return this;
        }

        public RestTemplateInterceptor build() {
            return new RestTemplateInterceptor(this);
        }
    }
}
