package net.stzups.tanks;

import java.sql.Timestamp;

public class Logger {

    static void log(Object msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(byte msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(short msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(int msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(long msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(float msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(double msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(char msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(boolean msg) {
        log(msg, LoggerType.INFO);
    }

    static void log(Object msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(byte msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(short msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(int msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(long msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(float msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(double msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(char msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    static void log(boolean msg, LoggerType loggerType) {
        switch (loggerType) {
            case INFO:
                System.out.println("[" + getTime() + "] " + msg);
                break;
            case WARNING:
                System.out.println("[" + getTime() + "] [WARNING]" + msg);
                break;
            case ERROR:
                System.out.println("[" + getTime() + "] [ERROR] " + msg);
                break;
        }
    }

    private static String getTime() {
        return new Timestamp(System.currentTimeMillis()).toString();
    }
}
