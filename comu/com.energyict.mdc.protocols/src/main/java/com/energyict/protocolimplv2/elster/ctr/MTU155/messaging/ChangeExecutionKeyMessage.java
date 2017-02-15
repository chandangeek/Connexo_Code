/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.util.Arrays;

public class ChangeExecutionKeyMessage extends AbstractChangeKeyMessage {

    public static final String CHANGE_KEYC_OBJECT_ID = "11.0.D";

    public ChangeExecutionKeyMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String key = getDeviceMessageAttribute(message, DeviceMessageConstants.executionKeyAttributeName).getDeviceMessageAttributeValue();
        super.doExecuteMessage(key);
        return null;
    }

    @Override
    protected void writeKey(String key) throws CTRException {
        getFactory().executeRequest(new CTRObjectID(CHANGE_KEYC_OBJECT_ID), createRawData(key));
        updateKeyCProperties(key); // if we change our own key, we should update it as wel in the connection
    }

    private void updateKeyCProperties(String key) {
        if (getFactory().getProperties().getSecurityLevel() == 1) {
            getFactory().getProperties().updateKeyC(key);
            CTREncryption encryption = getFactory().getConnection().getCTREncryption();
            if (encryption != null) {
                encryption.update(getFactory().getProperties());
            }
        }
    }

    private byte[] createRawData(String keyC_0) {
        byte[] rawData = keyC_0.getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, getDoNotChangeValue());
        rawData = ProtocolTools.concatByteArrays(rawData, getDoNotChangeValue());
        rawData = ProtocolTools.concatByteArrays(rawData, getDoNotChangeValue());
        rawData = ProtocolTools.concatByteArrays(rawData, getDoNotChangeValue());
        return rawData;
    }

    private byte[] getDoNotChangeValue() {
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte) 0xFF);
        return bytes;
    }
}
