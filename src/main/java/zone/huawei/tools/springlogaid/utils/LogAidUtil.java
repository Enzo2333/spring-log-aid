package zone.huawei.tools.springlogaid.utils;

import zone.huawei.tools.springlogaid.constants.AidConstants;

public class LogAidUtil {

    public static String filterStackTrace(Throwable throwable) {
        String basePackage = AidConstants.BASE_PACKAGE;
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        sb.append(throwable).append(System.lineSeparator());
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().startsWith(basePackage)) {
                sb.append("\tat ").append(stackTraceElement).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public static String fullStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        sb.append(throwable).append(System.lineSeparator());
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("\tat ").append(stackTraceElement).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
