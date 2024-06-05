package zone.huawei.tools.springlogaid.processors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import zone.huawei.tools.springlogaid.annotations.ExclusionRequest;
import zone.huawei.tools.springlogaid.annotations.TrackingRequest;
import zone.huawei.tools.springlogaid.constants.AidConstants;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TrackingRequestProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (AidConstants.isGlobalMode()) {
            return bean;
        }

        Set<String> uris = new HashSet<>();

        Set<String> controllerUris = new HashSet<>();
        Class<?> objClz = bean.getClass();
        {
            RequestMapping requestMapping = objClz.getAnnotation(RequestMapping.class);
            if (requestMapping != null) {
                controllerUris.addAll(List.of(requestMapping.value()));
            }
        }

        if (objClz.isAnnotationPresent(TrackingRequest.class)) {
            processMethods(uris, controllerUris, objClz.getDeclaredMethods(), true);
        } else {
            processMethods(uris, controllerUris, objClz.getDeclaredMethods(), false);
        }

        if (!uris.isEmpty()) {
            AidConstants.ACTIVE_URIS.addAll(uris);
        }

        return bean;
    }

    private void processMethods(Set<String> uris, Set<String> controllerUris, Method[] methods, boolean addAll) {
        for (Method method : methods) {
            if (addAll) {
                if (method.isAnnotationPresent(ExclusionRequest.class)) {
                    continue;
                }
            } else {
                if (!method.isAnnotationPresent(TrackingRequest.class)) {
                    continue;
                }
            }
            RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (requestMapping != null) {
                for (String value : requestMapping.value()) {
                    if (CollectionUtils.isEmpty(controllerUris)){
                        uris.add(value);
                    }else {
                        for (String cUri : controllerUris) {
                            uris.add(buildUri(cUri, value));
                        }
                    }
                }
            }
        }
    }

    private String buildUri(String cUri, String value) {
        String uriString = Paths.get(cUri, value).toString().replaceAll("\\\\", "/");
        if (!uriString.startsWith("/")){
            uriString = "/".concat(uriString);
        }
        return uriString;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}