package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.issue.Issue;

import java.util.List;

/**
 * Provides functionality to collect Register Readings from a Device.
 * The collection software uses an universal identification mechanism based on OBIS codes.
 * readRegisters(...) method is used by the collection software at runtime to retrieve the meter's register value for the given List of ObisCodes.
 */
public interface DeviceRegisterSupport {

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper {@link ResultType resultType}
     * <b>and</b> {@link Issue issue} should be returned so proper logging of this action can be performed.
     *
     * @param registers The OfflineRtuRegisters for which to request a value
     * @return a <code>List</code> of collected register values
     */
    List<CollectedRegister> readRegisters(List<OfflineRegister> registers);

}