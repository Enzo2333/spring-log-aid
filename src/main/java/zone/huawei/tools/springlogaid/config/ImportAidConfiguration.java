package zone.huawei.tools.springlogaid.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LogAidConfiguration.class})
@ConditionalOnProperty(
        name = {"log.aid.enable"},
        havingValue = "true"
)
public class ImportAidConfiguration {

}