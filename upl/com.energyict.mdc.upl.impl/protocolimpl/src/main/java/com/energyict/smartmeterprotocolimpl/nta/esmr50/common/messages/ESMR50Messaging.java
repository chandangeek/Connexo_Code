package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.List;
@Deprecated
public class ESMR50Messaging extends Dsmr40Messaging {
    private final ESMR50MessageExecutor messageExecutor;

    public ESMR50Messaging(ESMR50MessageExecutor messageExecutor) {
        super(messageExecutor);

        this.messageExecutor = messageExecutor;
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> messageCategories = super.getMessageCategories();
        messageCategories.add(getLTEModemSetupCategory());
//        messageCategories.add(getDefinableLoadProfileCategory()); todo Add definableLoadProfileCategory
//        addMessageSpecToCategory(messageCategories, RtuMessageCategoryConstants.MBUSSETUP, createMbusChangeConfigurationObjectMessageSpec());
        return messageCategories;
    }

    private void addMessageSpecToCategory(List<MessageCategorySpec> messageCategories, String category, MessageSpec messageSpec) {
        for(MessageCategorySpec messageCategory : messageCategories ){
            if (messageCategory.getName().equals(category)){
                messageCategory.addMessageSpec(messageSpec);
            }
        }
    }

        //todo create MbusChangeConfiguration method?
//    private MessageSpec createMbusChangeConfigurationObjectMessageSpec() {
//        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.EMSR5_MBUS_CHANGE_CONFIGURATION_OBJECT, false);
//        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.MBUS_CHANGE_CONFIGURATION_OBJECT);
//        MessageAttributeSpec bit11attr = new MessageAttributeSpec(RtuMessageConstant.MBUS_CHANGE_CONFIGURATION_OBJECT_BIT11, true);
//        tagSpec.add(bit11attr);
//        msgSpec.add(tagSpec);
//        return msgSpec;
//    }


    /**
     * MessageCategory containing relevant functionality to change the administrative state of a ESMR 5.0 meter.
     *
     * @return the newly created category
     */
    /**
     * Create a message to set the <b>apn, username, password </b>of the LTE modem
     *
     * @return a category with one MessageSpec for LTE setup functionality
     */
    public MessageCategorySpec getLTEModemSetupCategory() {
        MessageCategorySpec catLTEModemSetup = new MessageCategorySpec("test");
        //todo Fix getLTEModemSetupCategory
//        (
//                RtuMessageCategoryConstants.LTEMODEMSETUP);
//        MessageSpec msgSpec = addChangeLTESetup(
//                RtuMessageKeyIdConstants.LTEMODEMSETUP,
//                RtuMessageConstant.LTE_MODEM_SETUP, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//
//        msgSpec = addChangeLTESetupAPN(RtuMessageKeyIdConstants.SET_LTE_APN,
//                RtuMessageConstant.SET_LTE_APN, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//
//        msgSpec = addSetLTEPingAddress(RtuMessageKeyIdConstants.SET_LTE_PING_ADDRESS,
//                RtuMessageConstant.SET_LTE_PING_ADDRESS, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);

        return catLTEModemSetup;
    }


