package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import zone.huawei.tools.springlogaid.config.BeanDependencyConfigurer;
import zone.huawei.tools.springlogaid.config.JacksonConfig;
import zone.huawei.tools.springlogaid.config.LogAidConfigProps;
import zone.huawei.tools.springlogaid.config.WebMvcConfig;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.exception.AidExceptionResolver;
import zone.huawei.tools.springlogaid.exception.ResponseBodyLoggingAdvice;
import zone.huawei.tools.springlogaid.filters.LogAidFilter;
import zone.huawei.tools.springlogaid.processors.TrackingRequestProcessor;

import java.util.ArrayList;
import java.util.List;

public class AidBaseImportSelector implements ImportSelector {


    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> result = new ArrayList<>(8);
        result.add(JacksonConfig.class.getName());
        result.add(LogAidConfigProps.class.getName());
        result.add(AidConstants.class.getName());
        result.add(BeanDependencyConfigurer.class.getName());
        result.add(WebMvcConfig.class.getName());
        result.add(ResponseBodyLoggingAdvice.class.getName());
        result.add(LogAidFilter.class.getName());
        result.add(AidExceptionResolver.class.getName());
        result.add(TrackingRequestProcessor.class.getName());
        return StringUtils.toStringArray(result);
    }
}
