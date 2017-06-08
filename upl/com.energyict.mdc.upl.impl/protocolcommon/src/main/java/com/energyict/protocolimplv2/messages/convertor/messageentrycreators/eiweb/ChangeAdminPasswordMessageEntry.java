package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class ChangeAdminPasswordMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_ADMINOLD_TAG = "AdminOld";
    private static final String LEGACY_ADMINNEW_TAG = "AdminNew";

    /**
     * Default constructor
     */
    public ChangeAdminPasswordMessageEntry() {
    }

    @Override             //E.G. <AdminOld>sdfsdf</AdminOld><AdminNew>sdfsdf2</AdminNew>
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String[] keys;
        String adminActualPassiveValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.AdminPassword).getValue();
        ByteArrayInputStream in = new ByteArrayInputStream(DatatypeConverter.parseHexBinary(adminActualPassiveValue));
        try {
            keys = (String[]) new ObjectInputStream(in).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw DataParseException.generalParseException(e);
        }

        MessageTag oldTag = new MessageTag(LEGACY_ADMINOLD_TAG);
        oldTag.add(new MessageValue(keys[0]));

        MessageTag newTag = new MessageTag(LEGACY_ADMINNEW_TAG);
        newTag.add(new MessageValue(keys[1]));

        return MessageEntry
                .fromContent(SimpleTagWriter.writeTag(oldTag) + SimpleTagWriter.writeTag(newTag))
                .andMessage(offlineDeviceMessage)
                .finish();
    }
}