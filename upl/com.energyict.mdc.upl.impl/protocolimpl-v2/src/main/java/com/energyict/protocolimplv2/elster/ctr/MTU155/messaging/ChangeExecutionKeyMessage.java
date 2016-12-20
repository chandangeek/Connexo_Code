package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 4/03/11
 * Time: 8:40
 */
public class ChangeExecutionKeyMessage extends AbstractChangeKeyMessage {

    public static final String CHANGE_KEYC_OBJECT_ID = "11.0.D";

    public ChangeExecutionKeyMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(SecurityMessage.CHANGE_EXECUTION_KEY.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String key = getDeviceMessageAttribute(message, DeviceMessageConstants.executionKeyAttributeName).getValue();
        super.doExecuteMessage(message, key);
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
