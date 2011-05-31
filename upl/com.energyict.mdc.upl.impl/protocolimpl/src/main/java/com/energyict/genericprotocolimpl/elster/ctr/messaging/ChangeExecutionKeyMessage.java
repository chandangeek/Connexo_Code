package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 4/03/11
 * Time: 8:40
 */
public class ChangeExecutionKeyMessage extends AbstractChangeKeyMessage {

    public static final String KEY_NAME = "Execution_0_";
    public static final String CHANGE_KEYC_OBJECT_ID = "11.0.D";

    public ChangeExecutionKeyMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor, KEY_NAME);
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

    public static MessageSpec getMessageSpec(boolean advanced) {
        return createMessageSpec(advanced, KEY_NAME);
    }

}
