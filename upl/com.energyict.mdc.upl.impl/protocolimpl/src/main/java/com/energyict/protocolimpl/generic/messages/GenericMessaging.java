package com.energyict.protocolimpl.generic.messages;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;

import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class to implement messages in your protocol. Your
 * protocol needs to extend your own MeterMessageClass. The extended
 * class only needs to construct of list of MessageCategories. Commonly
 * used get'X'Categories are defined below.
 *
 * @author gna
 */
public abstract class GenericMessaging implements Messaging {

    /**
     * Tag that wraps around an included file.
     */
    public static final String INCLUDED_USERFILE_TAG = "IncludedFile";
    /**
     * The tag that is used for an include file.
     */
    protected static final String INCLUDE_USERFILE_TAG = "includeFile";
    /**
     * This is an attribute to aforementioned tag indicating the ID of the user file. See DeviceMessageContentParser for more details.
     */
    protected static final String INCLUDE_USERFILE_ID_ATTRIBUTE = "fileId";
    /**
     * This is an attribute tag to indicate whether zipping needs to be applied. See DeviceMessageContentParser for more details.
     */
    protected static final String CREATEZIP_ATTRIBUTE_TAG = "createZip";

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            builder.append("/>");
            return builder.toString();
        }
        builder.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    /**
     * Will create a message that requires an xml code in the value field of the
     * XMLConfig element
     *
     * @return a category with one MessageSpec for xmlConfig functionality
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
     * @return a category with one MessageSpec for firmwareUpgrade functionality
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
     * @return a category with two MessageSpecs for P1 functionality
     */
    public MessageCategorySpec getP1Category() {
        MessageCategorySpec catP1Messages = new MessageCategorySpec(
                RtuMessageCategoryConstants.CONSUMERMESSAGEP1);
        MessageSpec msgSpec = addP1Text(
                RtuMessageKeyIdConstants.CONSUMERTEXTP1,
                RtuMessageConstant.P1TEXTMESSAGE, false);
        catP1Messages.addMessageSpec(msgSpec);
        catP1Messages.addMessageSpec(addP1Code(RtuMessageKeyIdConstants.CONSUMERCODEP1, RtuMessageConstant.P1CODEMESSAGE, false));
        return catP1Messages;
    }

    /**
     * Creates three messages, one for <b>disconnect</b>, one for <b>connect</b> and the other
     * one to <b>set the connectMode</b>
     *
     * @return a category with three MessageSpecs for Connect/Disconnect control functionality
     */
    public MessageCategorySpec getConnectControlCategory() {
        MessageCategorySpec catDisconnect = new MessageCategorySpec(
                RtuMessageCategoryConstants.DISCONNECTCONTROL);
        MessageSpec msgSpec = addConnectControl(
                RtuMessageKeyIdConstants.DISCONNECT,
                RtuMessageConstant.DISCONNECT_LOAD, false);
        catDisconnect.addMessageSpec(msgSpec);
        catDisconnect.addMessageSpec(addConnectControl(RtuMessageKeyIdConstants.CONNECT, RtuMessageConstant.CONNECT_LOAD, false));
        catDisconnect.addMessageSpec(
                addConnectControlMode(
                    RtuMessageKeyIdConstants.CONNECTCONTROLMODE,
                    RtuMessageConstant.CONNECT_CONTROL_MODE, false));
        return catDisconnect;
    }

    /**
     * Create three messages, one to <b>configure the loadlimiting parameters</b>, one
     * to <b>clear the loadlimit config</b>, and one to <b>set the loadlimit groupId</b>
     *
     * @return a category with three MessageSpecs for LoadLimit functionality
     */
    public MessageCategorySpec getLoadLimitCategory() {
        MessageCategorySpec catLoadLimit = new MessageCategorySpec(RtuMessageCategoryConstants.LOADLIMIT);
        catLoadLimit.addMessageSpec(
                addConfigureLL(
                    RtuMessageKeyIdConstants.LOADLIMITCONFIG,
                    RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false));
        catLoadLimit.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.LOADLIMITCLEAR,
                        RtuMessageConstant.LOAD_LIMIT_DISABLE, false));
        catLoadLimit.addMessageSpec(
                addGroupIdsLL(
                    RtuMessageKeyIdConstants.LOADLIMITGROUPID,
                    RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST,
                    false));
        return catLoadLimit;
    }

    /**
     * Creates two messages, on to give up the ID of the code table to use as an
     * <b>activityCalendar</b>, the other to give the code table id of the <b>special days</b>
     *
     * @return a category with two MessageSpecs for ActivityCalendar functionality
     */
    public MessageCategorySpec getActivityCalendarCategory(String prefix) {
        MessageCategorySpec catActivityCal = new MessageCategorySpec(prefix + RtuMessageCategoryConstants.ACTICITYCALENDAR);
        catActivityCal.addMessageSpec(
                addTimeOfUse(
                    RtuMessageKeyIdConstants.ACTIVITYCALENDAR,
                    prefix + RtuMessageConstant.TOU_ACTIVITY_CAL,
                        false));
        catActivityCal.addMessageSpec(
                addSpecialDays(
                        RtuMessageKeyIdConstants.SPECIALDAYS,
                        prefix + RtuMessageConstant.TOU_SPECIAL_DAYS,
                        false));
        return catActivityCal;
    }

    public MessageCategorySpec getActivityCalendarCategory() {
        return getActivityCalendarCategory("");
    }

    /**
     * Create a message to set the meter to a certain time
     *
     * @return a category with one MessageSpec for Timing functionality
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
     * @return a category with one MessageSpec for testing functionality
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
     * @return a category with one MessageSpec for GPRS setup functionality
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
     * @return a category with one messageSpec for testing functionality
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

    public MessageCategorySpec getTestSecurityCategory() {
        MessageCategorySpec catTestMessage = new MessageCategorySpec(
                RtuMessageCategoryConstants.TESTSECURITYMESSAGE);
        MessageSpec msgSpec = addTestMessage(
                RtuMessageKeyIdConstants.TESTSECURITYMESSAGE,
                RtuMessageConstant.TEST_SECURITY_MESSAGE, true);
        catTestMessage.addMessageSpec(msgSpec);
        return catTestMessage;
    }

    /**
     * Creates a message without a value, <b>meterReset</b> must be handled after this
     * message
     *
     * @return a category with one MessageSpec for globalMeterReset functionality
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

    public MessageCategorySpec getRestoreFactorySettings() {
        MessageCategorySpec catGlobalDisc = new MessageCategorySpec(
                RtuMessageCategoryConstants.FACTORYSETTINGS);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.FACTORYSETTINGS,
                RtuMessageConstant.RESTORE_FACTORY_SETTINGS, false);
        catGlobalDisc.addMessageSpec(msgSpec);
        return catGlobalDisc;
    }

    public MessageCategorySpec getDiscoverySettingsCategory() {
        MessageCategorySpec cat = new MessageCategorySpec(RtuMessageCategoryConstants.DISCOVERY_SETTINGS);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.ENABLE_DISCOVERY_ON_POWER_UP,
                RtuMessageConstant.ENABLE_DISCOVERY_ON_POWER_UP, false);
        cat.addMessageSpec(msgSpec);
        MessageSpec msgSpec2 = addNoValueMsg(
                RtuMessageKeyIdConstants.DISABLE_DISCOVERY_ON_POWER_UP,
                RtuMessageConstant.DISABLE_DISCOVERY_ON_POWER_UP, false);
        cat.addMessageSpec(msgSpec2);
        return cat;
    }

    /**
     * Creates a message without a value,
     *
     * @return a category with one MessageSpec for webserverEnable functionality
     */
    public MessageCategorySpec getWebserverCategory() {
        MessageCategorySpec catWebserver = new MessageCategorySpec(RtuMessageCategoryConstants.WEBSERVER);
        catWebserver.addMessageSpec(
                addNoValueMsg(
                    RtuMessageKeyIdConstants.WEBSERVER_DISABLE,
                    RtuMessageConstant.WEBSERVER_DISABLE,
                    false));
        catWebserver.addMessageSpec(
                addNoValueMsg(
                    RtuMessageKeyIdConstants.WEBSERVER_ENABLE,
                    RtuMessageConstant.WEBSERVER_ENABLE,
                        false));
        return catWebserver;
    }

    public MessageCategorySpec getRebootCategory() {
        MessageCategorySpec catReboot = new MessageCategorySpec(
                RtuMessageCategoryConstants.REBOOT);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.REBOOT,
                RtuMessageConstant.REBOOT, false);
        catReboot.addMessageSpec(msgSpec);
        return catReboot;
    }

    /**
     * Create one message to set <b>phoneNumbers to the whiteList</b>
     *
     * @return a category with one MessageSpec for WhiteList functionality
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
     * Create three messages, one to change the <b>globalKey</b>, one to change the
     * <b>AuthenticationKey</b>, and the other one to change
     * the <b>HLSSecret</b>
     *
     * @return a category with four MessageSpecs for Authenticate/Encrypt functionality
     */
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
        catAuthEncrypt.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.CHANGEGLOBALKEY,
                        RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY,
                        false));
        catAuthEncrypt.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.CHANGEAUTHENTICATIONKEY,
                        RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY,
                        false));
        catAuthEncrypt.addMessageSpec(
                addSecurityLevelMsg(
                        RtuMessageKeyIdConstants.ACTIVATE_SECURITY,
                        RtuMessageConstant.AEE_ACTIVATE_SECURITY,
                        true));
        catAuthEncrypt.addMessageSpec(
                addAuthenticationLevelMsg(
                        RtuMessageKeyIdConstants.CHANGE_AUTHENTICATION_LEVEL,
                        RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL,
                        true));
        return catAuthEncrypt;
    }

    /**
     * Create four messages, one to <b>decommission</b> the mbus device, on to set the
     * <b>encryption keys</b> and one to set <b>use corrected mbus values</b> and the last to
     * <b>use UNcorrected mbus values</b>
     * gasLoadProfile
     *
     * @return a category with four messages for Mbus functionality
     */
    public MessageCategorySpec getMbusSetupCategory() {
        MessageCategorySpec catMbusSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.MBUSSETUP);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.MBUSDECOMMISSION,
                RtuMessageConstant.MBUS_DECOMMISSION, false);
        catMbusSetup.addMessageSpec(msgSpec);
        catMbusSetup.addMessageSpec(
                addEncryptionkeys(
                        RtuMessageKeyIdConstants.MBUSENCRYPTIONKEY,
                        RtuMessageConstant.MBUS_ENCRYPTION_KEYS,
                        false));
