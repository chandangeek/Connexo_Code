package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.MbusDeviceMessaging;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 15:36:39
 */
public class MbusDevice extends SlaveMeter implements MessageProtocol{

    public MessageProtocol getMessageProtocol() {
        return new MbusDeviceMessaging();
    }

    public MbusDevice(){
        super();
    }

    public MbusDevice(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 Mbus Slave";
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

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMeterProtocol().queryMessage(messageEntry);
    }

    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    public List getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }
}
