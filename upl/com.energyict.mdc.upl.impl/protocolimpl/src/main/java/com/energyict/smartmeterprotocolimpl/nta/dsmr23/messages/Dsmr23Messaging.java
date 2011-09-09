package com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages;

import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.*;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 13:17:46
 */
public class Dsmr23Messaging extends GenericMessaging implements MessageProtocol{

    private final Dsmr23MessageExecutor messageExecutor;

    public Dsmr23Messaging(final GenericMessageExecutor messageExecutor) {
        this.messageExecutor = (Dsmr23MessageExecutor) messageExecutor;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
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

	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catXMLConfig = getXmlConfigCategory();
		MessageCategorySpec catFirmware = getFirmwareCategory();
		MessageCategorySpec catP1Messages = getP1Category();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catLoadLimit = getLoadLimitCategory();
		MessageCategorySpec catActivityCal = getActivityCalendarCategory();
		MessageCategorySpec catTime = getTimeCategory();
		MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
		MessageCategorySpec catTestMessage = getTestCategory();
		MessageCategorySpec catGlobalDisc = getGlobalResetCategory();
		MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
		MessageCategorySpec catConnectivity = getConnectivityCategory();

		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catTestMessage);
		categories.add(catGlobalDisc);
		categories.add(catConnectivity);
		categories.add(catAuthEncrypt);

		return categories;
	}

	private MessageCategorySpec getConnectivityCategory() {
		MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
				RtuMessageCategoryConstants.CHANGECONNECTIVITY);
		MessageSpec msgSpec = addChangeGPRSSetup(
				RtuMessageKeyIdConstants.GPRSMODEMSETUP,
				RtuMessageConstant.GPRS_MODEM_SETUP, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addGPRSModemCredantials(RtuMessageKeyIdConstants.GPRSCREDENTIALS,
                RtuMessageConstant.GPRS_MODEM_CREDENTIALS, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
				RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_ACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_DEACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
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

}
