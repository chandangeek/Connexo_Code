package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.cpo.TypedProperties;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Place holder class for the MBus device.
 * Contains the standard DSMR 2.3 MBus messages (connect control and setup)
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:04
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice {

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
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
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

}
