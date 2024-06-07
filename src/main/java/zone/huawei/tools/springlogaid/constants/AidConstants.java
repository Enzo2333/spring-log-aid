package zone.huawei.tools.springlogaid.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import zone.huawei.tools.springlogaid.config.LogAidConfigProps;
import zone.huawei.tools.springlogaid.enums.OperatingMode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

public class AidConstants {

    public static final Object DEFAULT_VALUE = new Object();

    public static final Set<RequestMappingInfo> ACTIVATED_REQUEST_MAPPING = new CopyOnWriteArraySet<>();

    public static final Set<Pattern> FILTER_EXCLUDE_URI_PATTERNS = new HashSet<>();

    public static final String INBOUND_REQUEST_ID_HEADER_KEY = "log-trace-requestId";

    public static final String TRACE_REQUEST_ID = "TraceRequestID";

    public static final String ERROR_REQUEST_ID = "ErrorRequestID";

    public static final String SUMMARY_REPORT = "SummaryReport";

    public static String MDC_REQUEST_ID_KEY;

    public static String BASE_PACKAGE;

    public static Boolean ENABLE;

    public static OperatingMode MODE;

    public static Boolean GLOBAL_MODE;

    public static boolean CONFIG_ENABLED;

    public static Boolean ENABLE_INBOUND_REQUEST_GLOBAL;

    public static Boolean ENABLE_TRACKING_REQUEST;

    public static LogLevel LOG_LEVER;

    public static String CONTEXT_DIVIDER_STYLE;

    public static class OutboundRequest {

        public static Set<String> PASSING_HEADERS = new HashSet<>();
        ;

        public static Boolean PRINT_REQUEST_BODY;

        public static Boolean PRINT_RESPONSE_BODY;
    }

    public static class InboundRequest {

        public static Boolean PRINT_REQUEST_BODY;

        public static Boolean PRINT_RESPONSE_BODY;
    }

    public AidConstants(LogAidConfigProps configProps) {
        CONFIG_ENABLED = true;

        if ((ENABLE_TRACKING_REQUEST == null || !ENABLE_TRACKING_REQUEST) && configProps.getMode() != null) {
            ENABLE_TRACKING_REQUEST = configProps.getMode() == OperatingMode.Request;
        }
        if ((ENABLE_TRACKING_REQUEST == null || !ENABLE_TRACKING_REQUEST) && configProps.getOutboundRequest().getMode() != null) {
            ENABLE_TRACKING_REQUEST = configProps.getOutboundRequest().getMode() == OperatingMode.Request;
        }
        if ((ENABLE_TRACKING_REQUEST == null || !ENABLE_TRACKING_REQUEST) && configProps.getInboundRequest().getMode() != null) {
            ENABLE_TRACKING_REQUEST = configProps.getInboundRequest().getMode() == OperatingMode.Request;
        }
        if (ENABLE_TRACKING_REQUEST == null) {
            ENABLE_TRACKING_REQUEST = false;
        }

        if (configProps.getPrintInboundRequestExclusionUris() != null) {
            for (String exclusionUri : configProps.getPrintInboundRequestExclusionUris()) {
                FILTER_EXCLUDE_URI_PATTERNS.add(Pattern.compile(exclusionUri));
            }
        }
        if (LOG_LEVER == null) {
            LOG_LEVER = configProps.getLogLevel();
        }
        if (MDC_REQUEST_ID_KEY == null && StringUtils.hasText(configProps.getMdcRequestIdName())) {
            MDC_REQUEST_ID_KEY = configProps.getMdcRequestIdName();
        }
        if (MDC_REQUEST_ID_KEY == null) {
            MDC_REQUEST_ID_KEY = "requestId";
        }
        if (configProps.getOutboundRequest().getPassingHeaders() != null) {
            OutboundRequest.PASSING_HEADERS.addAll(configProps.getOutboundRequest().getPassingHeaders());
        }
        if (OutboundRequest.PRINT_REQUEST_BODY == null) {
            OutboundRequest.PRINT_REQUEST_BODY = configProps.getOutboundRequest().getPrintRequestBody();
        }
        if (OutboundRequest.PRINT_RESPONSE_BODY == null) {
            OutboundRequest.PRINT_RESPONSE_BODY = configProps.getOutboundRequest().getPrintResponseBody();
        }

        if (MODE == null && configProps.getMode() != null) {
            MODE = configProps.getMode();
        }
        if (MODE == null) {
            MODE = OperatingMode.Global;
        }

        if (ENABLE_INBOUND_REQUEST_GLOBAL == null && configProps.getInboundRequest().getMode() != null) {
            ENABLE_INBOUND_REQUEST_GLOBAL = configProps.getInboundRequest().getMode() == OperatingMode.Global;
        }
        if (ENABLE_INBOUND_REQUEST_GLOBAL == null) {
            ENABLE_INBOUND_REQUEST_GLOBAL = MODE.equals(OperatingMode.Global);
        }

        if (InboundRequest.PRINT_REQUEST_BODY == null) {
            InboundRequest.PRINT_REQUEST_BODY = configProps.getInboundRequest().getPrintRequestBody();
        }
        if (InboundRequest.PRINT_RESPONSE_BODY == null) {
            InboundRequest.PRINT_RESPONSE_BODY = configProps.getInboundRequest().getPrintResponseBody();
        }

        if (OutboundRequest.PRINT_REQUEST_BODY == null) {
            OutboundRequest.PRINT_REQUEST_BODY = true;
        }
        if (OutboundRequest.PRINT_RESPONSE_BODY == null) {
            OutboundRequest.PRINT_RESPONSE_BODY = true;
        }

        if (InboundRequest.PRINT_REQUEST_BODY == null) {
            InboundRequest.PRINT_REQUEST_BODY = true;
        }
        if (InboundRequest.PRINT_RESPONSE_BODY == null) {
            InboundRequest.PRINT_RESPONSE_BODY = true;
        }

        if (!StringUtils.hasText(CONTEXT_DIVIDER_STYLE)) {
            CONTEXT_DIVIDER_STYLE = "=============================================================================" + System.lineSeparator();
        }

        GLOBAL_MODE = MODE.equals(OperatingMode.Global);
        String basePackage = configProps.getBasePackage();
        if (StringUtils.hasText(basePackage)) {
            BASE_PACKAGE = basePackage;
        } else {
            BASE_PACKAGE = configProps.getContext().getBeansWithAnnotation(SpringBootApplication.class).values().stream().findFirst().get().getClass().getPackageName();
        }
    }

    public static boolean isGlobalMode() {
        return GLOBAL_MODE;
    }

    public enum LogLevel {

        INFO, WARN, ERROR, DEBUG, TRACE, DEFAULT;

        @JsonValue
        public String getValue() {
            return this.name();
        }

        @Override
        public String toString() {
            return this.name();
        }

        @JsonCreator
        public static LogLevel fromValue(String value) {
            for (LogLevel level : LogLevel.values()) {
                if (level.toString().equalsIgnoreCase(value)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("LOG AID :: Unexpected value '" + value + "'");
        }
    }


}
