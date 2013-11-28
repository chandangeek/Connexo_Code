package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.*;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 5-sep-2011
 * Time: 8:38:03
 */
public class Dsmr40Messaging extends Dsmr23Messaging {

    private final Dsmr40MessageExecutor messageExecutor;
    private ArrayList<MessageEntry> securityMessages;
    private Map<String, MessageResult> securityResults = null;

    public Dsmr40Messaging(final GenericMessageExecutor messageExecutor) {
        super(messageExecutor);
        this.messageExecutor = (Dsmr40MessageExecutor) messageExecutor;
    }

    public List<MessageEntry> getSecurityMessages() {
        if (securityMessages == null) {
            securityMessages = new ArrayList<MessageEntry>();
        }
        return securityMessages;
    }

    /**
     * Map that links the (content of) message entries to their corresponding results
     */
    public Map<String, MessageResult> getSecurityResults() {
        if (securityResults == null) {
            securityResults = new HashMap<String, MessageResult>();
        }
        return securityResults;
    }

    /**
     * Check if there's pending security messages (enable/disable P0/P3 level). They should be executed in the right order.
     */
    @Override
    public void applyMessages(List messageEntries) throws IOException {
        for (Object messageEntryObject : messageEntries) {
            if (messageEntryObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) messageEntryObject;
                if (isEnableP0(messageEntry)) {
                    getSecurityMessages().add(messageEntry);
                }
            }
        }
        for (Object messageEntryObject : messageEntries) {
            if (messageEntryObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) messageEntryObject;
                if (isEnableP3(messageEntry)) {
                    getSecurityMessages().add(messageEntry);
                }
            }
        }
        for (Object messageEntryObject : messageEntries) {
            if (messageEntryObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) messageEntryObject;
                if (isDisableP0(messageEntry)) {
                    getSecurityMessages().add(messageEntry);
                }
            }
        }
        for (Object messageEntryObject : messageEntries) {
            if (messageEntryObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) messageEntryObject;
                if (isDisableP3(messageEntry)) {
                    getSecurityMessages().add(messageEntry);
                }
            }
        }
        super.applyMessages(messageEntries);
    }

    private boolean isDisableP3(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isDisableP0(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0);
    }

    private boolean isEnableP3(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isEnableP0(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0);
    }

    /**
     * If there's security messages, execute them all at once and in the right order.
     * Remember the results, return them when the actual security key message is queried.
     */
    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (securityResults == null) {            //Execute them only once!
            for (MessageEntry securityMessage : getSecurityMessages()) {
                MessageResult result = messageExecutor.executeMessageEntry(securityMessage);
                getSecurityResults().put(securityMessage.getContent(), result);
                if (result.isFailed()) {
                    break;      //Don't execute the next security key messages if one fails!
    }
            }
        }
        if (isDisableP0(messageEntry) || isDisableP3(messageEntry) || isEnableP0(messageEntry) || isEnableP3(messageEntry)) {
            MessageResult messageResult = getSecurityResults().get(messageEntry.getContent());
            if (messageResult == null) {
                messageResult = MessageResult.createFailed(messageEntry, "Earlier security message failed, skipping this message!");
            }
            return messageResult;
        } else {
            return messageExecutor.executeMessageEntry(messageEntry);
        }
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> messages = super.getMessageCategories();
        messages.add(getRestoreFactorySettings());
        messages.add(getDiscoverySettingsCategory());
        messages.add(ProtocolMessageCategories.getChangeAdministrativeStatusCategory());
        return messages;
    }

    /**
     * Has one extra attribute: imageIdentifier
     */
    public MessageCategorySpec getFirmwareCategory() {
        MessageCategorySpec catFirmware = new MessageCategorySpec(RtuMessageCategoryConstants.FIRMWARE);
        MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE, RtuMessageConstant.FIRMWARE_UPGRADE, false, true);
        catFirmware.addMessageSpec(msgSpec);
        return catFirmware;
    }

    @Override
    public MessageCategorySpec getAuthEncryptCategory() {
        MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
        MessageSpec msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.CHANGEHLSSECRET, RtuMessageConstant.AEE_CHANGE_HLS_SECRET, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTENCRYPTIONKEY, RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTAUTHENTICATIONKEY, RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addSecurityLevelMsg(RtuMessageKeyIdConstants.ACTIVATE_SECURITY, RtuMessageConstant.AEE_ACTIVATE_SECURITY, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.ENABLE_AUTHENTICATION_LEVEL_P0, RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.DISABLE_AUTHENTICATION_LEVEL_P0, RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.ENABLE_AUTHENTICATION_LEVEL_P3, RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.DISABLE_AUTHENTICATION_LEVEL_P3, RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        return catAuthEncrypt;
    }

}
