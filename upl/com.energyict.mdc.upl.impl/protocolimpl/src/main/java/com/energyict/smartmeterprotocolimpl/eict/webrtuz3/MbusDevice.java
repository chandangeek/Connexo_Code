package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.MbusDeviceMessaging;

import java.io.IOException;
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

    protected MbusDevice(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS Mbus Slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-01-14 10:24:29 +0100 (Wed, 14 Jan 2015) $";
    }

    @Override
    public void addProperties(final Properties properties) {
        //nothing to do
    }

    @Override
    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMeterProtocol().queryMessage(messageEntry);
    }

    @Override
    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    @Override
    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

}