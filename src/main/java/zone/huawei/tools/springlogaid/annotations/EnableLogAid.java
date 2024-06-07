package zone.huawei.tools.springlogaid.annotations;


import org.springframework.context.annotation.Import;
import zone.huawei.tools.springlogaid.annotations.registrars.AidBaseBeanRegistrar;
import zone.huawei.tools.springlogaid.annotations.selectors.AidImportSelector;
import zone.huawei.tools.springlogaid.enums.OperatingMode;

import zone.huawei.tools.springlogaid.constants.AidConstants.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({AidBaseBeanRegistrar.class, AidImportSelector.class})
public @interface EnableLogAid {

    OperatingMode scope() default OperatingMode.DEFAULT;

    String[] filterExcludeUris() default {};

    String mdcRequestIdKey() default "";

    LogLevel logLevel() default LogLevel.DEFAULT;

    String[] passingHeaders() default {};
}
