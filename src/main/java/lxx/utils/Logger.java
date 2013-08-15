package lxx.utils;

import java.util.HashMap;
import java.util.Map;

public final class Logger {

    public static final int DEBUG_LEVEL = 0;
    public static final int INFO_LEVEL = 1;
    public static final int WARN_LEVEL = 2;
    public static final int ERR_LEVEL = 3;
    public static final int OFF_LEVEL = 4;

    private static final Map<Class, Logger> loggers = new HashMap<Class, Logger>();

    private static int level = OFF_LEVEL;

    private static long turn;

    private final String className;

    private Logger(Class clazz) {
        this.className = clazz.getSimpleName();
    }

    public static Logger getLogger(Class clazz) {
        if (!loggers.containsKey(clazz)) {
            loggers.put(clazz, new Logger(clazz));
        }

        return loggers.get(clazz);
    }

    public static void setLevel(int level) {
        Logger.level = level;
    }

    public static void setTurn(long turn) {
        Logger.turn = turn;
    }

    public void debug(String message, Object ... args) {
        doLog(DEBUG_LEVEL, message, args);
    }

    public void info(String message, Object ... args) {
        doLog(INFO_LEVEL, message, args);
    }

    public void warn(String message, Object ... args) {
        doLog(WARN_LEVEL, message, args);
    }

    public void err(String message, Object ... args) {
        doLog(ERR_LEVEL, message, args);
    }

    private void doLog(int level, String message, Object ... args) {
        if (level >= Logger.level) {
            final String interpolatedMessage = String.format(message, args);
            final String decoratedMessage = String.format("[%s] %s: %s", turn, className, interpolatedMessage);
            System.out.println(decoratedMessage);
        }
    }

}
