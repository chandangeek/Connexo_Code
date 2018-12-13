package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public abstract class AbstractChangeKeyMessage extends AbstractMTU155Message {

    public AbstractChangeKeyMessage(Messaging messaging, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(messaging, collectedDataFactory, issueFactory);
    }

    protected abstract void writeKey(String formattedKey) throws CTRException;

    protected void doExecuteMessage(OfflineDeviceMessage message, String key) throws CTRException {
        String formattedKey = validateAndFormatKey(key);
        writeKey(formattedKey);
    }

    private String validateAndFormatKey(String key) throws CTRException {
        String fullLengthKey = null;
        if (key.length() == 16) {
            fullLengthKey = ProtocolTools.getHexStringFromBytes(key.getBytes(), "");
        } else if (key.length() == 32) {
            fullLengthKey = key;
        } else {
            String msg = "Invalid key [" + key + "]. Key should have a length of 8 or 16, but given key had a length of [" + key.length() + "]";
            throw new CTRException(msg);
        }

        fullLengthKey = fullLengthKey.toUpperCase();
        try {
            byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString(fullLengthKey, "");
        } catch (Exception e) {
            String msg = "Invalid key [" + key + "]. Cannot convert [" + fullLengthKey + "] to bytes. " + e.getMessage();
            throw new CTRException(msg);
        }

        if ("FFFFFFFFFFFFFFFF".equalsIgnoreCase(fullLengthKey) || "0000000000000000".equalsIgnoreCase(fullLengthKey)) {
            String msg = "Unable to use [" + fullLengthKey + "] as key. This value is reserved.";
            throw new CTRException(msg);
        }

        return fullLengthKey;
    }
}
