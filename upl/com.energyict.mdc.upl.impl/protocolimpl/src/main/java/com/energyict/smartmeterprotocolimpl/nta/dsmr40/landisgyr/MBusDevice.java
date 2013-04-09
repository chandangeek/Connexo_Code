package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 16:30
 */
public class MBusDevice extends AbstractNtaMbusDevice {

    @Override
    public MessageProtocol getMessageProtocol() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();    }
}
