package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.*;

/**
 * Place holder class for the MBus device.
 * Contains the standard DSMR 2.3 MBus messages (connect control and setup)
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:04
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

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

    /**
     * add the properties
     *
     * @param properties properties to add
     */
    public void addProperties(final Properties properties) {
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

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder();
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder();
    }

    @Override
    public String getProtocolDescription() {
        return "Placeholder Mbus Slave NTA DSMR 4.0";
    }
}
