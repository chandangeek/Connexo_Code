package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

/**
 * Provides information of a {@link com.energyict.mdc.protocol.api.DeviceMessageFile}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-23 (11:19)
 */
public class DeviceMessageFileInfo extends LinkInfo<Long> {
    public String name;
    public LinkInfo deviceType;
}