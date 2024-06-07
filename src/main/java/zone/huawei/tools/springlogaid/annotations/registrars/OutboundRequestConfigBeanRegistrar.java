package zone.huawei.tools.springlogaid.annotations.registrars;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import zone.huawei.tools.springlogaid.config.BeanDependencyConfigurer;
import zone.huawei.tools.springlogaid.config.LogAidConfigProps;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.exception.AidExceptionResolver;

import java.util.List;


public class OutboundRequestConfigBeanRegistrar implements ImportBeanDefinitionRegistrar {

    private final List<Class<?>> beanClasses = List.of(
            LogAidConfigProps.class,
            AidConstants.class,
            BeanDependencyConfigurer.class,
            AidExceptionResolver.class);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        beanClasses.forEach(aClass -> {
            if (registry.containsBeanDefinition(aClass.getName())) {
                return;
            }
            registry.registerBeanDefinition(aClass.getName(), BeanDefinitionBuilder.genericBeanDefinition(aClass).getBeanDefinition());
        });
    }

}
