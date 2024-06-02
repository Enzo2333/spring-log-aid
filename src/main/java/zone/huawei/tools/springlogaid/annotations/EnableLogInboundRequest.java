package zone.huawei.tools.springlogaid.annotations;

import org.springframework.context.annotation.Import;
import zone.huawei.tools.springlogaid.annotations.selectors.AidBaseImportSelector;
import zone.huawei.tools.springlogaid.annotations.selectors.AidInboundRequestImportSelector;
import zone.huawei.tools.springlogaid.enums.AidBoolean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({AidBaseImportSelector.class, AidInboundRequestImportSelector.class})
public @interface EnableLogInboundRequest {

    AidBoolean printRequestBody() default AidBoolean.Default;

    AidBoolean printResponseBody() default AidBoolean.Default;
}
