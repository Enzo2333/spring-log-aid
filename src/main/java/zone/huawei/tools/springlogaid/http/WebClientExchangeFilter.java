package zone.huawei.tools.springlogaid.http;

import jakarta.servlet.http.HttpServletRequest;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;


public class WebClientExchangeFilter implements ExchangeFilterFunction {

    private final Logger logger = LoggerFactory.getLogger(WebClientExchangeFilter.class);

    private final boolean isPrintRequestBody;

    private final boolean isPrintResponseBody;

    private final boolean requestMode;

    private final boolean hideCurl;

    private final Consumer<HttpHeaders> headersConsumer;

    private final Set<String> passToNextRequestHeaderName;

    private WebClientExchangeFilter(Builder builder) {
        this.isPrintRequestBody = builder.isPrintRequestBody;
        this.isPrintResponseBody = builder.isPrintResponseBody;
        this.headersConsumer = builder.headersConsumer;
        this.passToNextRequestHeaderName = builder.passToNextRequestHeaderName;
        this.requestMode = builder.requestMode;
        this.hideCurl = builder.hideCurl;
    }

    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull ClientRequest request,
                                       @NonNull ExchangeFunction next) {
        if (!isEnabled()) {
            return next.exchange(request);
        }
        StopWatch stopWatch = new StopWatch();
        StringBuilder requestInfoHolder = new StringBuilder();
        requestInfoHolder.append("LogRequestId:").append(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)).append(System.lineSeparator());
        ClientRequest clientRequest = ClientRequest.from(request)
                .headers(httpHeaders -> {
                    httpHeaders.add(AidConstants.INBOUND_REQUEST_ID_HEADER_KEY, UUID.randomUUID().toString());
                    if (!CollectionUtils.isEmpty(this.passToNextRequestHeaderName) && RequestContextHolder.getRequestAttributes() != null) {
                        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                        for (String headerName : this.passToNextRequestHeaderName) {
                            if (!httpHeaders.containsKey(headerName)) {
                                httpHeaders.add(headerName, httpServletRequest.getHeader(headerName));
                            }
                        }
                    }
                    if (this.headersConsumer != null)
                        this.headersConsumer.accept(httpHeaders);
                })
                .body((ClientHttpRequest outputMessage, BodyInserter.Context context) -> request.body().insert(new BufferingDecorator(outputMessage, this::traceRequest, requestInfoHolder), context)).build();
        stopWatch.start();
        Mono<ClientResponse> clientResponseMono = next.exchange(clientRequest).flatMap(clientResponse -> clientResponse.bodyToMono(String.class).flatMap(responseBody ->{
            traceResponse(clientResponse, requestInfoHolder,responseBody);
            return Mono.just(clientResponse.mutate().body(responseBody).build());
        })).doFinally((signalType) -> {
            stopWatch.stop();
            requestInfoHolder.insert(0, new StringBuilder(System.lineSeparator()).append("Request takes time: ").append(stopWatch.getTotalTimeSeconds()).append(" S").append(System.lineSeparator()));
            logger.info(requestInfoHolder.toString());
        });
        LTH.setOutboundRequestInfo(requestInfoHolder);
        return clientResponseMono;
    }

    private boolean isEnabled() {
        if (this.requestMode) {
            return LTH.isEnabled();
        } else {
            return true;
        }
    }

    private void traceRequest(ClientHttpRequest clientHttpRequest, DataBuffer dataBuffer, StringBuilder requestInfoHolder) {
        try {
            StringBuilder sb = new StringBuilder();
            String requestBody;
            sb.append("===========OUTBOUND REQUEST START============").append(System.lineSeparator());
            requestBody = Optional.ofNullable(dataBuffer).map((bodyBytes) -> dataBuffer.toString(StandardCharsets.UTF_8)).orElse(null);
            if (!this.isPrintRequestBody && StringUtils.hasText(requestBody)) {
                requestBody = "Print requestBody flag is disabled!";
            }
            sb.append(toCurl(clientHttpRequest, requestBody));
            sb.append("===========OUTBOUND REQUEST END============").append(System.lineSeparator());
            requestInfoHolder.append(sb);
        } catch (Exception e) {
            requestInfoHolder.append("Failed to tracing request in log aid,due to :").append(e).append(System.lineSeparator());
        } finally {
            MDC.clear();
        }
    }

    private StringBuilder toCurl(ClientHttpRequest request, String requestBody) {
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

    private void traceResponse(ClientResponse clientResponse, StringBuilder requestInfoHolder, String responseBody) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("===========OUTBOUND REQUEST RESPONSE START============").append(System.lineSeparator());
            sb.append("Response time: ").append(LocalDateTime.now()).append(System.lineSeparator());
            sb.append("Status code  : ").append(clientResponse.statusCode()).append(System.lineSeparator());
            sb.append("Headers      : ").append(clientResponse.headers().asHttpHeaders()).append(System.lineSeparator());
            if (this.isPrintResponseBody) {
                sb.append("Body         : ").append(responseBody).append(System.lineSeparator());
            } else {
                sb.append("Body         : ").append("body be masked, because of Print ResponseBody flag is disabled in log aid!").append(System.lineSeparator());
            }
            sb.append("===========OUTBOUND REQUEST RESPONSE END============").append(System.lineSeparator());
            requestInfoHolder.append(sb);
        } catch (Exception e) {
            requestInfoHolder.append("Failed to tracing response in log aid,due to :").append(e).append(System.lineSeparator());
        }
    }

    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private static final class BufferingDecorator extends ClientHttpRequestDecorator {
        private final TriConsumer<ClientHttpRequest, DataBuffer, StringBuilder> callback;

        private final StringBuilder requestInfoHolder;

        public BufferingDecorator(ClientHttpRequest outputMessage,
                                  TriConsumer<ClientHttpRequest, DataBuffer, StringBuilder> callback, StringBuilder requestInfoHolder) {
            super(outputMessage);
            this.callback = callback;
            this.requestInfoHolder = requestInfoHolder;
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            return DataBufferUtils.join(body).flatMap(buffer -> {
                if (callback != null) callback.accept(this, buffer, requestInfoHolder);
                return super.writeWith(Mono.just(buffer));
            });
        }

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
            this.requestMode = true;
            return this;
        }

        public Builder hideCurl() {
            this.hideCurl = true;
            return this;
        }

        public Builder usePropertiesConfig() throws UnsupportedOperationException {
            if (AidConstants.CONFIG_ENABLED) {
                this.passToNextRequestHeaderName = AidConstants.OutboundRequest.PASSING_HEADERS;
                this.isPrintRequestBody = AidConstants.OutboundRequest.PRINT_REQUEST_BODY;
                this.isPrintResponseBody = AidConstants.OutboundRequest.PRINT_RESPONSE_BODY;
            } else {
                throw new UnsupportedOperationException("LOG AID :: You can not use Properties Config in WebClientExchangeFilter.class, Because Log Aid Config is not enable, Please consider adding the @EnableAidConfig annotation in your Configuration class to enable it!");
            }
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

        public Builder setPassingToNextRequestHeaderName(String... passToNextRequestHeaderName) {
            this.passToNextRequestHeaderName.addAll(Arrays.asList(passToNextRequestHeaderName));
            return this;
        }

        public Builder setPassingToNextRequestHeaderName(List<String> passToNextRequestHeaderName) {
            this.passToNextRequestHeaderName.addAll(passToNextRequestHeaderName);
            return this;
        }

        public WebClientExchangeFilter build() {
            return new WebClientExchangeFilter(this);
        }
    }
}