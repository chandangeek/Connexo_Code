/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

/**
 * Provides functionality to identify the protocol based on an unique protocol description
 * *
 * @author sva
 * @since 30/10/13 - 15:11
 */
public interface DeviceDescriptionSupport {

    /**
     * Getter for the (unique) protocol description, formatted like<br/>
     * <i> &lt;Manufacturer&gt;[\&lt;Old Manufacturer&gt;] &lt;Device Type or Family&gt; &lt;Protocol Base&gt; </i>
     */
    public String getProtocolDescription();

}