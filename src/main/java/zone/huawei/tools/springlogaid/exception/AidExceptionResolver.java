package zone.huawei.tools.springlogaid.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import zone.huawei.tools.springlogaid.context.LTH;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AidExceptionResolver implements HandlerExceptionResolver {

    private final List<HandlerExceptionResolver> resolvers;

    public AidExceptionResolver(List<HandlerExceptionResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (LTH.isEnabled()){
            LTH.initException(ex);
        }
        Exception e = ex;
        if (e instanceof CompletionException && e.getCause() != null && e.getCause() instanceof Exception) {
            e = (Exception) e.getCause();
        }
        if (e instanceof SubThreadException && e.getCause() != null && e.getCause() instanceof Exception) {
            e = (Exception) e.getCause();
        }
        if (e != ex) {
            for (HandlerExceptionResolver resolver : resolvers) {
                if (resolver != this) {
                    ModelAndView mv = resolver.resolveException(request, response, handler, e);
                    if (mv != null) {
                        return mv;
                    }
                }
            }
        }
        return null;
    }
}