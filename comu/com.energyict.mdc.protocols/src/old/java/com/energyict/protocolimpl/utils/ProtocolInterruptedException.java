package com.energyict.protocolimpl.utils;

import com.energyict.mdc.common.ApplicationException;

/**
 * Unchecked exception for interrupt handling.
 * This one should be catched at the highest protocol level
 *
 * Copyrights EnergyICT
 * Date: 30-apr-2010
 * Time: 10:37:39
 */
public class ProtocolInterruptedException extends ApplicationException{

    /**
     * Creates new <code>ProtocolInterruptedException</code> without detail message.
     */
    public ProtocolInterruptedException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Constructs an <code>ProtocolInterruptedException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ProtocolInterruptedException(String msg) {
        super(msg);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Constructs a new <CODE>ProtocolInterruptedException</CODE> with the specified cause
     *
     * @param ex underlying cause of the new <CODE>ApplicationException</CODE>
     */
    public ProtocolInterruptedException(Throwable ex) {
        super(ex);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Constructs a new <CODE>ProtocolInterruptedException</CODE> with the specified cause
     *
     * @param ex  underlying cause of the new <CODE>ProtocolInterruptedException</CODE>
     * @param msg the detail message.
     */
    public ProtocolInterruptedException(String msg, Throwable ex) {
        super(msg, ex);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
