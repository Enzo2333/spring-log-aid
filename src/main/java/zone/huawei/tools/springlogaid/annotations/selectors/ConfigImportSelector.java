package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Objects;

public abstract class ConfigImportSelector<A extends Annotation> implements ImportSelector {

    public ConfigImportSelector() {
    }

    @Override
    @NonNull
    public final String[] selectImports(AnnotationMetadata metadata) {
        Class<?> annType = GenericTypeResolver.resolveTypeArgument(this.getClass(), ConfigImportSelector.class);
        Assert.state(annType != null, "Unresolvable type argument for AdviceModeImportSelector");
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annType.getName(), false));
        if (attributes == null) {
            throw new IllegalArgumentException(String.format("@%s is not present on importing class '%s' as expected", annType.getSimpleName(), metadata.getClassName()));
        } else {
            return Objects.requireNonNull(this.selectImports(attributes));
        }
    }

    @Nullable
    protected abstract String[] selectImports(AnnotationAttributes attributes);
}
