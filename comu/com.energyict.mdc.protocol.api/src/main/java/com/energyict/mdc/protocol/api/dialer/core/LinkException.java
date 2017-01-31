/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.dialer.core;

public class LinkException extends Exception {

    /**
     * @param str
     */
    public LinkException(String str) {
        super(str);
    }

    /**
     * @param e
     */
    public LinkException(Throwable e) {
        super(e);
    }

    /**
     *
     */
    public LinkException() {
        super();
    }

}