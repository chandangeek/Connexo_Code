package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.cpo.*;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:00:53
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    public MbusDevice() {
        super();
    }

    public MbusDevice(final AbstractSmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT Mbus Slave NTA DSMR 2.3";
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
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /**
     * add the properties
     *
     * @param properties properties to add
     */
    public void addProperties(final Properties properties) {
        //TODO implement proper functionality.
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder();
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder();
    }
}
