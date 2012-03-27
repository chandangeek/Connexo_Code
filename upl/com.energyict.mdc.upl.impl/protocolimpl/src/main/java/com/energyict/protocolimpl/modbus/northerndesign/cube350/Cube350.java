/**
 *
 */
package com.energyict.protocolimpl.modbus.northerndesign.cube350;

import com.energyict.protocolimpl.modbus.eimeter.EIMeter;

/**
 * @deprecated The Northern design Cube 350 is rebranded as an EnergyICT EIMeter.
 *             Please use the {@link com.energyict.protocolimpl.modbus.eimeter.EIMeter} protocol.
 */
@Deprecated
public class Cube350 extends EIMeter {

    @Override
    protected String getDeviceName() {
        return "Cube 350";
    }

}
