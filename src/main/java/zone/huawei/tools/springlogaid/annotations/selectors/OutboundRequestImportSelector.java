package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.annotation.AnnotationAttributes;
import zone.huawei.tools.springlogaid.annotations.EnableOutboundRequestConfig;
import zone.huawei.tools.springlogaid.enums.AidBoolean;
import zone.huawei.tools.springlogaid.enums.OperatingMode;
import zone.huawei.tools.springlogaid.processors.RequestMappingInfoCollector;

import java.util.List;

import static zone.huawei.tools.springlogaid.constants.AidConstants.OutboundRequest.*;
import static zone.huawei.tools.springlogaid.constants.AidConstants.ENABLE_TRACKING_REQUEST;

public class OutboundRequestImportSelector extends ConfigImportSelector<EnableOutboundRequestConfig> implements BeanDefinitionRegistryPostProcessor {

    @Override
    protected String[] selectImports(AnnotationAttributes attributes) {
        settingConstants(attributes);
        return new String[0];
    }

    private void settingConstants(AnnotationAttributes attributes) {
        OperatingMode mode = attributes.getEnum("scope");
        String[] passingHeaders = attributes.getStringArray("passingHeaders");
        AidBoolean printRequestBody = attributes.getEnum("printRequestBody");
        AidBoolean printResponseBody = attributes.getEnum("printResponseBody");

        if (mode == OperatingMode.Request) {
            ENABLE_TRACKING_REQUEST = true;
        }
        if (printResponseBody != AidBoolean.Default)
            PRINT_REQUEST_BODY = printRequestBody == AidBoolean.True;
        if (printResponseBody != AidBoolean.Default)
            PRINT_RESPONSE_BODY = printResponseBody == AidBoolean.True;
        if (passingHeaders.length > 0)
            PASSING_HEADERS.addAll(List.of(passingHeaders));
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
