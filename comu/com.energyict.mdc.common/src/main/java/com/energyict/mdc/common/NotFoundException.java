/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * NotFoundException.java
 *
 * Created on 6 november 2001, 9:53
 */

package com.energyict.mdc.common;

/**
 * Instances of <CODE>NotFoundException</CODE> are thrown when a
 * business object with certain characteristics does not exist
 *
 * @author Karel
 */
public class NotFoundException extends ApplicationException {

    /**
     * Creates new <code>NotFoundException</code> without detail message.
     */
    public NotFoundException() {
    }

    /**
     * Constructs an <code>NotFoundException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotFoundException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>NotFoundException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotFoundException(Throwable cause, String msg) {
        super(msg, cause);
    }

}