package com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 13:17:46
 */
public class Dsmr23Messaging extends GenericMessaging implements MessageProtocol {

    private final Dsmr23MessageExecutor messageExecutor;

    /**
     * Boolean indicating whether or not to show the MBus related messages in EIServer
     */
    protected boolean supportMBus = true;

    /**
     * Flag to decide if serial no or channel id should be used in the clear the MBus client message
     */
    protected boolean useSerialNoInClearMBusClientMessage = false;

    /**
     * Boolean indicating whether or not to show the GPRS related messages in EIServer
     */
    protected boolean supportGPRS = true;

    /**
     * Boolean indicating whether or not to show the messages related to resetting the meter in EIServer
     */
    protected boolean supportMeterReset = true;

    /**
     * Boolean indicating whether or not to show the messages related to configuring the limiter in EIServer
     */
    protected boolean supportsLimiter = true;

    /**
     * Boolean indicating whether or not to show the message to reset the alarm window in EIServer
     */
    protected boolean supportResetWindow = true;

    /**
     * Boolean indicating whether or not to show the XML config message in EIServer
     */
    protected boolean supportXMLConfig = true;

    public Dsmr23Messaging(final MessageParser messageParser) {
        this.messageExecutor = (Dsmr23MessageExecutor) messageParser;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List<MessageEntry> messageEntries) throws IOException {
        //nothing much to do here ...
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        MessageCategorySpec catFirmware = getFirmwareCategory();
        MessageCategorySpec catP1Messages = getP1Category();
        if (supportMBus) {
            MessageCategorySpec mbusCategory = getSimpleInstallMbusCategory(useSerialNoInClearMBusClientMessage);
            mbusCategory.addMessageSpec(addMBusClientRemoteMessage(RtuMessageKeyIdConstants.MBUS_CLIENT_REMOTE_COMMISSION, RtuMessageConstant.MBUS_REMOTE_COMMISSION, false));
            mbusCategory.addMessageSpec(addChangeMBusAttributesMessage(RtuMessageKeyIdConstants.CHANGE_MBUS_CLIENT_ATTRIBUTES, RtuMessageConstant.CHANGE_MBUS_CLIENT_ATTRIBUTES, false));
            categories.add(mbusCategory);
        }
        if (supportsLimiter) {
        MessageCategorySpec catLoadLimit = getLoadLimitCategory();
            categories.add(catLoadLimit);
        }
        MessageCategorySpec catActivityCal = getActivityCalendarCategory();
        MessageCategorySpec catTime = getTimeCategory();
        MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
        MessageCategorySpec catTestMessage = getTestCategory();
        //MessageCategorySpec catTestSecurityMessage = getTestSecurityCategory();
        if (supportMeterReset) {
        MessageCategorySpec catGlobalDisc = getGlobalResetCategory();
            categories.add(catGlobalDisc);
        }
        MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
        if (supportGPRS) {
        MessageCategorySpec catConnectivity = getConnectivityCategory();
            categories.add(catConnectivity);
        }
        MessageCategorySpec catResetParameters;
        if (supportResetWindow) {
            catResetParameters = getResetParametersCategory();
        } else {
            catResetParameters = getAlarmResetCategory();
        }

        if (supportXMLConfig) {
            MessageCategorySpec catXMLConfig = getXmlConfigCategory();
        categories.add(catXMLConfig);
        }
        categories.add(catFirmware);
        categories.add(catP1Messages);
        if (messageExecutor.getProtocol().hasBreaker()) {
            categories.add(getConnectControlCategory());
        }
        categories.add(catActivityCal);
        categories.add(catTime);
        categories.add(catMakeEntries);
        categories.add(catTestMessage);
        //categories.add(catTestSecurityMessage);         //TODO uncomment when functionality should be added
        categories.add(catAuthEncrypt);
        categories.add(catResetParameters);

        return categories;
    }

