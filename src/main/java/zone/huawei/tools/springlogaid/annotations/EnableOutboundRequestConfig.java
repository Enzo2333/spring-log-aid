package zone.huawei.tools.springlogaid.annotations;

import org.springframework.context.annotation.Import;
import zone.huawei.tools.springlogaid.annotations.registrars.OutboundRequestConfigBeanRegistrar;
import zone.huawei.tools.springlogaid.annotations.selectors.OutboundRequestImportSelector;
import zone.huawei.tools.springlogaid.enums.AidBoolean;
import zone.huawei.tools.springlogaid.enums.OperatingMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({OutboundRequestConfigBeanRegistrar.class, OutboundRequestImportSelector.class})
public @interface EnableOutboundRequestConfig {

    OperatingMode scope() default OperatingMode.DEFAULT;

    String[] passingHeaders() default {};

    AidBoolean printRequestBody() default AidBoolean.Default;

    AidBoolean printResponseBody() default AidBoolean.Default;
}
