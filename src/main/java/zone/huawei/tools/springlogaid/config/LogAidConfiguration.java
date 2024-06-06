package zone.huawei.tools.springlogaid.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import zone.huawei.tools.springlogaid.constants.AidConstants;

@EnableAspectJAutoProxy
@ConditionalOnMissingBean({LogAidConfiguration.class})
@ComponentScan({"zone.huawei.tools.springlogaid"})
@Import(WebMvcConfig.class)
public class LogAidConfiguration {

    public LogAidConfiguration() {
        AidConstants.ENABLE = true;
    }

}
