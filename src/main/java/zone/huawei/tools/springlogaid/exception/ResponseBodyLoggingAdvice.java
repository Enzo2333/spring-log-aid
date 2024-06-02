package zone.huawei.tools.springlogaid.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import zone.huawei.tools.springlogaid.context.LTH;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class ResponseBodyLoggingAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Autowired
    public ResponseBodyLoggingAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null){
            try {
                LTH.setInboundRequestResponseBody(new StringBuilder(objectMapper.writeValueAsString(body)));
            } catch (JsonProcessingException e) {
                LTH.setInboundRequestResponseBody(new StringBuilder("Record filed!"));
            }
        }
        return body;
    }
}