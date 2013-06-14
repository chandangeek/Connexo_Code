package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:59
 * Author: khe
 */
public class DeviceMessageIdentifierById implements MessageIdentifier {

    private final int messageId;

    public DeviceMessageIdentifierById(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        return MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getDeviceMessageFactory().find(messageId);
    }

    @Override
    public String toString() {
        return "messageId = " + messageId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifierById that = (DeviceMessageIdentifierById) obj;
        return this.messageId == that.messageId;
    }

    @Override
    public int hashCode () {
        return messageId;
    }

}
