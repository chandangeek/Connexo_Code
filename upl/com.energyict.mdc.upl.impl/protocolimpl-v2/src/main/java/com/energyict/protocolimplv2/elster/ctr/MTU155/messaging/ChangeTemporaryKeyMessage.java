package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(SecurityMessage.CHANGE_TEMPORARY_KEY.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        String key = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        return super.executeMessage(message, key);
    }

    @Override
    protected void writeKey(String key) throws CTRException {
        getFactory().executeRequest(new CTRObjectID(CHANGE_KEYT_OBJECT_ID), key.getBytes());
    }
}
