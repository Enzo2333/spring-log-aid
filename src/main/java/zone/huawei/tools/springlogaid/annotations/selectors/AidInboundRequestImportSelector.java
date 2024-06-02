package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.core.annotation.AnnotationAttributes;
import zone.huawei.tools.springlogaid.annotations.EnableLogInboundRequest;
import zone.huawei.tools.springlogaid.enums.AidBoolean;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;

public class AidInboundRequestImportSelector extends ConfigImportSelector<EnableLogInboundRequest> {

    @Override
    protected String[] selectImports(AnnotationAttributes attributes) {
        settingConstants(attributes);
        return new String[0];
    }

    private void settingConstants(AnnotationAttributes attributes){
        ENABLE_FILTER_GLOBAL = true;
        AidBoolean printRequestBody = attributes.getEnum("printRequestBody");
        AidBoolean printResponseBody = attributes.getEnum("printResponseBody");

        if (printResponseBody != AidBoolean.Default)
            InboundRequest.PRINT_REQUEST_BODY = printRequestBody == AidBoolean.True;
        if (printResponseBody != AidBoolean.Default)
            InboundRequest.PRINT_RESPONSE_BODY = printResponseBody == AidBoolean.True;
    }
}
