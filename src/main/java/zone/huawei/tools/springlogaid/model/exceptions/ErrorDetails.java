package zone.huawei.tools.springlogaid.model.exceptions;

import lombok.Data;

@Data
public class ErrorDetails {

    private int id;

    private boolean isFromSubThread;

    private String errorRequestId;

    private String errorMessage;

    private String filteredErrorStackTrace;

    private boolean hasOriginalError;

    private String originalErrorCause;

    private String originalErrorCauseFilteredStackTrace;

    private String completeErrorStackTrace;

    private String originalErrorCauseCompleteStackTrace;
}
