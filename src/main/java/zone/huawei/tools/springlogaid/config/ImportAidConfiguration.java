package zone.huawei.tools.springlogaid.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import zone.huawei.tools.springlogaid.annotations.registrars.AidBaseBeanRegistrar;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.processors.RequestMappingInfoCollector;

@Configuration
@Import({AidBaseBeanRegistrar.class})
@ConditionalOnProperty(
        name = {"log.aid.enable"},
        havingValue = "true"
)
public class ImportAidConfiguration implements BeanDefinitionRegistryPostProcessor {

    public ImportAidConfiguration() {
        AidConstants.ENABLE = true;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition(RequestMappingInfoCollector.class.getName())) {
            return;
        }
        registry.registerBeanDefinition(RequestMappingInfoCollector.class.getName(), BeanDefinitionBuilder.genericBeanDefinition(RequestMappingInfoCollector.class).getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}