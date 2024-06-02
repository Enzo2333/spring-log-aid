package zone.huawei.tools.springlogaid.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.filters.LogAidFilter;

@EnableAspectJAutoProxy
@ConditionalOnMissingBean({LogAidConfiguration.class})
@ComponentScan({"zone.huawei.tools.springlogaid"})
@Import(WebMvcConfig.class)
public class LogAidConfiguration {

    public LogAidConfiguration() {
        AidConstants.ENABLE = true;
    }

    @Bean
    @DependsOn("aidConstants")
    @ConditionalOnMissingBean({LogAidFilter.class})
    public FilterRegistrationBean<LogAidFilter> logTrackerFilter(){
        FilterRegistrationBean<LogAidFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LogAidFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);

        return registrationBean;
    }

}
