package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;

/**
 * Copyrights EnergyICT
 * Date: 4/03/11
 * Time: 8:40
 */
public class ChangeTemporaryKeyMessage extends AbstractChangeKeyMessage {

    public static final String CHANGE_KEYT_OBJECT_ID = "11.0.E";

    public ChangeTemporaryKeyMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(SecurityMessage.CHANGE_TEMPORARY_KEY.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String key = getDeviceMessageAttribute(message, DeviceMessageConstants.temporaryKeyAttributeName).getValue();
        super.doExecuteMessage(message, key);
        return null;
    }

    @Override
    protected void writeKey(String key) throws CTRException {
        getFactory().executeRequest(new CTRObjectID(CHANGE_KEYT_OBJECT_ID), key.getBytes());
    }
}