    /**
     * Creates a MessageSpec with the configuration fields for the LTE connection setup
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addChangeLTESetup(String keyId, String tagName,
                                             boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec("test", false);
//                RtuMessageConstant.LTE_APN, false);
//        tagSpec.add(msgAttrSpec);
//        msgAttrSpec = new MessageAttributeSpec(
//                RtuMessageConstant.LTE_USERNAME, false);
//        tagSpec.add(msgAttrSpec);
//        msgAttrSpec = new MessageAttributeSpec(
//                RtuMessageConstant.LTE_PASSWORD, false);
//        tagSpec.add(msgAttrSpec);
//        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addSetLTEPingAddress(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
                "test", false); // TODO add correct property
        tagSpec.add(msgAttrSpec);

        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addChangeLTESetupAPN(String keyId, String tagName,
                                            boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
                "test", false); // todo add correct property name
        tagSpec.add(msgAttrSpec);

        msgSpec.add(tagSpec);
        return msgSpec;
    }


    protected MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catLTEModemSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        //todo Add correct message spec
//        MessageSpec msgSpec = addChangeLTESetup(
//                RtuMessageKeyIdConstants.LTEMODEMSETUP,
//                RtuMessageConstant.LTE_MODEM_SETUP, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//        msgSpec = addGPRSModemCredantials(RtuMessageKeyIdConstants.LTECREDENTIALS,
//                RtuMessageConstant.LTE_MODEM_CREDENTIALS, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//        msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
//                RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
//                RtuMessageConstant.WAKEUP_ACTIVATE, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
//        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
//                RtuMessageConstant.WAKEUP_DEACTIVATE, false);
//        catLTEModemSetup.addMessageSpec(msgSpec);
        return catLTEModemSetup;
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
//        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.ENABLE_AUTHENTICATION_LEVEL_P0P3, RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL, true);
        catAuthEncrypt.addMessageSpec(msgSpec); // todo add correct properties
//        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.DISABLE_AUTHENTICATION_LEVEL_P0P3, RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        return catAuthEncrypt;
    }

    /**
     * Has one extra message: LTE Firmware Upgrade
     */
    public MessageCategorySpec getFirmwareCategory() {
        MessageCategorySpec catFirmware = new MessageCategorySpec(RtuMessageCategoryConstants.FIRMWARE);
        MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE, RtuMessageConstant.FIRMWARE_UPGRADE, false, true);
        catFirmware.addMessageSpec(msgSpec);
        //todo
//        msgSpec = addSetLTEModemFWLocationMsg(RtuMessageKeyIdConstants.SET_LTE_MODEM_FW_LOCATION, RtuMessageConstant.SET_LTE_FW_LOCATION, false);
//        catFirmware.addMessageSpec(msgSpec);
//        msgSpec = addSetLTEModemFWDownloadTimeMsg(RtuMessageKeyIdConstants.SET_LTE_MODEM_FW_DOWNLOAD_TIME, RtuMessageConstant.SET_LTE_FW_DOWNLOAD_TIME, false);
//        catFirmware.addMessageSpec(msgSpec);
//        msgSpec = initiateLTEImageTransferMsg(RtuMessageKeyIdConstants.INITIATE_LTE_IMAGE_TRANSFER, false);
//        catFirmware.addMessageSpec(msgSpec);
//        msgSpec = activateLTEImageTransferMsg(RtuMessageKeyIdConstants.ACTIVATE_LTE_IMAGE, false);
//        msgSpec = addLTEFirmwareUpgradeMsg(RtuMessageKeyIdConstants.LTE_MODEM_FIRMWARE_UPGRADE, RtuMessageConstant.LTE_MODEM_FIRMWARE_UPGRADE, false);
        catFirmware.addMessageSpec(msgSpec);
        return catFirmware;
    }

    private MessageSpec activateLTEImageTransferMsg(String keyId, boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(keyId.replace(' ', '_'));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec initiateLTEImageTransferMsg(String keyId, boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(keyId.replace(' ', '_'));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addSetLTEModemFWDownloadTimeMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
//        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec("", false);
//                RtuMessageConstant.SET_LTE_FW_DOWNLOAD_TIME, true); todo add correct property
//        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addSetLTEModemFWLocationMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
//        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec("", false);
//                RtuMessageConstant.SET_LTE_FW_LOCATION, true); // todo add correct property
//        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }


    private MessageSpec addLTEFirmwareUpgradeMsg(String keyId, String tagName,
                                                 boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
//        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec("", false);
//                RtuMessageConstant.LTE_MODEM_FIRMWARE_UPGRADE_USERFILE_ID, true); todo add correct property
//        tagSpec.add(msgAttrSpec);
//        msgAttrSpec = new MessageAttributeSpec(
//                RtuMessageConstant.LTE_MODEM_FIRMWARE_UPGRADE_DOWNLOAD_TIMEOUT, true);
//        tagSpec.add(msgAttrSpec);
//        msgVal.setValue(" ");
        msgSpec.add(tagSpec);
        return msgSpec;
    }


}
