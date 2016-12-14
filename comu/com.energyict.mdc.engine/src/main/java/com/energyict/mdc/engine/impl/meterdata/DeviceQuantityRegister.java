package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

/**
 * Copyrights EnergyICT
 * Date: 9/16/14
 * Time: 2:19 PM
 */
public abstract class DeviceQuantityRegister extends DeviceRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the identifier of the Register
     * @param readingTypeMRID
     */
    public DeviceQuantityRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        super(registerIdentifier, readingTypeMRID);
    }

    @Override
    public boolean isTextRegister() {
        return false;
    }
}
