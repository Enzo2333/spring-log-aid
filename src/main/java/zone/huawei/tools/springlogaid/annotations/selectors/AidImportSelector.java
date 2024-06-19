package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import zone.huawei.tools.springlogaid.annotations.EnableLogAid;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.enums.OperatingMode;
import zone.huawei.tools.springlogaid.processors.RequestMappingInfoCollector;

import java.util.List;
import java.util.regex.Pattern;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;
import static zone.huawei.tools.springlogaid.constants.AidConstants.OutboundRequest.PASSING_HEADERS;

public class AidImportSelector extends ConfigImportSelector<EnableLogAid> implements BeanDefinitionRegistryPostProcessor {

    @Override
    protected String[] selectImports(AnnotationAttributes attributes) {
        settingConstants(attributes);
        return new String[0];
    }

    private void settingConstants(AnnotationAttributes attributes) {
        AidConstants.ENABLE = true;
        OperatingMode mode = attributes.getEnum("scope");
        String[] filterExcludeUris = attributes.getStringArray("filterExcludeUris");
        String mdcRequestIdKey = attributes.getString("mdcRequestIdKey");
        AidConstants.LogLevel logLevel = attributes.getEnum("logLevel");
        String[] passingHeaders = attributes.getStringArray("passingHeaders");
        if (mode != OperatingMode.DEFAULT) {
            AidConstants.MODE = mode;
            if (mode == OperatingMode.Request) {
                ENABLE_TRACKING_REQUEST = true;
            }
        }
        for (String excludeUri : filterExcludeUris) {
            FILTER_EXCLUDE_URI_PATTERNS.add(Pattern.compile(excludeUri));
        }
        if (!mdcRequestIdKey.isBlank())
            MDC_REQUEST_ID_KEY = mdcRequestIdKey;
        if (logLevel != AidConstants.LogLevel.DEFAULT)
            LOG_LEVER = logLevel;
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
    public void postProcessBeanFactory(@Nullable ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
