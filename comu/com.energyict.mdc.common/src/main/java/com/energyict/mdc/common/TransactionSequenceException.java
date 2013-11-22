/*
 * TransactionSequenceException.java
 *
 * Created on 22 september 2002, 21:17
 */

package com.energyict.mdc.common;

import com.energyict.mdc.common.ApplicationException;

/**
 * @author Karel
 */
public class TransactionSequenceException extends ApplicationException {

    /**
     * Creates a new instance of <code>TransactionSequenceException</code> without detail message.
     */
    public TransactionSequenceException() {
    }


    /**
     * Constructs an instance of <code>TransactionSequenceException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public TransactionSequenceException(String msg) {
        super(msg);
    }
}
