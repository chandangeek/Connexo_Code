package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.EMeterMessaging;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 15:36:27
 */
public class EMeter extends SlaveMeter implements MessageProtocol {

    public MessageProtocol getMessageProtocol() {
        return new EMeterMessaging();
    }

    public EMeter(){
        super();
    }

    protected EMeter(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS E-meter slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public void addProperties(final Properties properties) {
        // currently nothing to do
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
