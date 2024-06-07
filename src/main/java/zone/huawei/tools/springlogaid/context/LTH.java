package zone.huawei.tools.springlogaid.context;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.model.ChildThreadIdInfo;
import zone.huawei.tools.springlogaid.model.ThreadLogContext;
import zone.huawei.tools.springlogaid.model.concurrent.ThreadInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static zone.huawei.tools.springlogaid.constants.AidConstants.ENABLE_INBOUND_REQUEST_GLOBAL;
import static zone.huawei.tools.springlogaid.constants.AidConstants.LogLevel.*;

public class LTH {

    private static final Logger logger = LoggerFactory.getLogger(LTH.class);

    private static final ThreadLocal<ThreadLogContext> threadLogContext = new ThreadLocal<>();

    public static boolean isEnabled() {
        return getThreadLogContext().isRequestEnabled();
    }

    public static void enable() {
        getThreadLogContext().setRequestEnabled(true);
    }

    public static void enableInboundRequest() {
        getThreadLogContext().setInboundRequestEnabled(true);
    }

    public static boolean isInboundRequestEnabled() {
        return getThreadLogContext().isInboundRequestEnabled();
    }

    public static void enableOutboundRequest() {
        getThreadLogContext().setOutboundRequestEnabled(true);
    }

    public static boolean isOutboundRequestEnabled() {
        return getThreadLogContext().isOutboundRequestEnabled();
    }

    public static boolean error() {
        return getThreadLogContext().getErrorDetails() != null;
    }

