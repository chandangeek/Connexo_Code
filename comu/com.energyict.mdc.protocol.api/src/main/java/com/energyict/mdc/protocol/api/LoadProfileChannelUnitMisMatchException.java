/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

/**
 * Exception can be thrown when the configuration of the channelUnits of a loadProfile does not match with the configuration in the meter
 */
public class LoadProfileChannelUnitMisMatchException extends LoadProfileConfigurationException {

    /**
     * Constructs an <code>ProtocolException</code> with <code>null</code>
     * as its error detail message.
     */
    public LoadProfileChannelUnitMisMatchException() {
        super();
    }

    /**
     * Constructs an <code>ProtocolException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the <code>{@link Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param msg the detail message.
     */
    public LoadProfileChannelUnitMisMatchException(String msg) {
        super(msg);
    }
}
