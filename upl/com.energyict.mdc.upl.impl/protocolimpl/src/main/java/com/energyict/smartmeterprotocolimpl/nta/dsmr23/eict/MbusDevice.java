package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.cpo.*;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:00:53
 */
public class MbusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

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

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return new ArrayList<String>();
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        return new ArrayList<String>();
    }

    public LoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LoadProfileRegisterMessageBuilder();
    }

    public PartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new PartialLoadProfileMessageBuilder();
    }
}
