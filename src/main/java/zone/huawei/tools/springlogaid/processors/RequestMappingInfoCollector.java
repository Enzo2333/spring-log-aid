package zone.huawei.tools.springlogaid.processors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import zone.huawei.tools.springlogaid.annotations.ExclusionRequest;
import zone.huawei.tools.springlogaid.annotations.TrackingRequest;

import java.util.Map;

import static zone.huawei.tools.springlogaid.constants.AidConstants.ACTIVATED_REQUEST_MAPPING;
import static zone.huawei.tools.springlogaid.constants.AidConstants.ENABLE_TRACKING_REQUEST;

public class RequestMappingInfoCollector {

    private final RequestMappingHandlerMapping handlerMapping;

    public RequestMappingInfoCollector(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @PostConstruct
    public void collectRequestMappingInfo() {
        if (!ENABLE_TRACKING_REQUEST) {
            return;
        }

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod method = entry.getValue();

            Class<?> beanType = method.getBeanType();

            if ((beanType.isAnnotationPresent(TrackingRequest.class) || method.hasMethodAnnotation(TrackingRequest.class)) && !method.hasMethodAnnotation(ExclusionRequest.class)) {
                ACTIVATED_REQUEST_MAPPING.add(mappingInfo);
            }

        }
    }
}