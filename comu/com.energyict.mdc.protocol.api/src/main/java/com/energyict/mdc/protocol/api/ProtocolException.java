/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/** Java class "ProtocolException.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.energyict.mdc.protocol.api;

import java.io.IOException;

/**
 * This exception is the common superclass of all com.energyict.protocol Exceptions
 *
 * @author Karel
 *         </p>
 */
public class ProtocolException extends IOException {

    /**
     * <p></p>
     */
    public ProtocolException() {
    }

    /**
     * <p></p>
     *
     * @param msg <br>
     */
    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(Exception e) {
        super(e);
    }

    public ProtocolException(Exception e, String msg) {
        super(msg, e);
    }
}



