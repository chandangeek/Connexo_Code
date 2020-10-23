/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-15 (16:23)
 */
public final class StackTracePrinter {

    /**
     * Prints the stacktrace for the Throwable.
     *
     * @param thrown The Throwable (can be <code>null</code>).
     * @return The stacktrace or <code>null</code> if there was not Throwable.
     */
    public static String print (Throwable thrown) {
        if (thrown == null) {
            return null;
        }
        else {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            try {
                thrown.printStackTrace(printWriter);
                return writer.toString();
            } catch (Exception ex){
                System.err.println("Error while printing exception stack-trace!");
                System.err.println("\tException raised is: "+ex.getMessage());
                System.err.println("\tOriginal message is: "+thrown.getMessage());
                return thrown.getMessage();
            }
        }
    }

    /**
     * Hide utility class constructor
     */
    private StackTracePrinter () {super();}

}