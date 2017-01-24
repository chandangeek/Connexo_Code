package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;


/**
 * Copyrights EnergyICT
 * Date: 4/03/11
 * Time: 8:40
 */
public class ChangeTemporaryKeyMessage extends AbstractChangeKeyMessage {

    public static final String CHANGE_KEYT_OBJECT_ID = "11.0.E";

    public ChangeTemporaryKeyMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_TEMPORARY_KEY);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String key = getDeviceMessageAttribute(message, DeviceMessageConstants.temporaryKeyAttributeName).getDeviceMessageAttributeValue();
        super.doExecuteMessage(key);
        return null;
    }

    @Override
    protected void writeKey(String key) throws CTRException {
        getFactory().executeRequest(new CTRObjectID(CHANGE_KEYT_OBJECT_ID), key.getBytes());
    }

}