package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import zone.huawei.tools.springlogaid.annotations.EnableLogInboundRequest;
import zone.huawei.tools.springlogaid.enums.AidBoolean;
import zone.huawei.tools.springlogaid.enums.OperatingMode;
import zone.huawei.tools.springlogaid.processors.RequestMappingInfoCollector;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;

public class AidInboundRequestImportSelector extends ConfigImportSelector<EnableLogInboundRequest> implements BeanDefinitionRegistryPostProcessor {

    @Override
    protected String[] selectImports(AnnotationAttributes attributes) {
        settingConstants(attributes);
        return new String[0];
    }

    private void settingConstants(AnnotationAttributes attributes) {
        OperatingMode mode = attributes.getEnum("scope");
        AidBoolean printRequestBody = attributes.getEnum("printRequestBody");
        AidBoolean printResponseBody = attributes.getEnum("printResponseBody");

        if (mode.equals(OperatingMode.Request)) {
            ENABLE_TRACKING_REQUEST = true;
            ENABLE_INBOUND_REQUEST_GLOBAL = false;
        } else {
            ENABLE_INBOUND_REQUEST_GLOBAL = true;
        }
        if (printResponseBody != AidBoolean.Default)
            InboundRequest.PRINT_REQUEST_BODY = printRequestBody == AidBoolean.True;
        if (printResponseBody != AidBoolean.Default)
            InboundRequest.PRINT_RESPONSE_BODY = printResponseBody == AidBoolean.True;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition(RequestMappingInfoCollector.class.getName())) {
            return;
        }
        registry.registerBeanDefinition(RequestMappingInfoCollector.class.getName(), BeanDefinitionBuilder.genericBeanDefinition(RequestMappingInfoCollector.class).getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(@Nullable ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
