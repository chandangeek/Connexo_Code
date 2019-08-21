/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.energyict.mdc.common.protocol.DeviceMessageFile;

/**
 * Provides information of a {@link DeviceMessageFile}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-23 (11:19)
 */
public class DeviceMessageFileInfo extends LinkInfo<Long> {
    public String name;
    public LinkInfo deviceType;
}