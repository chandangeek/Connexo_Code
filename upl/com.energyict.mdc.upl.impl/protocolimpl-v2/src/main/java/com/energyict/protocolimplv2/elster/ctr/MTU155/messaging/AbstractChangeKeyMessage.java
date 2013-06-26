package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public abstract class AbstractChangeKeyMessage extends AbstractMTU155Message {

    public AbstractChangeKeyMessage(Messaging messaging) {
        super(messaging);
    }

    protected abstract void writeKey(String formattedKey) throws CTRException;

    public CollectedMessage executeMessage(OfflineDeviceMessage message, String key) {
        CollectedMessage collectedMessage = createCollectedMessage(message);

        try {
            String formattedKey = validateAndFormatKey(collectedMessage, key);
            writeKey(formattedKey);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private String validateAndFormatKey(CollectedMessage collectedMessage, String key) throws CTRException {
        String fullLengthKey = null;
        if (key.length() == 16) {
            fullLengthKey = ProtocolTools.getHexStringFromBytes(key.getBytes(), "");
        } else if (key.length() == 32) {
            fullLengthKey = key;
        } else {
            String msg = "Invalid key [" + key + "]. Key should have a length of 8 or 16, but given key had a length of [" + key.length() + "]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }

        fullLengthKey = fullLengthKey.toUpperCase();
        try {
            byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString(fullLengthKey, "");
        } catch (Exception e) {
            String msg = "Invalid key [" + key + "]. Cannot convert [" + fullLengthKey + "] to bytes. " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }

        if (fullLengthKey.equalsIgnoreCase("FFFFFFFFFFFFFFFF") || fullLengthKey.equalsIgnoreCase("0000000000000000")) {
            String msg = "Unable to use [" + fullLengthKey + "] as key. This value is reserved.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }

        return fullLengthKey;
    }
}
