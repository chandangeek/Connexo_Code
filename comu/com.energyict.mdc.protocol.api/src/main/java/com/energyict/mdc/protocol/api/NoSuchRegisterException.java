/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/** Java class "NoSuchRegisterException.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.energyict.mdc.protocol.api;

/**
 * This exception is thrown when the specified register is not available in the
 * device.
 *
 * @author Karel
 *         </p>
 */
public class NoSuchRegisterException extends ProtocolException {

    /**
     * <p></p>
     */
    public NoSuchRegisterException() {
    }

    /**
     * <p></p>
     *
     * @param msg <br>
     */
    public NoSuchRegisterException(String msg) {
        super(msg);
    }
}



