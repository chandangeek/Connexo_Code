/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

public class DeviceTextRegister extends DeviceRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the identifier of the Register
     */
    public DeviceTextRegister(RegisterIdentifier registerIdentifier) {
        super(registerIdentifier);
    }

    @Override
    public boolean isTextRegister() {
        return true;
    }
}