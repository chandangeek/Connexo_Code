package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdw.core.Pluggable;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.MbusDeviceMessaging;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 15:36:39
 */
public class MbusDevice extends SlaveMeter implements Pluggable{

    @Override
    public MessageProtocol getMessageProtocol() {
        return new MbusDeviceMessaging();
    }

    public MbusDevice(){
        super();
    }

    public MbusDevice(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
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
        //nothing to do
    }

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        return Collections.emptyList();
    }
}
