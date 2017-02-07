/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Adds behavior to {@link ChannelSpec} that is reserved for server side component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-14 (16:01)
 */
interface ServerChannelSpec extends ChannelSpec {
    void validateDelete();

    /**
     * Notifies this ChannelSpec that the configuration
     * it is part of is currently being deleted.
     */
    void configurationBeingDeleted();
}