    public static void setStopWatch(StopWatch stopWatch) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().setInboundRequestStopWatch(stopWatch);
    }

    public static void setErrorThreadInfo(ThreadInfo threadInfo, Throwable e) {
        if (!isEnabled()) {
            return;
        }
        threadInfo.getChildThreadErrorLogIds().add(String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)));
        LTH.addLogKey(AidConstants.TRACE_REQUEST_ID, threadInfo.getCurrentRequestId());
        LTH.initException(e);
    }

    public static StringBuilder getOutboundRequest() {
        return getThreadLogContext().getOutboundRequest();
    }

    public static ThreadLogContext getThreadLogContext() {
        ThreadLogContext trackLogContext = threadLogContext.get();
        if (trackLogContext == null) {
            trackLogContext = new ThreadLogContext();
            threadLogContext.set(trackLogContext);
        }
        return trackLogContext;
    }

    public static ChildThreadIdInfo getChildThreadIdInfo() {
        ChildThreadIdInfo subThreadInfo = getThreadLogContext().getChildThreadIdInfo();
        if (subThreadInfo == null) {
            subThreadInfo = new ChildThreadIdInfo();
            getThreadLogContext().setChildThreadIdInfo(subThreadInfo);
        }
        return subThreadInfo;
    }

    public static void setNextTrackURL(String url) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().setNextTrackURL(url);
    }

    public static List<String> getChildThreadErrorLogIds() {
        List<String> errorList = getChildThreadIdInfo().getChildThreadErrorLogIds();
        if (CollectionUtils.isEmpty(errorList)) {
            errorList = new CopyOnWriteArrayList<>();
            getChildThreadIdInfo().setChildThreadErrorLogIds(errorList);
        }
        return errorList;
    }

    public static List<String> getChildThreadLogIds() {
        List<String> childThreadLogIds = getChildThreadIdInfo().getChildThreadLogIds();
        if (childThreadLogIds == null) {
            childThreadLogIds = new CopyOnWriteArrayList<>();
            getChildThreadIdInfo().setChildThreadLogIds(childThreadLogIds);
        }
        return childThreadLogIds;
    }

    public static void saveS3RequestInfo(String requestInfo) {
        if (!isEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(requestInfo).append(System.lineSeparator());
        getThreadLogContext().setS3Request(sb);
    }

    public static void saveS3ResponseInfo(String responseInfo) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().getS3Request().append(responseInfo).append(System.lineSeparator());
    }

    public static Map<String, Object> getAdditionalLogMap() {
        Map<String, Object> map = getThreadLogContext().getAdditionalLogMap();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            getThreadLogContext().setAdditionalLogMap(map);
        }
        return map;
    }

    public static void addAdditionalLog(String key, Object value) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> map = getAdditionalLogMap();
        map.put(key, value);
    }

    public static void removeAdditionalLog(String key) {
        if (!isEnabled()) {
            return;
        }
        getAdditionalLogMap().remove(key);
    }

    public static void setNextTrackRequestId(String requestId) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().setNextTrackRequestId(requestId);
    }

    public static String getNextTrackRequestId() {
        return getThreadLogContext().getNextTrackRequestId();
    }

    public static void initException(Throwable e) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().initErrorDetails(e);
    }

    public static void clear() {
        threadLogContext.remove();
    }

    public static Map<String, String> getLogKeyMap() {
        Map<String, String> logKeyMap = getThreadLogContext().getLogKeyMap();
        if (logKeyMap == null) {
            logKeyMap = new ConcurrentHashMap<>();
            getThreadLogContext().setLogKeyMap(logKeyMap);
        }
        return logKeyMap;
    }

    public static void addLogKey(String key, String value) {
        if (!isEnabled()) {
            return;
        }
        getLogKeyMap().put(key, value);
    }

    public static void removeLogKey(String key) {
        getLogKeyMap().remove(key);
    }

    public static void setInboundRequest(StringBuilder request) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().setInboundRequest(request);
    }

    public static void saveExternalResponse(StringBuilder response) {
        if (!isEnabled()) {
            return;
        }
        getThreadLogContext().getInboundRequest().append(response).append(System.lineSeparator());
        StopWatch stopWatch = getThreadLogContext().getInboundRequestStopWatch();
        if (stopWatch != null) {
            stopWatch.stop();
            getThreadLogContext().getInboundRequest().insert(0, new StringBuilder().append(System.lineSeparator()).append("Request takes time: ").append(stopWatch.getTotalTimeSeconds()).append(" S").append(System.lineSeparator()));
        }
    }

    public static void saveOutboundRequestInfo(String requestInfo) {
        if (!isEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(requestInfo).append(System.lineSeparator());
        getThreadLogContext().setOutboundRequest(sb);
    }

    public static void setOutboundRequestInfo(StringBuilder requestInfo) {
        if (!isEnabled()) {
            return;
        }
        requestInfo.append(System.lineSeparator());
        getThreadLogContext().setOutboundRequest(requestInfo);
    }

    public static StringBuilder getOutboundRequestInfo() {
        return getThreadLogContext().getOutboundRequest();
    }

    public static String getThreadLogReport() {
        if (!AidConstants.ENABLE) {
            return "LOG AID :: No Log Tracker error message, Log Tracker not enabled.";
        }
        return getThreadLogContext().generateReport();
    }

    public static void setInboundRequestResponseBody(StringBuilder responseBody) {
        if (!ENABLE_INBOUND_REQUEST_GLOBAL && !isEnabled()) {
            return;
        }
        getThreadLogContext().setInboundRequestResponseBody(responseBody);
    }

    public static void traceResponse(HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()).append("===========INBOUND REQUEST RESPONSE START============").append(System.lineSeparator());
        sb.append("Status code  : ").append(response.getStatus()).append(System.lineSeparator());
        for (String headerName : response.getHeaderNames()) {
            sb.append("Header: ")
                    .append(headerName).append("=").append(response.getHeader(headerName))
                    .append(System.lineSeparator());
        }
        if (AidConstants.InboundRequest.PRINT_RESPONSE_BODY) {
            if (getThreadLogContext().getInboundRequestResponseBody() != null) {
                sb.append("Body  : ").append(getThreadLogContext().getInboundRequestResponseBody()).append(System.lineSeparator());
            }
        }
        sb.append("===========INBOUND REQUEST RESPONSE END============");
        saveExternalResponse(sb);
        if (isInboundRequestEnabled()) {
            logger.info(sb.toString());
        }
    }

    public static void logInfo() {
        if (!isEnabled() || !error()) {
            return;
        }

        if (AidConstants.LOG_LEVER == INFO) {
            logger.info(getThreadLogReport());
        } else if (AidConstants.LOG_LEVER == WARN) {
            logger.warn(getThreadLogReport());
        } else if (AidConstants.LOG_LEVER == ERROR) {
            logger.error(getThreadLogReport());
        } else if (AidConstants.LOG_LEVER == DEBUG) {
            logger.debug(getThreadLogReport());
        } else if (AidConstants.LOG_LEVER == TRACE) {
            logger.trace(getThreadLogReport());
        } else {
            logger.info(getThreadLogReport());
        }
    }

    public static void logInfoAndClear() {
        logInfo();
        clear();
    }
}
