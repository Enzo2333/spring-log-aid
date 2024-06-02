package zone.huawei.tools.springlogaid.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.MDC;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubThreadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -351283547061021419L;

    private Map<String, Object> messages = new HashMap<>();

    public SubThreadException() {
        super();
        initMessage();
    }

    public SubThreadException(String message) {
        super(message);
        initMessage();
    }

    public SubThreadException(String message, Throwable cause) {
        super(message, cause);
        initMessage();
    }

    public SubThreadException(Throwable cause) {
        super(cause);
        initMessage();
    }

    protected SubThreadException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        initMessage();
    }

    public void addMessage(String key, Object value) {
        this.messages.put(key, value);
    }

    private void initMessage() {
        StringBuilder outboundRequest = LTH.getOutboundRequest();
        if (outboundRequest != null) {
            addMessage("OutboundRequest", outboundRequest);
        }
        addMessage("mdcRequestId", String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY)));
    }

    @Override
    public String toString() {
        return super.getCause().toString();
    }
}
