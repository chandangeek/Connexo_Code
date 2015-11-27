/**
 *
 */
package com.energyict.protocolimpl.modbus.northerndesign.cube350;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.modbus.eimeter.EIMeter;

import javax.inject.Inject;

/**
 * @deprecated The Northern design Cube 350 is rebranded as an EnergyICT EIMeter.
 *             Please use the {@link com.energyict.protocolimpl.modbus.eimeter.EIMeter} protocol.
 */
@Deprecated
public class Cube350 extends EIMeter {

    @Inject
    public Cube350(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected String getDeviceName() {
        return "Cube 350";
    }

}