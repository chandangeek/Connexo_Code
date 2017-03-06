/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import java.util.List;

/**
 * Represents usage point's register
 */
public class UsagePointRegisterInfo {
    public long id;
    /**
     * Date of last data from latest meter activation.
     */
    public Long measurementTime;
    public ReadingTypeInfo readingType;
    public List<UsagePointDeviceRegisterInfo> deviceRegisters;
}
