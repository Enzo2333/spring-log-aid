package zone.huawei.tools.springlogaid.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zone.huawei.tools.springlogaid.interceptors.TrackerInterceptor;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TrackerInterceptor());
    }
}