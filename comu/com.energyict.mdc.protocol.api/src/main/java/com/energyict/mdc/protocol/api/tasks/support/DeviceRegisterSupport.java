/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.util.List;

/**
 * Provides functionality to collect Register Readings from a Device.
 * The collection software uses an universal identification mechanism based on OBIS codes.
 * readRegisters(...) method is used by the collection software at runtime to retrieve the meter's register value for the given List of ObisCodes.
 */
public interface DeviceRegisterSupport {

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper {@link com.energyict.mdc.protocol.api.device.data.ResultType ResultType}
     * <b>and</b> {@link com.energyict.mdc.issues.Issue Issue} should be returned so proper logging of this action can be performed.
     *
     * @param registers The OfflineRtuRegisters for which to request a value
     * @return a <code>List</code> of collected register values
     */
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers);

}