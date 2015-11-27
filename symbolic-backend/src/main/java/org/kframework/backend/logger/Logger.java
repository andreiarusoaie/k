package org.kframework.backend.logger;

import java.time.LocalDateTime;

/**
 * Created by andrei on 8/6/15.
 */
public class Logger {

    private static StringBuilder stringBuilder = new StringBuilder();

    public static void putLine(String data) {
        stringBuilder.append("[" + getTimeStamp() + "]: " + data + "\n\n");
    }

    public static void putSimpleLine(String data) {
        stringBuilder.append(data + "\n");
    }

    public static void putLines(String... data) {
        String entryTimestamp = "[" + getTimeStamp() + "]: ";
        stringBuilder.append(entryTimestamp);
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                stringBuilder.append(data[i] + "\n");
            } else {
                stringBuilder.append(entryTimestamp.replaceAll(".", " ") + data[i] + "\n");
            }
        }
        stringBuilder.append("\n");
    }

    public static void failed(String... data) {
        stringBuilder.append("***** Failure ******\n");
        String entryTimestamp = "[" + getTimeStamp() + "]: ";
        stringBuilder.append(entryTimestamp);
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                stringBuilder.append(data[i] + "\n");
            } else {
                stringBuilder.append(entryTimestamp.replaceAll(".", " ") + data[i] + "\n");
            }
        }
        stringBuilder.append("*****  End  ******\n");
        stringBuilder.append("\n");
    }

    public static StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    private static String getTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalTime().toString();
    }
}
