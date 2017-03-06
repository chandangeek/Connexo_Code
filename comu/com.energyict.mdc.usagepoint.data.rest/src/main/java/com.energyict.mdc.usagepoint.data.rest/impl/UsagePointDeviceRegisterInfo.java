/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

/**
 * Represents device register related to {@link UsagePointRegisterInfo}
 */
public class UsagePointDeviceRegisterInfo {
    public Long from;
    public Long until;
    public String device;
    public IdWithNameInfo channel;
}
