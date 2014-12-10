package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.BaseDeviceMessageFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.io.CommunicationException;

import com.energyict.mdc.protocol.api.inbound.MessageIdentifierType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the ID of the DeviceMessage.
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:59
 * Author: khe
 */
public class DeviceMessageIdentifierById implements MessageIdentifier {

    private final long messageId;

    public DeviceMessageIdentifierById(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        return this.getDeviceMessageFactory().findDeviceMessage(this.messageId);
    }

    @Override
    public MessageIdentifierType getMessageIdentifierType() {
        return MessageIdentifierType.DataBaseId;
    }

    @Override
    public List<Object> getIdentifier() {
        return Arrays.asList(messageId);
    }

    @Override
    public String getXmlType() {
        return null;
    }

    @Override
    public void setXmlType(String ignore) {

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
        return (int) messageId;
    }

    private BaseDeviceMessageFactory getDeviceMessageFactory () {
        List<BaseDeviceMessageFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(BaseDeviceMessageFactory.class);
        if (factories.isEmpty()) {
            throw new CommunicationException(MessageSeeds.MISSING_MODULE, BaseDeviceMessageFactory.class);
        }
        else {
            return factories.get(0);
        }
    }
}
