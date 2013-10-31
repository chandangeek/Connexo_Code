package com.energyict.protocolimpl.modbus.northerndesign.eimeterflex;

import com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule;

/**
 * Protocol class for reading out an EIMeter flex SM352 module.
 * <p/>
 * Deprecated: use com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule instead
 *
 * @author alex
 */
@Deprecated
public final class EIMeterFlexSM352 extends EIMeterFlexSlaveModule {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT EIFlex SM352";
    }
}