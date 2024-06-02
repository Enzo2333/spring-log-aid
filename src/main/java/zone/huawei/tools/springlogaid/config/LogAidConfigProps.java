package zone.huawei.tools.springlogaid.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.enums.OperatingMode;

import java.util.Set;

@Data
@Component
@ConfigurationProperties("log.aid")
public class LogAidConfigProps {

    private ApplicationContext context;

    private Boolean enable;

    private OperatingMode mode;

    private Set<String> printInboundRequestExclusionUris;

    private AidConstants.LogLevel logLevel;

    private String mdcRequestIdName;

    private String basePackage;

    private InboundRequest inboundRequest = new InboundRequest();

    private OutboundRequest outboundRequest = new OutboundRequest();

    @Data
    public static class OutboundRequest {
        private Set<String> passingHeaders;

        private Boolean printRequestBody;

        private Boolean printResponseBody = true;
    }

    @Data
    public static class InboundRequest {

        private Boolean printRequestBody;

        private Boolean printResponseBody;
    }

    @Autowired
    public LogAidConfigProps(ApplicationContext context){
        this.context = context;
    }
}
