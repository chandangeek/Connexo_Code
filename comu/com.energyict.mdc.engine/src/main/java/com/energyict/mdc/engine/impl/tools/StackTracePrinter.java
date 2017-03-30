/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class StackTracePrinter {

    /**
     * Hide utility class constructor
     */
    private StackTracePrinter() {
        super();
    }

    /**
     * Prints the stacktrace for the Throwable.
     *
     * @param thrown The Throwable (can be <code>null</code>).
     * @return The stacktrace or <code>null</code> if there was not Throwable.
     */
    public static String print(Throwable thrown) {
        return print(thrown, null);
    }

    public static String print(Throwable thrown, LogLevel serverLogLevel) {
        if (thrown == null) {
            return null;
        } else {
            return printStackTrace(thrown);
        }
    }

    /**
     * Prints the error message or stackTrace for the Throwable (which is specified by its error message and stackTrace).<br></br>
     * This method is basically a selector and will return the throwableErrorMessage or the throwableStackTrace depending whether or not the serverLogLevel
     * allows printing of the stackTrace.
     *
     * @param throwableErrorMessage The Throwables error message
     * @param throwableStackTrace The Throwables stackTrace
     * @return The error message or stackTrace
     */
    public static String print(String throwableErrorMessage, String throwableStackTrace, LogLevel serverLogLevel) {
        return throwableStackTrace;
    }

    /**
     * Print out the full StackTrace of the {@link Throwable}
     *
     * @param thrown The Throwable for which the full StackTrace should be print out
     * @return A String containing the full print out of the Throwables StackTrace
     */
    private static String printStackTrace(Throwable thrown) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        thrown.printStackTrace(printWriter);
        return writer.toString();
    }

    /**
     * Print out the error message of the {@link Throwable}, including the messages of it's cause
     *
     * @param thrown The Throwable for which the error message should be print out
     * @return A String containing the message print out
     */
    private static String printMessage(Throwable thrown) {
        StringBuilder builder = new StringBuilder();
        appendThrowableMessage(thrown, builder);

        Throwable cause = thrown.getCause();
        if ((cause != null) && (thrown.getMessage() != null) && (cause.getMessage() != null)) {
            if (!thrown.getMessage().contains(cause.getMessage())) {    // Only append the cause details, if these are not already present in the throwable's root message
                builder.append(System.lineSeparator()).append(" ");
                appendThrowableMessage(cause, builder);
            }
        }
        return builder.toString();
    }

    /**
     * Append the error message of a given {@link Throwable} to a given {@link StringBuilder}
     *
     * @param throwable The Throwable for which the error message should be print out
     * @param builder The builder to which the message should be appended
     */
    private static void appendThrowableMessage(Throwable throwable, StringBuilder builder) {
        String s = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();
        builder.append((message != null) ? (s + ": " + message) : s);
    }

    /**
     * Note: leaving this as it was ported from EIServer but for Connexo we do want the full stacktrace for now
     * <p/>
     * Tests if the specified server log level is sufficient to allow a print out of full StackTraces
     *
     * @param serverLogLevel The server LogLevel
     * @return A flag that indicates if StackTrace details should show up in journaling
     */
    private static boolean allowedToPrintStackTrace(LogLevel serverLogLevel) {
        return serverLogLevel == null || serverLogLevel.compareTo(LogLevel.DEBUG) >= 0; // If serverLogLevel not specified, then allow print out anyway
    }

}