/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/** Java class "UnsupportedException.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package com.energyict.mdc.protocol.api;

/**
 * <p> This exception is thrown when a meter protocol specific parameter has an
 * invalid value </p>
 *
 * @author Karel
 *         </p>
 */
public class InvalidPropertyException extends ProtocolException {

    /**
     * <p></p>
     */
    public InvalidPropertyException() {
    }

    /**
     * <p></p>
     *
     * @param msg <br>
     */
    public InvalidPropertyException(String msg) {
        super(msg);
    }
}


