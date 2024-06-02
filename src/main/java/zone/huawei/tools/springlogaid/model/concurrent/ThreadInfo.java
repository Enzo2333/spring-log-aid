package zone.huawei.tools.springlogaid.model.concurrent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ThreadInfo {

    private String currentRequestId;

    private List<String> childThreadLogIds;

    private boolean requestLogEnabled;

    private List<String> childThreadErrorLogIds;

    private RequestAttributes requestAttributes;

    private Map<String,String> mdcMessage;

    public ThreadInfo() {
        try {
            this.currentRequestId = String.valueOf(MDC.get(AidConstants.MDC_REQUEST_ID_KEY));
            this.childThreadLogIds = LTH.getChildThreadLogIds();
            this.requestLogEnabled = LTH.isEnabled();
            this.childThreadErrorLogIds = LTH.getChildThreadErrorLogIds();
            this.requestAttributes = RequestContextHolder.getRequestAttributes();
            this.mdcMessage = MDC.getCopyOfContextMap();
        } catch (Exception e) {
            log.info("LOG AID :: ThreadInfo init error, cause :{}", e.getMessage());
        }
    }
}
