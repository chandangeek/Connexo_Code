package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.LoadProfileRegisterMessageBuilder;
import com.energyict.protocol.messaging.LoadProfileRegisterMessaging;
import com.energyict.protocol.messaging.PartialLoadProfileMessageBuilder;
import com.energyict.protocol.messaging.PartialLoadProfileMessaging;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Place holder class for the MBus device.
 * Contains the standard DSMR 2.3 MBus messages (connect control and setup)
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:04
 * Author: khe
 */
public class MBusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    public MBusDevice() {
        super();
    }

    public MBusDevice(final AbstractSmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }


    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    public LoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LoadProfileRegisterMessageBuilder();
    }

    public PartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new PartialLoadProfileMessageBuilder();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }
}
