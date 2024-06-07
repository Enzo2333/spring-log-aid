package zone.huawei.tools.springlogaid.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.exception.SubThreadException;
import zone.huawei.tools.springlogaid.model.exceptions.ErrorDetails;
import zone.huawei.tools.springlogaid.utils.LogAidUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class ThreadLogContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -5398519900408587015L;

    private boolean requestEnabled = false;

    private Map<String, String> logKeyMap = new ConcurrentHashMap<>();

    private boolean inboundRequestEnabled;

    private boolean outboundRequestEnabled;

    private StringBuilder inboundRequest;

    private StringBuilder inboundRequestResponseBody;

    private StopWatch inboundRequestStopWatch;

    private StringBuilder outboundRequest;

    private String nextTrackURL;

    private String nextTrackRequestId;

    private ErrorDetails errorDetails;

    private StringBuilder s3Request;

    private ChildThreadIdInfo childThreadIdInfo;

    private Map<String, Object> additionalLogMap;

    public void initErrorDetails(Throwable e) {
        if (e == null) {
            log.info("LogTracker:: initErrorDetails : Exception can not be null.");
            return;
        }

        ErrorDetails ex = new ErrorDetails();
        if (e instanceof CompletionException) {
            e = e.getCause();
        }

        if (e instanceof SubThreadException subThreadException) {
            String mdcRequestId = (String) subThreadException.getMessages().get("mdcRequestId");
            ex.setFromSubThread(true);
            ex.setErrorRequestId(mdcRequestId);
            logKeyMap.put(AidConstants.ERROR_REQUEST_ID, mdcRequestId);
            Object outboundRequest = subThreadException.getMessages().get("OutboundRequest");
            if (outboundRequest != null) {
                this.outboundRequest = (StringBuilder) outboundRequest;
            }
            e = e.getCause();
        }

        ex.setId(e.hashCode());
        logKeyMap.put("ErrorId", String.valueOf(ex.getId()));

        ex.setErrorMessage(e.getMessage());

        ex.setFilteredErrorStackTrace(LogAidUtil.filterStackTrace(e));
        if (e.getCause() != null) {
            ex.setHasOriginalError(true);
            Throwable cause = e.getCause();
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            ex.setOriginalErrorCause(cause.getMessage());
            StringBuilder originalCauseFilteredStackTrace = new StringBuilder();
            StringBuilder originalCauseFullStackTrace = new StringBuilder();
            originalCauseFilteredStackTrace.append(LogAidUtil.filterStackTrace(cause));
            originalCauseFullStackTrace.append(LogAidUtil.fullStackTrace(cause));
            for (Throwable suppressed : cause.getSuppressed()) {
                originalCauseFilteredStackTrace.append(LogAidUtil.filterStackTrace(suppressed));
                originalCauseFullStackTrace.append(LogAidUtil.fullStackTrace(suppressed));
            }
            ex.setOriginalErrorCauseFilteredStackTrace(originalCauseFilteredStackTrace.toString());
            ex.setOriginalErrorCauseCompleteStackTrace(originalCauseFullStackTrace.toString());
        } else {
            ex.setCompleteErrorStackTrace(LogAidUtil.fullStackTrace(e));
        }
        errorDetails = ex;
    }

    private void initRequestId() {
        if (this.inboundRequest != null && !logKeyMap.containsKey(AidConstants.SUMMARY_REPORT))
            logKeyMap.put(AidConstants.SUMMARY_REPORT, String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)));
        if (!logKeyMap.containsKey(AidConstants.TRACE_REQUEST_ID))
            logKeyMap.put(AidConstants.TRACE_REQUEST_ID, String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)));
        if (errorDetails != null && !logKeyMap.containsKey(AidConstants.ERROR_REQUEST_ID))
            logKeyMap.put(AidConstants.ERROR_REQUEST_ID, String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)));
    }

    public String generateReport() {
        initRequestId();
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        for (Map.Entry<String, String> entry : logKeyMap.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(System.lineSeparator());
        }
        sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        if (childThreadIdInfo != null && !CollectionUtils.isEmpty(childThreadIdInfo.getChildThreadLogIds())) {
            sb.append("ChildThreadLogIds:").append(childThreadIdInfo.getChildThreadLogIds()).append(System.lineSeparator());
            if (!CollectionUtils.isEmpty(childThreadIdInfo.getChildThreadErrorLogIds())) {
                sb.append("ChildThreadErrorLogIds:").append(childThreadIdInfo.getChildThreadErrorLogIds()).append(System.lineSeparator());
                sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
            }
        }
        if (errorDetails != null) {
            if (errorDetails.isFromSubThread()) {
                sb.append("Note: You have an exception from a child thread. You can use this requestId to find all logs of this thread:").append(errorDetails.getErrorRequestId()).append(System.lineSeparator()).append(System.lineSeparator());
            }
            sb.append("Request failed, due to:").append(errorDetails.getErrorMessage()).append(System.lineSeparator());
            sb.append("Exception Filtered StackTrace:").append(errorDetails.getFilteredErrorStackTrace()).append(System.lineSeparator());
            if (errorDetails.isHasOriginalError()) {
                sb.append("Original cause:").append(errorDetails.getOriginalErrorCause()).append(System.lineSeparator());
                sb.append("Original cause Filtered StackTrace:").append(errorDetails.getOriginalErrorCauseFilteredStackTrace()).append(System.lineSeparator());
            }
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        }
        if (additionalLogMap != null && !additionalLogMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : additionalLogMap.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue().toString()).append(System.lineSeparator());
            }
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        }
        if (outboundRequest != null && !outboundRequest.isEmpty()) {
            sb.append("Last Outbound Request & Response Info:").append(System.lineSeparator());
            sb.append(outboundRequest).append(System.lineSeparator());
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
            if (StringUtils.hasText(nextTrackRequestId)) {
                sb.append("NextTrackURL:").append(nextTrackURL).append(System.lineSeparator());
                sb.append("NextTrackRequestId:").append(nextTrackRequestId).append(System.lineSeparator());
                sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
            }
        }
        if (s3Request != null && !s3Request.isEmpty()) {
            sb.append("Last Outbound S3 Request & Response Info:").append(System.lineSeparator());
            sb.append(s3Request).append(System.lineSeparator());
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        }
        if (inboundRequest != null && !inboundRequest.isEmpty()) {
            sb.append("Inbound Request Info:").append(System.lineSeparator());
            sb.append(inboundRequest).append(System.lineSeparator());
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        }
        if (errorDetails != null) {
            if (errorDetails.isHasOriginalError()) {
                sb.append("Original Error Cause Complete StackTrace:").append(System.lineSeparator());
                sb.append(errorDetails.getOriginalErrorCauseCompleteStackTrace()).append(System.lineSeparator());
            } else {
                sb.append("Exception Complete Error StackTrace:").append(System.lineSeparator());
                sb.append(errorDetails.getCompleteErrorStackTrace()).append(System.lineSeparator());
            }
            sb.append(AidConstants.CONTEXT_DIVIDER_STYLE);
        }
        return sb.toString();
    }
}
