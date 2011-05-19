package com.energyict.genericprotocolimpl.nta.messagehandling;

import java.util.ArrayList;
import java.util.List;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.*;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;

/**
 * Protocol implementation of which messages should be used.
 * @author gna
 *
 */
public class MeterMessages extends GenericMessaging {

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
		
		
		// MessageCategorySpec catWakeUp = getWakeupCategory();
		// MessageCategorySpec catGPRSModemSetup = getGPRSModemSetupCategory();

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
		
		// These two are bound into one (catConnectivity) from 04/08/09
		// categories.add(catGPRSModemSetup);
		// categories.add(catWakeUp);

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

		//TODO uncomment this again after testing
//		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.CHANGELLSSECRET,
//				RtuMessageConstant.AEE_CHANGE_LLS_SECRET, false);
//		catAuthEncrypt.addMessageSpec(msgSpec);
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
