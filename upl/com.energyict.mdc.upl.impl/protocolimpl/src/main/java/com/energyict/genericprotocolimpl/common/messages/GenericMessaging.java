package com.energyict.genericprotocolimpl.common.messages;

import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;

/**
 * 
 * @author gna
 * 
 *         <p>
 *         Abstract base class to implement messages in your protocol. Your
 *         protocol needs to extend your own MeterMessageClass. The extended
 *         class only needs to construct of list of MessageCategories. Common
 *         used getXCategories are defined below.
 *         </p>
 * 
 */
public abstract class GenericMessaging implements Messaging {

	/** Abstract method to define your message categories **/
	public abstract List getMessageCategories();

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

	public String writeTag(MessageTag msgTag) {
		StringBuffer buf = new StringBuffer();

		// a. Opening tag
		buf.append("<");
		buf.append(msgTag.getName());

		// b. Attributes
		for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = (MessageAttribute) it.next();
			if (att.getValue() == null || att.getValue().length() == 0)
				continue;
			buf.append(" ").append(att.getSpec().getName());
			buf.append("=").append('"').append(att.getValue()).append('"');
		}
		if (msgTag.getSubElements().isEmpty()) {
			buf.append("/>");
			return buf.toString();
		}
		buf.append(">");
		// c. sub elements
		for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
			MessageElement elt = (MessageElement) it.next();
			if (elt.isTag())
				buf.append(writeTag((MessageTag) elt));
			else if (elt.isValue()) {
				String value = writeValue((MessageValue) elt);
				if (value == null || value.length() == 0)
					return "";
				buf.append(value);
			}
		}

		// d. Closing tag
		buf.append("</");
		buf.append(msgTag.getName());
		buf.append(">");

		return buf.toString();
	}

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	/**
	 * Will create a message that requires an xml code in the value field of the
	 * XMLConfig element
	 * 
	 * @return
	 */
	public MessageCategorySpec getXmlConfigCategory() {
		MessageCategorySpec mcs = new MessageCategorySpec(
				RtuMessageCategoryConstants.XLMCONFIG);
		MessageSpec msgSpec = addDefaultValueMsg(
				RtuMessageKeyIdConstants.XLMCONFIG,
				RtuMessageConstant.XMLCONFIG, false);
		mcs.addMessageSpec(msgSpec);
		return mcs;
	}

	/**
	 * Create a message that uses a UserFile id as value for the <b>firmware</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getFirmwareCategory() {
		MessageCategorySpec catFirmware = new MessageCategorySpec(
				RtuMessageCategoryConstants.FIRMWARE);
		MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE,
				RtuMessageConstant.FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		return catFirmware;
	}

	/**
	 * Creates two messages, one for sending <b>code to P1</b>, the other for sending
	 * <b>text to P1</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getP1Category() {
		MessageCategorySpec catP1Messages = new MessageCategorySpec(
				RtuMessageCategoryConstants.CONSUMERMESSAGEP1);
		MessageSpec msgSpec = addP1Text(
				RtuMessageKeyIdConstants.CONSUMERTEXTP1,
				RtuMessageConstant.P1TEXTMESSAGE, false);
		catP1Messages.addMessageSpec(msgSpec);
		msgSpec = addP1Code(RtuMessageKeyIdConstants.CONSUMERCODEP1,
				RtuMessageConstant.P1CODEMESSAGE, false);
		catP1Messages.addMessageSpec(msgSpec);
		return catP1Messages;
	}

	/**
	 * Creates three messages, one for <b>disconnect</b>, one for <b>connect</b> and the other
	 * one to <b>set the connectMode</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getConnectControlCategory() {
		MessageCategorySpec catDisconnect = new MessageCategorySpec(
				RtuMessageCategoryConstants.DISCONNECTCONTROL);
		MessageSpec msgSpec = addConnectControl(
				RtuMessageKeyIdConstants.DISCONNECT,
				RtuMessageConstant.DISCONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControl(RtuMessageKeyIdConstants.CONNECT,
				RtuMessageConstant.CONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControlMode(
				RtuMessageKeyIdConstants.CONNECTCONTROLMODE,
				RtuMessageConstant.CONNECT_CONTROL_MODE, false);
		catDisconnect.addMessageSpec(msgSpec);
		return catDisconnect;
	}

	/**
	 * Create three messages, one to <b>configure the loadlimiting parameters</b>, one
	 * to <b>clear the loadlimit config</b>, and one to <b>set the loadlimit groupId</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getLoadLimitCategory() {
		MessageCategorySpec catLoadLimit = new MessageCategorySpec(
				RtuMessageCategoryConstants.LOADLIMIT);
		MessageSpec msgSpec = addConfigureLL(
				RtuMessageKeyIdConstants.LOADLIMITCONFIG,
				RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false);
		catLoadLimit.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.LOADLIMITCLEAR,
				RtuMessageConstant.LOAD_LIMIT_DISABLE, false);
		catLoadLimit.addMessageSpec(msgSpec);
		msgSpec = addGroupIdsLL(RtuMessageKeyIdConstants.LOADLIMITGROUPID,
				RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST,
				false);
		catLoadLimit.addMessageSpec(msgSpec);
		return catLoadLimit;
	}

	/**
	 * Creates two messages, on to give up the ID of the code table to use as an
	 * <b>activityCalendar</b>, the other to give the code table id of the <b>special days</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getActivityCalendarCategory() {
		MessageCategorySpec catActivityCal = new MessageCategorySpec(
				RtuMessageCategoryConstants.ACTICITYCALENDAR);
		MessageSpec msgSpec = addTimeOfUse(
				RtuMessageKeyIdConstants.ACTIVITYCALENDAR,
				RtuMessageConstant.TOU_ACTIVITY_CAL, false);
		catActivityCal.addMessageSpec(msgSpec);
		msgSpec = addSpecialDays(RtuMessageKeyIdConstants.SPECIALDAYS,
				RtuMessageConstant.TOU_SPECIAL_DAYS, false);
		catActivityCal.addMessageSpec(msgSpec);
		return catActivityCal;
	}

	/**
	 * Create a message to set the meter to a certain time
	 * 
	 * @return
	 */
	public MessageCategorySpec getTimeCategory() {
		MessageCategorySpec catTime = new MessageCategorySpec(
				RtuMessageCategoryConstants.TIME);
		MessageSpec msgSpec = addTimeMessage(RtuMessageKeyIdConstants.SETTIME,
				RtuMessageConstant.SET_TIME, true);
		catTime.addMessageSpec(msgSpec);
		return catTime;
	}

	/**
	 * Create a message to simulate database entries in the meter
	 * 
	 * @return
	 */
	public MessageCategorySpec getDataBaseEntriesCategory() {
		MessageCategorySpec catMakeEntries = new MessageCategorySpec(
				RtuMessageCategoryConstants.DATABASEENTRIES);
		MessageSpec msgSpec = addCreateDBEntries(
				RtuMessageKeyIdConstants.CREATEDATABASEENTRIES,
				RtuMessageConstant.ME_MAKING_ENTRIES, true);
		catMakeEntries.addMessageSpec(msgSpec);
		return catMakeEntries;
	}

	/**
	 * Create a message to set the <b>apn, username, password </b>of the gprs modem
	 * 
	 * @return
	 */
	public MessageCategorySpec getGPRSModemSetupCategory() {
		MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
				RtuMessageCategoryConstants.GPRSMODEMSETUP);
		MessageSpec msgSpec = addChangeGPRSSetup(
				RtuMessageKeyIdConstants.GPRSMODEMSETUP,
				RtuMessageConstant.GPRS_MODEM_SETUP, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
	}

	/**
	 * Creates a message with one value for example to parse a userfile ID
	 * 
	 * @return
	 */
	public MessageCategorySpec getTestCategory() {
		MessageCategorySpec catTestMessage = new MessageCategorySpec(
				RtuMessageCategoryConstants.TESTMESSAGE);
		MessageSpec msgSpec = addTestMessage(
				RtuMessageKeyIdConstants.TESTMESSAGE,
				RtuMessageConstant.TEST_MESSAGE, true);
		catTestMessage.addMessageSpec(msgSpec);
		return catTestMessage;
	}

	/**
	 * Creates a message without a value, <b>meterreset</b> must be handled after this
	 * message
	 * 
	 * @return
	 */
	public MessageCategorySpec getGlobalResetCategory() {
		MessageCategorySpec catGlobalDisc = new MessageCategorySpec(
				RtuMessageCategoryConstants.GLOBALRESET);
		MessageSpec msgSpec = addNoValueMsg(
				RtuMessageKeyIdConstants.GLOBALRESET,
				RtuMessageConstant.GLOBAL_METER_RESET, false);
		catGlobalDisc.addMessageSpec(msgSpec);
		return catGlobalDisc;
	}

	/**
	 * Create one message to set <b>phoneNumbers to the whiteList</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getWakeupCategory() {
		MessageCategorySpec catWakeUp = new MessageCategorySpec(
				RtuMessageCategoryConstants.WAKEUPFUNCTIONALITY);
		MessageSpec msgSpec = addPhoneListMsg(
				RtuMessageKeyIdConstants.SETWHITELIST,
				RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
		catWakeUp.addMessageSpec(msgSpec);
		return catWakeUp;
	}

	/**
	 * Create three messages, one to change the <b>globalKey</b>, one to chang the
	 * <b>AuthenticationKey</b>, and the other one to change
	 * the <b>HLSSecret</b>
	 * 
	 * @return
	 */
	public MessageCategorySpec getAuthEncryptCategory() {
		MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(
				RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
		// TODO do these have to be advanced messages?
		MessageSpec msgSpec = addNoValueMsg(
				RtuMessageKeyIdConstants.CHANGEHLSSECRET,
				RtuMessageConstant.AEE_CHANGE_HLS_SECRET, false);
		catAuthEncrypt.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.CHANGEGLOBALKEY,
				RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY, false);
		catAuthEncrypt.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.CHANGEAUTHENTICATIONKEY,
				RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY, false);
		catAuthEncrypt.addMessageSpec(msgSpec);
		return catAuthEncrypt;
	}

	/**
	 * Create three messages, one to <b>decommission</b> the mbus device, on to set the
	 * <b>encryption keys</b> and one to set the <b>corrected or uncorrected</b>
	 * gasLoadProfile
	 * 
	 * @return
	 */
	public MessageCategorySpec getMbusSetupCategory() {
		MessageCategorySpec catMbusSetup = new MessageCategorySpec(
				RtuMessageCategoryConstants.MBUSSETUP);
		MessageSpec msgSpec = addNoValueMsg(
				RtuMessageKeyIdConstants.MBUSDECOMMISSION,
				RtuMessageConstant.MBUS_DECOMMISSION, false);
		catMbusSetup.addMessageSpec(msgSpec);
		msgSpec = addEncryptionkeys(RtuMessageKeyIdConstants.MBUSENCRYPTIONKEY,
				RtuMessageConstant.MBUS_ENCRYPTION_KEYS, false);
		catMbusSetup.addMessageSpec(msgSpec);
		msgSpec = addCorrectSwitchMsg(
				RtuMessageKeyIdConstants.MBUSGASCORRECTION,
				RtuMessageConstant.MBUS_CORRECTED_SWITCH, false);
		catMbusSetup.addMessageSpec(msgSpec);
		return catMbusSetup;
	}

