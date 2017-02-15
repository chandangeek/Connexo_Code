/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

public abstract class DeviceQuantityRegister extends DeviceRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the identifier of the Register
     * @param readingType
     */
    public DeviceQuantityRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        super(registerIdentifier, readingType);
    }


    @Override
    public boolean isTextRegister() {
        return false;
    }
}
