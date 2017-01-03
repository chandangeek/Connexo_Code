package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleSecuritySetupObject;
import com.elster.protocolimpl.dlms.SecurityData;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 30.09.11
 * Time: 10:13
 */
@SuppressWarnings({"unused"})
public class ChangeKeysMessage extends AbstractDlmsMessage {

    /**
     * RtuMessage tags for the key change message
     */
    public static final String MESSAGE_TAG = "ChangeKeys";
    public static final String MESSAGE_DESCRIPTION = "Change keys in device";
    public static final String ATTR_CLIENT_ID = "ClientId";
    public static final String ATTR_AUTHENTICATION_KEY = "NewAuthenticationKey";
    public static final String ATTR_ENCRYPTION_KEY = "NewEncryptionKey";
    public static final String ATTR_WRAPPER_KEY = "WrapperKey";

    public ChangeKeysMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message  to the meter.
     *
     * @param messageEntry: the message containing the new keys
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException {
        String clientId = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CLIENT_ID);
        String authenticationKey = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTHENTICATION_KEY);
        String encryptionKey = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ENCRYPTION_KEY);
        String wrapperKey = MessagingTools.getContentOfAttribute(messageEntry, ATTR_WRAPPER_KEY);
        validateMessageData(clientId, authenticationKey, encryptionKey, wrapperKey);

        try {
            write(clientId, authenticationKey, encryptionKey, wrapperKey);
        } catch (IOException e) {
            throw new IOException("Unable to change keys in device: " + e.getMessage(), e);
        }
    }

    private void write(String clientId, String authenticationKey, String encryptionKey, String wrapperKey) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        int clId = Integer.parseInt(clientId);

        final SimpleSecuritySetupObject securitySetup =
                  objectManager.getSimpleCosemObject(Ek280Defs.SECURITY_SETUP_OBJECT_30.derive(4, clId),
                          SimpleSecuritySetupObject.class);

        byte[] authKeyData = CodingUtils.string2ByteArray(authenticationKey);
        byte[] encrKeyData = CodingUtils.string2ByteArray(encryptionKey);
        byte[] wrapKeyData = CodingUtils.string2ByteArray(wrapperKey);

        securitySetup.wrapAndTransferKeys(wrapKeyData, encrKeyData, null, authKeyData);
    }


    private void validateMessageData(String clientId, String authenticationKey, String encryptionKey, String wrapperKey) {
        checkString(clientId, "client id");
        try {
            int clId = Integer.parseInt(clientId);
            if ((clId < 30) || (clId > 89)) {
                throw new IllegalArgumentException("Out of range (30-89");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error in client id: " + e.getMessage(), e);
        }

        checkKey(authenticationKey, "authentication key");
        checkKey(encryptionKey, "encryption key");
        checkKey(wrapperKey, "wrapper key");
    }

    private void checkString(String stringToCheck, String name) {
        if ((stringToCheck == null) || (stringToCheck.isEmpty())) {
            throw new IllegalArgumentException("Parameter " + name + " is 'null' or empty.");
        }
    }

    private void checkKey(String key, String name) {
        String msg = SecurityData.checkKey(key, name);
        if (!msg.isEmpty()) {
            throw new IllegalArgumentException("Error: " + msg);
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        tagSpec.add(new MessageAttributeSpec(ATTR_CLIENT_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTHENTICATION_KEY, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_ENCRYPTION_KEY, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_WRAPPER_KEY, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