    protected MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        catGPRSModemSetup.addMessageSpec(
                addChangeGPRSSetup(
                    RtuMessageKeyIdConstants.GPRSMODEMSETUP,
                    RtuMessageConstant.GPRS_MODEM_SETUP,
                        false));
        catGPRSModemSetup.addMessageSpec(
                addGPRSModemCredantials(
                        RtuMessageKeyIdConstants.GPRSCREDENTIALS,
                        RtuMessageConstant.GPRS_MODEM_CREDENTIALS,
                        false));
        catGPRSModemSetup.addMessageSpec(
                addPhoneListMsg(
                        RtuMessageKeyIdConstants.SETWHITELIST,
                        RtuMessageConstant.WAKEUP_ADD_WHITELIST,
                        false));
        catGPRSModemSetup.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
                        RtuMessageConstant.WAKEUP_ACTIVATE,
                        false));
        catGPRSModemSetup.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
                        RtuMessageConstant.WAKEUP_DEACTIVATE,
                        false));
        return catGPRSModemSetup;
    }

    protected MessageCategorySpec getResetParametersCategory() {
        MessageCategorySpec catResetParameters = new MessageCategorySpec(
                RtuMessageCategoryConstants.RESET_PARAMETERS);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.RESETALARMREGISTER,
                RtuMessageConstant.RESET_ALARM_REGISTER, false);
        catResetParameters.addMessageSpec(msgSpec);

        msgSpec = addChangeDefaultResetWindowMsg(
                RtuMessageKeyIdConstants.CHANGEDEFAULTRESETWINDOW,
                RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW, false);
        catResetParameters.addMessageSpec(msgSpec);
        return catResetParameters;
    }

    protected MessageCategorySpec getAlarmResetCategory() {
        MessageCategorySpec catResetParameters = new MessageCategorySpec(
                RtuMessageCategoryConstants.RESET_PARAMETERS);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.RESETALARMREGISTER,
                RtuMessageConstant.RESET_ALARM_REGISTER, false);
        catResetParameters.addMessageSpec(msgSpec);
        return catResetParameters;
    }

    /**
     * Create three messages, one to change the <b>globalKey</b>, one to change the
     * <b>AuthenticationKey</b>, and the other one to change
     * the <b>HLSSecret</b>
     *
     * @return a category with four MessageSpecs for Authenticate/Encrypt functionality
     */
    @Override
    public MessageCategorySpec getAuthEncryptCategory() {
        MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(
                RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.CHANGEHLSSECRET,
                RtuMessageConstant.AEE_CHANGE_HLS_SECRET, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTENCRYPTIONKEY,
                RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTAUTHENTICATIONKEY,
                RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addSecurityLevelMsg(RtuMessageKeyIdConstants.ACTIVATE_SECURITY,
                RtuMessageConstant.AEE_ACTIVATE_SECURITY, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        msgSpec = addAuthenticationLevelMsg(RtuMessageKeyIdConstants.CHANGE_AUTHENTICATION_LEVEL,
                RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL, true);
        catAuthEncrypt.addMessageSpec(msgSpec);
        return catAuthEncrypt;
    }

    public void setSupportMBus(boolean supportMBus) {
        this.supportMBus = supportMBus;
    }

    public void setSupportGPRS(boolean supportGPRS) {
        this.supportGPRS = supportGPRS;
    }

    public void setSupportMeterReset(boolean supportMeterReset) {
        this.supportMeterReset = supportMeterReset;
    }

    public void setSupportsLimiter(boolean supportsLimiter) {
        this.supportsLimiter = supportsLimiter;
    }

    public void setSupportResetWindow(boolean supportResetWindow) {
        this.supportResetWindow = supportResetWindow;
    }

    public void setSupportXMLConfig(boolean supportXMLConfig) {
        this.supportXMLConfig = supportXMLConfig;
    }

    public void setUseSerialNoInClearMBusClientMessage(boolean useSerialNoInClearMBusClientMessage) {
        this.useSerialNoInClearMBusClientMessage = useSerialNoInClearMBusClientMessage;
    }

    private MessageSpec addMBusClientRemoteMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);;
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_INSTALL_CHANNEL, true));
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_SHORT_ID, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addChangeMBusAttributesMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_INSTALL_CHANNEL, true));
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_CLIENT_MANUFACTURER_ID, true));
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_CLIENT_IDENTIFICATION_NUMBER, true));
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_CLIENT_DEVICE_TYPE, true));
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_CLIENT_VERSION, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
