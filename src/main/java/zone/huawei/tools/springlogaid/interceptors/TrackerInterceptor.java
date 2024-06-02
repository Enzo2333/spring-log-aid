package zone.huawei.tools.springlogaid.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import zone.huawei.tools.springlogaid.context.LTH;
import zone.huawei.tools.springlogaid.model.responses.CachedHttpServletResponse;

import java.io.IOException;


public class TrackerInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(TrackerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CachedHttpServletResponse responseWrapper = new CachedHttpServletResponse(response);
        request.setAttribute("responseWrapper", responseWrapper);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            return;
        }
        try {
            LTH.traceResponse(response);
            LTH.logInfo();
        } catch (IOException e) {
            logger.warn("LOG AID :: Failed to tracing external response in log aid,due to :{},StackTrace:{}", e, e.getStackTrace());
        } finally {
            MDC.clear();
            LTH.clear();
        }
    }
}