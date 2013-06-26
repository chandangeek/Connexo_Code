package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(SecurityMessage.CHANGE_EXECUTION_KEY.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        String key = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        return super.executeMessage(message, key);
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
