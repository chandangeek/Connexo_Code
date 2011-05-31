package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public abstract class AbstractChangeKeyMessage extends AbstractMTU155Message {

    private String keyName;

    public AbstractChangeKeyMessage(MTU155MessageExecutor messageExecutor, String keyName) {
        super(messageExecutor);
        this.keyName = keyName;
    }

    protected abstract void writeKey(String keyAttr) throws CTRException;

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(getMessageTag(keyName), messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String keyAttr = MessagingTools.getContentOfAttribute(messageEntry, getKeyAttribute(keyName));
        String formattedKey = validateAndFormatKey(keyAttr);
        try {
            writeKey(formattedKey);
        } catch (CTRException e) {
            throw new BusinessException("Unable to write " + keyName + " key. " + e.getMessage(), e);
        }
    }

    private String validateAndFormatKey(String key) throws BusinessException {
        if (key == null) {
            throw new BusinessException("Key cannot be null.");
        }

        key = key.trim();
        String fullLengthKey = null;
        if (key.length() == 8) {
            fullLengthKey = ProtocolTools.getHexStringFromBytes(key.getBytes());
        } else if (key.length() == 16) {
            fullLengthKey = key;
        } else {
            throw new BusinessException("Invalid key [" + key + "]. Key should have a length of 8 or 16, but given key had a length of [" + key.length() + "]");
        }

        fullLengthKey = fullLengthKey.toUpperCase();
        try {
            byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString(fullLengthKey, "");
        } catch (Exception e) {
            throw new BusinessException("Invalid key [" + key + "]. Cannot convert [" + fullLengthKey + "] to bytes. " + e.getMessage());
        }

        if (fullLengthKey.equalsIgnoreCase("FFFFFFFFFFFFFFFF") || fullLengthKey.equalsIgnoreCase("0000000000000000")) {
            throw new BusinessException("Unable to use [" + fullLengthKey + "] as key. This value is reserved.");
        }

        return fullLengthKey;

    }

    private static String getMessageTag(String keyName) {
        return "Change" + keyName + "Key";
    }

    private static String getKeyAttribute(String keyName) {
        return keyName + "Key";
    }

    protected static MessageSpec createMessageSpec(boolean advanced, String keyName) {
        MessageSpec msgSpec = new MessageSpec("Change the " + keyName + " key", advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(getMessageTag(keyName));
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(getKeyAttribute(keyName), true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
