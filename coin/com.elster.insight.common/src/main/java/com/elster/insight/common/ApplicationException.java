package com.elster.insight.common;

/**
 * Application is the parent of all application
 * generated Runtime exceptions. It or its subclassses are thrown
 * when an unanticipated system problem stops the application's normal
 * functions
 *
 * @author Karel
 */
public class ApplicationException extends RuntimeException {

    /**
     * Creates new <code>ApplicationException</code> without detail message.
     */
    public ApplicationException() {
    }


    /**
     * Constructs an <code>ApplicationException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ApplicationException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <CODE>ApplicationException</CODE> with the specified cause
     *
     * @param ex underlying cause of the new <CODE>ApplicationException</CODE>
     */
    public ApplicationException(Throwable ex) {
        super(ex);
    }

    /**
     * Constructs a new <CODE>ApplicationException</CODE> with the specified cause
     *
     * @param ex  underlying cause of the new <CODE>ApplicationException</CODE>
     * @param msg the detail message.
     */
    public ApplicationException(String msg, Throwable ex) {
        super(msg, ex);
    }

}


