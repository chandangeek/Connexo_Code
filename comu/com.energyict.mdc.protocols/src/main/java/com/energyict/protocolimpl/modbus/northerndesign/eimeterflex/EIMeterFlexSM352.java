package com.energyict.protocolimpl.modbus.northerndesign.eimeterflex;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule;

import javax.inject.Inject;
import java.time.Clock;

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
        return "Northern Design MultiCube Modbus";
    }

    @Inject
    public EIMeterFlexSM352(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService, clock);
    }
}