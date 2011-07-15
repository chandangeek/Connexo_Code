package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.SmartNtaProtocol;

import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:00:53
 */
public class MbusDevice extends com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.MbusDevice{

    public MbusDevice(final SmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }


    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";
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
        return null;  //TODO implement proper functionality.
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        return null;  //TODO implement proper functionality.
    }
}
