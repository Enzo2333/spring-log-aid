package zone.huawei.tools.springlogaid.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Configuration
public class BeanDependencyConfigurer implements BeanDefinitionRegistryPostProcessor {

    private static final List<Class<?>> DEPENDENCY_CLASSES = List.of(RestTemplate.class, WebClient.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        DEPENDENCY_CLASSES.stream()
                .flatMap(aClass -> Arrays.stream(BeanFactoryUtils.beanNamesForTypeIncludingAncestors((ListableBeanFactory) registry, aClass)))
                .map(registry::getBeanDefinition)
                .forEach(beanDefinition -> beanDefinition.setDependsOn("zone.huawei.tools.springlogaid.constants.AidConstants"));
    }

}
