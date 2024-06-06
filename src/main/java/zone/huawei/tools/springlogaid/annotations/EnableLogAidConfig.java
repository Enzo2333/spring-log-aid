package zone.huawei.tools.springlogaid.annotations;

import org.springframework.context.annotation.Import;
import zone.huawei.tools.springlogaid.annotations.registrars.ReadAidConfigBeanRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({ReadAidConfigBeanRegistrar.class})
public @interface EnableLogAidConfig {
}