//		msgSpec = addCorrectSwitchMsg(
//				RtuMessageKeyIdConstants.MBUSGASCORRECTION,
//				RtuMessageConstant.MBUS_CORRECTED_SWITCH, false);
        catMbusSetup.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.MBUSVALUESCORRECTED,
                        RtuMessageConstant.MBUS_CORRECTED_VALUES,
                        false));
        catMbusSetup.addMessageSpec(
                addNoValueMsg(
                        RtuMessageKeyIdConstants.MBUSVALUESUNCORRECTED,
                        RtuMessageConstant.MBUS_UNCORRECTED_VALUES,
                        false));
        return catMbusSetup;
    }

    /**
     * Creates a simpler message to install an MBus device, the only input is the channel ID.
     * Adds a message to reset the MBus client.
     */
    public MessageCategorySpec getSimpleInstallMbusCategory(boolean useSerialNoInClearMBusClientMessage) {
        MessageCategorySpec categorySpec = new MessageCategorySpec(
                RtuMessageCategoryConstants.MBUSSETUP);
        MessageSpec msgSpec = addSimpleMbusInstallMessage(RtuMessageKeyIdConstants.MBUSINSTALL,
                RtuMessageConstant.MBUS_INSTALL, false);
        categorySpec.addMessageSpec(msgSpec);


        MessageSpec msgSpec2 = addResetMBusClientMessage(RtuMessageKeyIdConstants.RESET_MBUS_CLIENT,
                RtuMessageConstant.RESET_MBUS_CLIENT, false, useSerialNoInClearMBusClientMessage);
        categorySpec.addMessageSpec(msgSpec2);

        return categorySpec;
    }

    /**
     * Creates a MessageSpec for specialDays functionality. It contains one field to enter the ID of the codeTable
     * which has the special days configured.
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addSpecialDays(String keyId, String tagName,
                                         boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with no fields to be filled in.
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addNoValueMsg(String keyId, String tagName,
                                        boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to enter the ID of the LookUp table which contains the GroupID's of meters that
     * must be LoadLimit enabled
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addGroupIdsLL(String keyId, String tagName,
                                        boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with several fields to enter to configure the LoadLimiting in the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addConfigureLL(String keyId, String tagName,
                                         boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION, false));
        MessageTagSpec profileTagSpec = new MessageTagSpec("Emergency_Profile");
        profileTagSpec.add(msgVal);
        profileTagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID, false));
        profileTagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME, false));
        profileTagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_EP_DURATION, false));
        tagSpec.add(msgVal);
        tagSpec.add(profileTagSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with the configuration fields for the GPRS connection setup
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addChangeGPRSSetup(String keyId, String tagName,
                                             boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.GPRS_APN, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.GPRS_USERNAME, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.GPRS_PASSWORD, false));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with the credential fields for the GPRS Sim card (only UserName and PassWord)
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return
     */
    protected MessageSpec addGPRSModemCredantials(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.GPRS_USERNAME, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.GPRS_PASSWORD, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add entries in the Loadprofiles of the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addCreateDBEntries(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.ME_START_DATE, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.ME_NUMBER_OF_ENTRIES, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.ME_INTERVAL,true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.ME_SET_CLOCK_BACK, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with a field to set the meter to a certain time
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addTimeMessage(String keyId, String tagName,
                                         boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.SET_TIME_VALUE, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addMbusInstallMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_EQUIPMENT_ID, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_INSTALL_CHANNEL, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_DEFAULT_ENCRYPTION_KEY, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addSimpleMbusInstallMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_INSTALL_CHANNEL, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addResetMBusClientMessage(String keyId, String tagName, boolean advanced, boolean useSerialNoInClearMBusClientMessage) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        if (useSerialNoInClearMBusClientMessage) {
            tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_SERIAL_NUMBER, true));
        } else{
            tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_INSTALL_CHANNEL, true));
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to set several(max. 5) numbers in the WhiteList of the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addPhoneListMsg(String keyId, String tagName,
                                          boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR1, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR2, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR3, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR4, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.WAKEUP_NR5, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add a csv testFile that executes several commands
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addTestMessage(String keyId, String tagName,
                                         boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TEST_FILE, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to connect/disconnect the meter at a certain point in time
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addConnectControl(String keyId, String tagName,
                                            boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to change the disconnect mode of the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addConnectControlMode(String keyId, String tagName,
                                                boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.CONNECT_MODE, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add P1 code to the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addP1Code(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.P1CODE, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add P1 text to the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addP1Text(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.P1TEXT, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add a firmwareUserfile to send to the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addFirmwareMsg(String keyId, String tagName,
                                         boolean advanced) {
        return addFirmwareMsg(keyId, tagName, advanced, false);
    }

    protected MessageSpec addFirmwareMsg(String keyId, String tagName,
                                         boolean advanced, boolean imageIdentifier) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(msgVal);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.FIRMWARE, true));

        /*
           * The Act. Now value is deleted, we use the ActivationDate to check if
           * we need activation now or not. This way it's the same as for example
           * with the disconnector.
           */

        // msgAttrSpec = new
        // MessageAttributeSpec(RtuMessageConstant.FIRMWARE_ACTIVATE_NOW,
        // false);
        // tagSpec.add(msgAttrSpec);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE, false));
        if (imageIdentifier) {
            tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.FIRMWARE_IMAGE_IDENTIFIER, false));
        }
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add one value to the message
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addDefaultValueMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add ActivityCalendar functionality
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addTimeOfUse(String keyId, String tagName,
                                       boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_NAME, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_DATE, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.TOU_ACTIVITY_USER_FILE, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to upgrade the level of security
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addSecurityLevelMsg(String keyId, String tagName,
                                              boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.AEE_SECURITYLEVEL, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to change the level of the authentication
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addAuthenticationLevelMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.AEE_AUTHENTICATIONLEVEL, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to enable encryption over P2 (MBus communication)
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addEncryptionkeys(String keyId, String tagName,
                                            boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_OPEN_KEY, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_TRANSFER_KEY, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with the configuration fields for the SMS center number setup
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addChangeSMSCSetup(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.SMS_SMSC_NUMBER, true));
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with the configuration fields for the SMS center number setup
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addChangeDevicePhoneNumber(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DEVICE_PHONE_NUMBER, true));
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec with a field to set the internal default reset window of the meter
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addChangeDefaultResetWindowMsg(String keyId, String tagName,
                                                         boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    /**
     * Abstract method to define your message categories *
     */
    public abstract List<MessageCategorySpec> getMessageCategories();


    /**
     * Add an openingTag to the Builder
     *
     * @param builder the builder to complete
     * @param tagName the opening TagName
     */
    protected void addOpeningTag(StringBuilder builder, String tagName) {
        builder.append("<");
        builder.append(tagName);
        builder.append(">");
    }

    /**
     * Add a closingTag to the Builder
     *
     * @param builder the builder to complete
     * @param tagName the closing TagName
     */
    protected void addClosingTag(StringBuilder builder, String tagName) {
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
    }
}
