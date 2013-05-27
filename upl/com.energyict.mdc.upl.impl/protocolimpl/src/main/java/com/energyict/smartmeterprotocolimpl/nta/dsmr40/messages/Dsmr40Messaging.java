package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.*;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 5-sep-2011
 * Time: 8:38:03
 */
public class Dsmr40Messaging extends Dsmr23Messaging {

    private final Dsmr40MessageExecutor messageExecutor;

    public Dsmr40Messaging(final GenericMessageExecutor messageExecutor) {
        super(messageExecutor);
        this.messageExecutor = (Dsmr40MessageExecutor) messageExecutor;
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> messages = super.getMessageCategories();
        messages.add(getRestoreFactorySettings());

        //TODO enable once it is required
//        messages.add(ProtocolMessageCategories.getChangeAdministrativeStatusCategory());
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
