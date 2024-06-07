package zone.huawei.tools.springlogaid.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import zone.huawei.tools.springlogaid.constants.AidConstants;
import zone.huawei.tools.springlogaid.context.LTH;
import zone.huawei.tools.springlogaid.interfaces.ConcurrentSupplier;
import zone.huawei.tools.springlogaid.model.concurrent.ThreadInfo;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static zone.huawei.tools.springlogaid.context.LTH.isEnabled;

@Slf4j
public class AidCompletableFuture {

    public static <U> CompletableFuture<U> supplyAsync(ConcurrentSupplier<U> concurrentSupplier) {
        ThreadInfo threadInfo = new ThreadInfo();
        return CompletableFuture.supplyAsync(() -> futureMethod(concurrentSupplier, threadInfo));
    }

    public static <U> CompletableFuture<U> supplyAsync(ConcurrentSupplier<U> concurrentSupplier, Executor executor) {
        ThreadInfo threadInfo = new ThreadInfo();
        return CompletableFuture.supplyAsync(() -> futureMethod(concurrentSupplier, threadInfo), executor);
    }

    private static <U> U futureMethod(ConcurrentSupplier<U> concurrentSupplier, ThreadInfo threadInfo) {
        setThreadInfo(threadInfo);
        try {
            return concurrentSupplier.get();
        } catch (Exception e) {
            LTH.setErrorThreadInfo(threadInfo, e);
            throw e;
        } finally {
            RequestContextHolder.resetRequestAttributes();
            LTH.logInfoAndClear();
            MDC.clear();
        }
    }

    private static void setThreadInfo(ThreadInfo threadInfo) {
        try {
            MDC.setContextMap(threadInfo.getMdcMessage());
            RequestContextHolder.setRequestAttributes(threadInfo.getRequestAttributes());
            String threadName = Thread.currentThread().getName();
            String requestId = threadInfo.getCurrentRequestId() + "-T-" + threadName;
            MDC.put(AidConstants.MDC_REQUEST_ID_KEY, requestId);
            if (threadInfo.isRequestLogEnabled()) {
                LTH.enable();
            }
            if (threadInfo.isOutboundRequestEnabled()) {
                LTH.enableOutboundRequest();
            }
            if (!isEnabled()) {
                return;
            }
            threadInfo.getChildThreadLogIds().add(requestId);
            LTH.addLogKey("ThreadName", threadName);
        } catch (IllegalArgumentException e) {
            log.info("LOG AID :: setThreadInfo creation failed, reason: {},StackTrace:{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }
}
