package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import zone.huawei.tools.springlogaid.config.BeanDependencyConfigurer;
import zone.huawei.tools.springlogaid.config.JacksonConfig;
import zone.huawei.tools.springlogaid.config.LogAidConfigProps;
import zone.huawei.tools.springlogaid.config.WebMvcConfig;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.exception.AidExceptionResolver;
import zone.huawei.tools.springlogaid.exception.ResponseBodyLoggingAdvice;
import zone.huawei.tools.springlogaid.filters.LogAidFilter;
import zone.huawei.tools.springlogaid.processors.TrackingRequestProcessor;

import java.util.List;

public class AidBaseBeanRegistrar implements ImportBeanDefinitionRegistrar {

    private final List<Class<?>> beanClasses = List.of(
            JacksonConfig.class,
            LogAidConfigProps.class,
            AidConstants.class,
            BeanDependencyConfigurer.class,
            WebMvcConfig.class,
            ResponseBodyLoggingAdvice.class,
            LogAidFilter.class,
            AidExceptionResolver.class,
            TrackingRequestProcessor.class);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        beanClasses.forEach(aClass -> registry.registerBeanDefinition(aClass.getName(), BeanDefinitionBuilder.genericBeanDefinition(aClass).getBeanDefinition()));
    }
}
