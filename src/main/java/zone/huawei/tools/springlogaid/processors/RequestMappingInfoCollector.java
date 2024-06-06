package zone.huawei.tools.springlogaid.processors;

import jakarta.annotation.PostConstruct;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import zone.huawei.tools.springlogaid.annotations.ExclusionRequest;
import zone.huawei.tools.springlogaid.annotations.TrackingRequest;

import java.util.Map;

import static zone.huawei.tools.springlogaid.constants.AidConstants.ACTIVATED_REQUEST_MAPPING;

//@Component
public class RequestMappingInfoCollector {

    private final RequestMappingHandlerMapping handlerMapping;

    public RequestMappingInfoCollector(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @PostConstruct
    public void collectRequestMappingInfo() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();



        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod method = entry.getValue();
            
            Class<?> beanType = method.getBeanType();
            
            if ((beanType.isAnnotationPresent(TrackingRequest.class) || method.hasMethodAnnotation(TrackingRequest.class)) && !method.hasMethodAnnotation(ExclusionRequest.class) ){
                ACTIVATED_REQUEST_MAPPING.add(mappingInfo);
            }
            
        }
    }
}