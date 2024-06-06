package zone.huawei.tools.springlogaid.annotations.selectors;

import org.springframework.core.annotation.AnnotationAttributes;
import zone.huawei.tools.springlogaid.annotations.EnableLogAid;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.enums.OperatingMode;
import java.util.List;
import java.util.regex.Pattern;

import static zone.huawei.tools.springlogaid.constants.AidConstants.*;
import static zone.huawei.tools.springlogaid.constants.AidConstants.OutboundRequest.PASSING_HEADERS;

public class AidImportSelector extends ConfigImportSelector<EnableLogAid> {

    @Override
    protected String[] selectImports(AnnotationAttributes attributes) {
        settingConstants(attributes);
        return new String[0];
    }

    private void settingConstants(AnnotationAttributes attributes){
        AidConstants.ENABLE = true;
        OperatingMode mode = attributes.getEnum("scope");
        String[] filterExcludeUris = attributes.getStringArray("filterExcludeUris");
        String mdcRequestIdKey = attributes.getString("mdcRequestIdKey");
        AidConstants.LogLevel logLevel = attributes.getEnum("logLevel");
        String[] passingHeaders = attributes.getStringArray("passingHeaders");
        if (mode != OperatingMode.DEFAULT)
            AidConstants.MODE = mode;
        for (String excludeUri : filterExcludeUris) {
            FILTER_EXCLUDE_URI_PATTERNS.add(Pattern.compile(excludeUri));
        }
        if (!mdcRequestIdKey.isBlank())
            MDC_REQUEST_ID_KEY = mdcRequestIdKey;
        if (logLevel!= AidConstants.LogLevel.DEFAULT)
            LOG_LEVER = logLevel;
        if (passingHeaders.length>0)
            PASSING_HEADERS.addAll(List.of(passingHeaders));
    }
}