//	private MessageSpec addChangeHLSKeyMsg(String keyId, String tagName,
//			boolean advanced) {
//		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
//		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
//		MessageValueSpec msgVal = new MessageValueSpec();
//		msgVal.setValue(" ");
//		tagSpec.add(msgVal);
//		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
//				RtuMessageConstant.AEE_HLS_SECRET, true);
//		tagSpec.add(msgAttrSpec);
//		msgSpec.add(tagSpec);
//		return msgSpec;
//	}
//
//	private MessageSpec addChangeGlobalKey(String keyId, String tagName,
//			boolean advanced) {
//		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
//		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
//		MessageValueSpec msgVal = new MessageValueSpec();
//		msgVal.setValue(" ");
//		tagSpec.add(msgVal);
//		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
//				RtuMessageConstant.AEE_GLOBAL_KEY, true);
//		tagSpec.add(msgAttrSpec);
//		msgAttrSpec = new MessageAttributeSpec(
//				RtuMessageConstant.AEE_GLOBAL_KEY_TYPE, true);
//		tagSpec.add(msgAttrSpec);
//		msgSpec.add(tagSpec);
//		return msgSpec;
//	}

	private MessageSpec addSpecialDays(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE, false);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addNoValueMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addGroupIdsLL(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID, true);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addConfigureLL(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION,
				false);
		tagSpec.add(msgAttrSpec);
		MessageTagSpec profileTagSpec = new MessageTagSpec("Emergency_Profile");
		profileTagSpec.add(msgVal);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID, false);
		profileTagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME, false);
		profileTagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.LOAD_LIMIT_EP_DURATION, false);
		profileTagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		tagSpec.add(profileTagSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addChangeGPRSSetup(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.GPRS_APN, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.GPRS_USERNAME, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.GPRS_PASSWORD, false);
		tagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addCreateDBEntries(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.ME_START_DATE, true);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.ME_NUMBER_OF_ENTRIES, true);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.ME_INTERVAL,
				true);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.ME_SET_CLOCK_BACK, false);
		tagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addTimeMessage(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.SET_TIME_VALUE, true);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addPhoneListMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.WAKEUP_NR1, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR2,
				false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR3,
				false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR4,
				false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR5,
				false);
		tagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addTestMessage(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TEST_FILE, true);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addConnectControl(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, false);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addConnectControlMode(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.CONNECT_MODE, true);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addP1Code(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.P1CODE, false);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addP1Text(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.P1TEXT, false);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addFirmwareMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.FIRMWARE, true);
		tagSpec.add(msgAttrSpec);

		/*
		 * The Act. Now value is deleted, we use the ActivationDate to check if
		 * we need activation now or not. This way it's the same as for example
		 * with the disconnector.
		 */

		// msgAttrSpec = new
		// MessageAttributeSpec(RtuMessageConstant.FIRMWARE_ACTIVATE_NOW,
		// false);
		// tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.FIRMWARE_ACTIVATE_DATE, false);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addDefaultValueMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addTimeOfUse(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		tagSpec.add(msgVal);
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TOU_ACTIVITY_NAME, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TOU_ACTIVITY_DATE, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.TOU_ACTIVITY_USER_FILE, false);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addCorrectSwitchMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.MBUS_CORRECTED_VALUE, true);
		tagSpec.add(msgVal);
		tagSpec.add(msgAttrSpec);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private MessageSpec addEncryptionkeys(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
		msgVal.setValue(" ");
		MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.MBUS_OPEN_KEY, false);
		tagSpec.add(msgAttrSpec);
		msgAttrSpec = new MessageAttributeSpec(
				RtuMessageConstant.MBUS_TRANSFER_KEY, false);
		tagSpec.add(msgAttrSpec);
		tagSpec.add(msgVal);
		msgSpec.add(tagSpec);
		return msgSpec;
	}
}
