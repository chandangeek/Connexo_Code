package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import java.util.TimeZone;

/**
 * Creates XML: <TimedDisconnect Date (dd/mm/yyyy hh:mm)="10/10/2010 10:50" TimeZone="Europe/Paris" ></TimedDisconnect>
 * or
 * Creates XML: <TimedReconnect Date (dd/mm/yyyy hh:mm)="10/10/2010 10:50" TimeZone="Europe/Paris" ></TimedReconnect>
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 10:23
 * Author: khe
 */
public class ContactorControlWithActivationDateAndTimezoneMessageEntry implements MessageEntryCreator {

    private final String tag;

    public ContactorControlWithActivationDateAndTimezoneMessageEntry(String tag) {
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageAttribute("Date (dd/mm/yyyy hh:mm)", offlineDeviceMessage.getDeviceMessageAttributes().get(0).getValue()));
        messageTag.add(new MessageAttribute("TimeZone", TimeZone.getDefault().getID()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(SimpleTagWriter.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
