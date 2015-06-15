package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.WakeUpProtocolSupport;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterDataMessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.AutoConnect;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.TCPUDPSetup;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMX372Properties;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou.ActivityCalendarReader;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou.CosemActivityCalendarBuilder;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/01/12
 * Time: 11:19
 */
public class IskraMx372Messaging extends ProtocolMessages implements WakeUpProtocolSupport {

    private final IskraMx372 protocol;
    private final Clock clock;
    private final TopologyService topologyService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final LoadProfileFactory loadProfileFactory;
    private BaseDevice rtu;

    private ObisCode llsSecretObisCode1 = ObisCode.fromString("0.0.128.100.1.255");
    private ObisCode llsSecretObisCode2 = ObisCode.fromString("0.0.128.100.2.255");
    private ObisCode llsSecretObisCode3 = ObisCode.fromString("0.0.128.100.3.255");
    private ObisCode llsSecretObisCode4 = ObisCode.fromString("0.0.128.100.4.255");
    private ObisCode breakerObisCode = ObisCode.fromString("0.0.128.30.21.255");

    private ObisCode crGroupID = ObisCode.fromString("0.0.128.62.0.255");
    private ObisCode crStartDate = ObisCode.fromString("0.0.128.62.1.255");
    private ObisCode crDuration = ObisCode.fromString("0.0.128.62.2.255");

    private ObisCode crPowerLimit = ObisCode.fromString("0.0.128.62.3.255");
    private ObisCode crMeterGroupID = ObisCode.fromString("0.0.128.62.6.255");
    private ObisCode contractPowerLimit = ObisCode.fromString("0.0.128.61.1.255");

    public static final int MBUS_MAX = 0x04;

    private MbusDevice[] mbusDevices = {null, null, null, null};                // max. 4 MBus meters
    private ObisCode[] mbusPrimaryAddress = {ObisCode.fromString("0.1.128.50.20.255"),
            ObisCode.fromString("0.2.128.50.20.255"),
            ObisCode.fromString("0.3.128.50.20.255"),
            ObisCode.fromString("0.4.128.50.20.255")};
    private ObisCode[] mbusCustomerID = {ObisCode.fromString("0.1.128.50.21.255"),
            ObisCode.fromString("0.2.128.50.21.255"),
            ObisCode.fromString("0.3.128.50.21.255"),
            ObisCode.fromString("0.4.128.50.21.255")};
    private ObisCode[] mbusUnit = {ObisCode.fromString("0.1.128.50.30.255"),
            ObisCode.fromString("0.2.128.50.30.255"),
            ObisCode.fromString("0.3.128.50.30.255"),
            ObisCode.fromString("0.4.128.50.30.255")};
    private ObisCode[] mbusMedium = {ObisCode.fromString("0.1.128.50.23.255"),
            ObisCode.fromString("0.2.128.50.23.255"),
            ObisCode.fromString("0.3.128.50.23.255"),
            ObisCode.fromString("0.4.128.50.23.255")};

    private byte[] connectMsg = new byte[]{AxdrType.UNSIGNED.getTag(), 0x01};
    private byte[] disconnectMsg = new byte[]{AxdrType.UNSIGNED.getTag(), 0x00};
    private byte[] contractPowerLimitMsg = new byte[]{AxdrType.DOUBLE_LONG_UNSIGNED.getTag(), 0, 0, 0, 0};
    private byte[] crPowerLimitMsg = new byte[]{AxdrType.DOUBLE_LONG_UNSIGNED.getTag(), 0, 0, 0, 0};
    private byte[] crDurationMsg = new byte[]{AxdrType.DOUBLE_LONG_UNSIGNED.getTag(), 0, 0, 0, 0};
    private byte[] crMeterGroupIDMsg = new byte[]{AxdrType.LONG_UNSIGNED.getTag(), 0, 0};
    private byte[] crGroupIDMsg = new byte[]{AxdrType.LONG_UNSIGNED.getTag(), 0, 0};

    /**
     * The maximum allowed number of phoneNumbers to make a CSD call to the device
     */
    private static final int maxNumbersCSDWhiteList = 8;
    /**
     * The maximum allowed number of managed calls to be put in the whiteList
     */
    private static final int maxNumbersManagedWhiteList = 8;


    public IskraMx372Messaging(IskraMx372 protocol, Clock clock, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        this.protocol = protocol;
        this.clock = clock;
        this.topologyService = topologyService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.loadProfileFactory = loadProfileFactory;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec catAuthenticationEncryption = getAuthEncryptCategory();
        MessageCategorySpec catBasicMessages = getBasicMessagesCategory();
        MessageCategorySpec catLoadLimit = getLoadLimitCategory();
        MessageCategorySpec catMbus = getMbusCategory();
        MessageCategorySpec catWakeUp = getWakeUpCategory();

        theCategories.add(catAuthenticationEncryption);
        theCategories.add(catBasicMessages);
        theCategories.add(catLoadLimit);
        theCategories.add(catMbus);
        theCategories.add(catWakeUp);
        return theCategories;
    }

    private MessageSpec addMsgWithAttributes(final String description, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, required));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addMsgWithTags(final String description, final boolean advanced, String... tags) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        for (String tag : tags) {
            MessageTagSpec tagSpec = new MessageTagSpec(tag);
            tagSpec.add(new MessageValueSpec());
            msgSpec.add(tagSpec);
        }
        return msgSpec;
    }

    private MessageSpec addMessageWithValue(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Create three messages, one to change the <b>globalKey</b>, one to change the
     * <b>AuthenticationKey</b>, and the other one to change
     * the <b>HLSSecret</b>
     *
     * @return a category with four MessageSpecs for Authenticate/Encrypt functionality
     */
    private MessageCategorySpec getAuthEncryptCategory() {
        MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
        catAuthEncrypt.addMessageSpec(addBasicMsg(RtuMessageKeyIdConstants.CHANGELLSSECRET, RtuMessageConstant.AEE_CHANGE_LLS_SECRET, false));
        return catAuthEncrypt;
    }

    private MessageCategorySpec getBasicMessagesCategory() {
        MessageCategorySpec catBasicMessages = new MessageCategorySpec(RtuMessageCategoryConstants.BASICMESSAGES_DESCRIPTION);
        catBasicMessages.addMessageSpec(addMsgWithAttributes("Change GPRS Modem credentials", RtuMessageConstant.GPRS_MODEM_CREDENTIALS, false, false, RtuMessageConstant.GPRS_USERNAME, RtuMessageConstant.GPRS_PASSWORD));
        catBasicMessages.addMessageSpec(addMsgWithTags("Change GPRS Modem setup", false, RtuMessageConstant.GPRS_APN, RtuMessageConstant.GPRS_USERNAME, RtuMessageConstant.GPRS_PASSWORD));
        catBasicMessages.addMessageSpec(addMsgWithAttributes("Connect meter", RtuMessageConstant.CONNECT_LOAD, false, false));
        catBasicMessages.addMessageSpec(addMessageWithValue("Connect Control mode", RtuMessageConstant.CONNECT_MODE, false));
        catBasicMessages.addMessageSpec(addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false));
        catBasicMessages.addMessageSpec(addMessageWithValue("Set new tariff program", RtuMessageConstant.TOU_SCHEDULE, false));
        return catBasicMessages;
    }

    private MessageCategorySpec getLoadLimitCategory() {
        MessageCategorySpec catLoadLimit = new MessageCategorySpec(RtuMessageCategoryConstants.LOADLIMIT_DESCRIPTION);
        catLoadLimit.addMessageSpec(addMsgWithTags("Apply Load limiting", false, RtuMessageConstant.THRESHOLD_GROUPID, RtuMessageConstant.THRESHOLD_STARTDT, RtuMessageConstant.THRESHOLD_STOPDT));
        catLoadLimit.addMessageSpec(addMessageWithValue(RtuMessageKeyIdConstants.LOADLIMITCLEAR, RtuMessageConstant.CLEAR_THRESHOLD, false));
        catLoadLimit.addMessageSpec(addMsgWithTags(RtuMessageKeyIdConstants.LOADLIMITCONFIG, false, RtuMessageConstant.PARAMETER_GROUPID, RtuMessageConstant.THRESHOLD_POWERLIMIT, RtuMessageConstant.CONTRACT_POWERLIMIT));
        return catLoadLimit;
    }

    private MessageCategorySpec getMbusCategory() {
        MessageCategorySpec catMbus = new MessageCategorySpec(RtuMessageCategoryConstants.MBUSMESSAGES);
        catMbus.addMessageSpec(addBasicMsg("Install MBus device", RtuMessageConstant.MBUS_INSTALL, false));
        catMbus.addMessageSpec(addBasicMsg("Install-DataReadout Mbus device", RtuMessageConstant.MBUS_INSTALL_DATAREADOUT, false));
        catMbus.addMessageSpec(addBasicMsg("Remove MBus device", RtuMessageConstant.MBUS_REMOVE, false));
        return catMbus;
    }

    private MessageCategorySpec getWakeUpCategory() {
        MessageCategorySpec catWakeUp = new MessageCategorySpec(RtuMessageCategoryConstants.WAKEUPFUNCTIONALITY);
        catWakeUp.addMessageSpec(addBasicMsg("Activate wakeup mechanism", RtuMessageConstant.WAKEUP_ACTIVATE, false));
        catWakeUp.addMessageSpec(addMsgWithPhoneNumbers("Add Managed numbers to the white list", RtuMessageConstant.WAKEUP_MANAGED_NR, false));
        catWakeUp.addMessageSpec(addMsgWithPhoneNumbers("Add numbers to white list", RtuMessageConstant.WAKEUP_NR, false));
        catWakeUp.addMessageSpec(addMessageWithValue("Change inactivity timeout", RtuMessageConstant.WAKEUP_INACT_TIMEOUT, false));
        return catWakeUp;
    }

    private MessageCategorySpec getFirmwareCategory() {
        MessageCategorySpec catFirmware = new MessageCategorySpec(RtuMessageCategoryConstants.FIRMWARE);
        catFirmware.addMessageSpec(addMessageWithValue("Upgrade Firmware", RtuMessageConstant.FIRMWARE_CONTENT, false));
        return catFirmware;
    }

    private MessageSpec addMsgWithPhoneNumbers(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec;

        for (int i = 0; i < 8; i++) {
            tagSpec = new MessageTagSpec(tagName + (i + 1));
            tagSpec.add(new MessageValueSpec());
            msgSpec.add(tagSpec);
        }
        return msgSpec;
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, this.loadProfileFactory);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(clock, this.topologyService, this.loadProfileFactory);
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each MessageEntry (see #queryMessage(MessageEntry)) to actually
     * perform the message.
     *
     * @param messageEntries a list of MessageEntrys
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(List messageEntries) throws IOException {
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {

        if (!this.protocol.getSerialNumber().equalsIgnoreCase(messageEntry.getSerialNumber())) {
            IskraMx372MbusMessageExecutor mbusMessageExecutor = new IskraMx372MbusMessageExecutor(protocol, clock, topologyService, loadProfileFactory);
            return mbusMessageExecutor.queryMessage(messageEntry);
        } else {
            MessageResult msgResult = null;
            try {
                if (isItThisMessage(messageEntry, RtuMessageConstant.AEE_CHANGE_LLS_SECRET)) {
                    infoLog("Sending Change_LLS_Secret message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    changeLLSSecret(messageEntry);
                    infoLog("Change_LLS_Secret message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.GPRS_MODEM_CREDENTIALS)) {
                    infoLog("Sending GPRS_modem_credentials message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    changeGprsCredentials(messageEntry);
                    infoLog("GPRS_modem_credentials successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.GPRS_APN) || isItThisMessage(messageEntry, RtuMessageConstant.GPRS_USERNAME) || isItThisMessage(messageEntry, RtuMessageConstant.GPRS_PASSWORD)) {
                    infoLog("Sending GPRS_modem_setup message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    changeGprsSetup(messageEntry);
                    infoLog("GPRS_modem_setup message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.DISCONNECT_LOAD)) {
                    infoLog("Sending disconnectLoad message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    connectDisconnectDevice(messageEntry, false);
                    infoLog("DisconnectLoad message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.CONNECT_LOAD)) {
                    infoLog("Sending connectLoad message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    connectDisconnectDevice(messageEntry, true);
                    infoLog("ConnectLoad message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.CONNECT_MODE)) {
                    infoLog("Sending Connect_control_mode message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    changeConnectorMode(messageEntry);
                    infoLog("Connect_control_mode message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.TOU_SCHEDULE)) {
                    infoLog("Sending SetNewTariffProgram message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    sendActivityCalendar(messageEntry);
                    infoLog("SetNewTariffProgram message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.THRESHOLD_GROUPID) ||
                        isItThisMessage(messageEntry, RtuMessageConstant.THRESHOLD_STARTDT) || isItThisMessage(messageEntry, RtuMessageConstant.THRESHOLD_STOPDT)) {
                    infoLog("Sending ApplyLoadLimiting message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    applyLoadLimit(messageEntry);
                    infoLog("ApplyLoadLimiting message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.CLEAR_THRESHOLD)) {
                    infoLog("Sending ClearLoadLimitConfiguration message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    clearLoadLimit(messageEntry);
                    infoLog("ClearLoadLimitConfiguration message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.PARAMETER_GROUPID)
                        || isItThisMessage(messageEntry, RtuMessageConstant.THRESHOLD_POWERLIMIT) || isItThisMessage(messageEntry, RtuMessageConstant.CONTRACT_POWERLIMIT)) {
                    infoLog("Sending ConfigureLoadLimitParameters message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    configureLoadLimit(messageEntry);
                    infoLog("ConfigureLoadLimitParameters message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.MBUS_INSTALL)) {
                    infoLog("Sending Mbus_Install message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    protocol.getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.128.255"), DLMSClassId.SCRIPT_TABLE.getClassId(), 1).invoke(new Unsigned16(0).getBEREncodedByteArray());
                    infoLog("Mbus_Install message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.MBUS_INSTALL_DATAREADOUT)) {
                    infoLog("Sending Mbus_DataReadout message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    protocol.getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.130.255"), DLMSClassId.SCRIPT_TABLE.getClassId(), 1).invoke(new Unsigned16(0).getBEREncodedByteArray());
                    checkMbusDevices();    // we do this to update the ConcentratorGateway
                    infoLog("Mbus_DataReadout message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.MBUS_REMOVE)) {
                    infoLog("Sending Mbus_Remove message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    protocol.getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.129.255"), DLMSClassId.SCRIPT_TABLE.getClassId(), 1).invoke(new Unsigned16(0).getBEREncodedByteArray());
                    clearMbusGateWays();
                    infoLog("Mbus_Remove message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.WAKEUP_ACTIVATE)) {
                    infoLog("Sending Activate_the_wakeup_mechanism message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    activateWakeUp();
                    infoLog("Activate_the_wakeup_mechanism message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.WAKEUP_INACT_TIMEOUT)) {
                    infoLog("Sending WakeUp_Inactivity_timeout message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    changeWakeUpInactivityTimeOut(messageEntry);
                    infoLog("WakeUp_Inactivity_timeout message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.WAKEUP_MANAGED_NR)) {
                    infoLog("Sending WakeUpAddManagedNumbers message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    addPhoneToManagedList(messageEntry);
                    infoLog("WakeUpAddManagedNumbers message successful.");
                } else if (isItThisMessage(messageEntry, RtuMessageConstant.WAKEUP_NR)) {
                    infoLog("Sending WakeUpAddNumbers message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    addPhoneToWhiteList(messageEntry);
                    infoLog("WakeUpAddNumbers message successful.");
                } else if (isItThisMessage(messageEntry, LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag())) {
                    infoLog("Sending LoadProfileRegister message for meter with serialnumber: " + messageEntry.getSerialNumber());
                    msgResult = doReadLoadProfileRegisters(messageEntry);
                } else if (isItThisMessage(messageEntry, LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag())) {
                    infoLog("Sending PartialLoadProfile message. for meter with serialnumber: " + messageEntry.getSerialNumber());
                    msgResult = doReadPartialLoadProfile(messageEntry);
                } else {
                    msgResult = MessageResult.createFailed(messageEntry, "Message not supported by the protocol.");
                }

                if (msgResult == null) {
                    msgResult = MessageResult.createSuccess(messageEntry);
                } else if (msgResult.isFailed()) {
                    protocol.getLogger().severe("Message failed : " + msgResult.getInfo());
                }
            } catch (Exception e) {
                msgResult = MessageResult.createFailed(messageEntry, e.getMessage());
                protocol.getLogger().severe("Message failed : " + e.getMessage());
            }
            return msgResult;
        }
    }

    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
        return true;
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        protocol.getLogger().info(messageToLog);
    }

    private String getMessageValue(String msgStr, String str) {
        try {
            return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
                    + 1, msgStr.indexOf("</" + str));
        } catch (Exception e) {
            return "";
        }
    }

    private String getMessageAttribute(String messageValue, String attributeName) {
        String result;
        try {
            int ptr = messageValue.indexOf(" " + attributeName);
            if (ptr == -1) {
                throw new IOException("Unable to find the attribute with name '" + attributeName + "'.");
            }
            result = messageValue.substring(ptr + attributeName.length() + 1);
            ptr = result.indexOf('"');
            if (ptr == -1) {
                throw new IOException("Unable to find the opening '\"' for the attribute with name '" + attributeName + "'.");
            }
            result = result.substring(ptr + 1);
            ptr = result.indexOf('"');
            if (ptr == -1) {
                throw new IOException("Unable to find the closing '\"' for the attribute with name '" + attributeName + "'.");
            }
            result = result.substring(0, ptr);
        } catch (Exception e) {
            result = "";
        }
        return result;
    }


    private void changeLLSSecret(MessageEntry messageEntry) throws IOException {
        String newLLSSecret = getMessageAttribute(messageEntry.getContent(), RtuMessageConstant.AEE_LLS_SECRET);
        if (newLLSSecret == null) {
            throw new InvalidPropertyException("Invalid new LLS secret property.");
        } else if (newLLSSecret.length() > 16) {
            throw new InvalidPropertyException("Invalid length of the new LLS secret property, MAX 16 char long.");
        } else {
            try {
                Data authKeyData = protocol.getCosemObjectFactory().getData(llsSecretObisCode4);
                authKeyData.setValueAttr(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(newLLSSecret, "")));
                authKeyData = protocol.getCosemObjectFactory().getData(llsSecretObisCode3);
                authKeyData.setValueAttr(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(newLLSSecret, "")));
                authKeyData = protocol.getCosemObjectFactory().getData(llsSecretObisCode2);
                authKeyData.setValueAttr(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(newLLSSecret, "")));
                authKeyData = protocol.getCosemObjectFactory().getData(llsSecretObisCode1);
                authKeyData.setValueAttr(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(newLLSSecret, "")));
            } catch (IOException e) {
                throw new IOException("Could not write all the necessary LLS keys.");
            }
        }
    }

    private void changeGprsCredentials(MessageEntry messageEntry) throws IOException {
        String description = "Changing gprs credentials for meter with serialnumber: " + getProperties().getSerialNumber();
        try {
            infoLog(description);
            String userName = getMessageAttribute(messageEntry.getContent(), RtuMessageConstant.GPRS_USERNAME);
            String pass = getMessageAttribute(messageEntry.getContent(), RtuMessageConstant.GPRS_PASSWORD);

            PPPSetup.PPPAuthenticationType pppat = protocol.getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
            pppat.setUserName(userName);
            pppat.setPassWord(pass);
            protocol.getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        } catch (IOException e) {
            throw new IOException("IOException while loading the PPPSetup - " + e.getMessage());
        }
    }

    private void changeGprsSetup(MessageEntry messageEntry) throws IOException {
        String description = "Changing apn/username/password for meter with serialnumber: " + getProperties().getSerialNumber();
        try {
            infoLog(description);
            String apn = getMessageValue(messageEntry.getContent(), RtuMessageConstant.GPRS_APN);
            if (apn.equalsIgnoreCase("")) {
                throw new ApplicationException("The APN value is required for message GPRS_modem_setup.");
            }
            String userName = getMessageValue(messageEntry.getContent(), RtuMessageConstant.GPRS_USERNAME);
            String pass = getMessageValue(messageEntry.getContent(), RtuMessageConstant.GPRS_PASSWORD);

            PPPSetup.PPPAuthenticationType pppat = protocol.getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
            pppat.setUserName(userName);
            pppat.setPassWord(pass);

            protocol.getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
            protocol.getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        } catch (IOException e) {
            throw new IOException("IOException while loading the PPPSetup or GPRSModemSetup - " + e.getMessage());
        }
    }

    private void connectDisconnectDevice(MessageEntry messageEntry, boolean connect) throws IOException {
        BigDecimal breakerState;
        if (connect) {
            infoLog("Sending connect message for meter with serialnumber: " + messageEntry.getSerialNumber());
        } else {
            infoLog("Sending disconnect message for meter with serialnumber: " + messageEntry.getSerialNumber());
        }
        try {
            protocol.getCosemObjectFactory().writeObject(breakerObisCode, 1, 2, connect ? connectMsg : disconnectMsg);
            List<Register> list = new ArrayList<Register>();
            list.add(new Register(-1, breakerObisCode, messageEntry.getSerialNumber()));

            breakerState = protocol.readRegisters(list).get(0).getQuantity().getAmount();
        } catch (IOException e) {
            throw new IOException("IOException while writing to/reading the breaker state - " + e.getMessage());
        }
        switch (breakerState.intValue()) {
            case 0: {
                if (messageEntry.getContent().indexOf(RtuMessageConstant.DISCONNECT_LOAD) == -1) {
                    throw new IOException("Invalid breaker state, load is not disconnected.");
                }
            }
            break;

            case 1: {
                if (messageEntry.getContent().indexOf(RtuMessageConstant.CONNECT_LOAD) == -1) {
                    throw new IOException("Invalid breaker state, load is not connected.");
                }
            }
            break;

            default: {
                throw new IOException("Invalid breaker state.");
            }
        }
    }

    private void changeConnectorMode(MessageEntry messageEntry) throws IOException, NumberFormatException {
        String description = "Changing the connectorMode for meter with serialnumber: " + messageEntry.getSerialNumber();
        infoLog(description);
        String mode = getMessageValue(messageEntry.getContent(), RtuMessageConstant.CONNECT_MODE);
        try {
            int iMode = Integer.parseInt(mode);
            Data dataMode = protocol.getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.30.22.255"));
            dataMode.setValueAttr(new Unsigned8(iMode));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The connect control mode must be an integer value.");
        } catch (IOException e) {
            throw new IOException("IOException while setting the new connector mode - " + e.getMessage());
        }
    }

    private void sendActivityCalendar(MessageEntry messageEntry) throws IOException {
        infoLog("Sending new Tariff Program message to meter with serialnumber: " + messageEntry.getSerialNumber());
        UserFile userFile = getUserFile(messageEntry.getContent());
        ActivityCalendar activityCalendar =
                protocol.getCosemObjectFactory().getActivityCalendar(ObisCode.fromString("0.0.13.0.0.255"));

        com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou.ActivityCalendar calendarData =
                new com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou.ActivityCalendar();
        ActivityCalendarReader reader = new IskraActivityCalendarReader(calendarData, protocol.getTimeZone(), getDeviceTimeZone());
        calendarData.setReader(reader);
        calendarData.read(new ByteArrayInputStream(userFile.loadFileInByteArray()));
        CosemActivityCalendarBuilder builder = new
                CosemActivityCalendarBuilder(calendarData);

        activityCalendar.writeCalendarNamePassive(builder.calendarNamePassive());
        activityCalendar.writeSeasonProfilePassive(builder.seasonProfilePassive());
        activityCalendar.writeWeekProfileTablePassive(builder.weekProfileTablePassive());
        activityCalendar.writeDayProfileTablePassive(builder.dayProfileTablePassive());
        if (calendarData.getActivatePassiveCalendarTime() != null) {
            activityCalendar.writeActivatePassiveCalendarTime(builder.activatePassiveCalendarTime());
        } else {
            activityCalendar.activateNow();
        }

        // check if xml file contains special days
        int newSpecialDays = calendarData.getSpecialDays().size();
        if (newSpecialDays > 0) {
            SpecialDaysTable specialDaysTable = protocol.getCosemObjectFactory().getSpecialDaysTable(ObisCode.fromString("0.0.11.0.0.255"));
            // delete old special days
            Array array = specialDaysTable.readSpecialDays();
            int currentMaxSpecialDayIndex = array.nrOfDataTypes();
            for (int i = newSpecialDays; i < currentMaxSpecialDayIndex; i++) {
                calendarData.addDummyDay(i);
            }
            specialDaysTable.writeSpecialDays(builder.specialDays());
        }
    }

    private TimeZone getDeviceTimeZone() throws UnsupportedException {
        throw new UnsupportedException("We need a way to get the Other timeZone from the Device ...");
    }

    protected UserFile getUserFile(String contents) throws IOException {
        throw new UnsupportedException("UserFiles are not supported yet!");
    }

    protected int getTouFileId(String contents) throws IOException {
        int startIndex = 2 + RtuMessageConstant.TOU_SCHEDULE.length();  // <TOU>
        int endIndex = contents.indexOf("</" + RtuMessageConstant.TOU_SCHEDULE + ">");
        String value = contents.substring(startIndex, endIndex);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid userfile id: " + value);
        }
    }

    private void applyLoadLimit(MessageEntry messageEntry) throws BusinessException, SQLException, IOException {
        infoLog("Setting threshold value for meter with serialnumber: " + messageEntry.getSerialNumber());
        String groupID = getMessageValue(messageEntry.getContent(), RtuMessageConstant.THRESHOLD_GROUPID);
        if (groupID.equalsIgnoreCase("")) {
            throw new BusinessException("No groupID was entered.");
        }
        int grID = 0;

        try {
            grID = Integer.parseInt(groupID);
            crGroupIDMsg[1] = (byte) (grID >> 8);
            crGroupIDMsg[2] = (byte) grID;
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid groupID");
        }

        String startDate = "";
        String stopDate = "";
        Calendar startCal = null;
        Calendar stopCal = null;

        startDate = getMessageValue(messageEntry.getContent(), RtuMessageConstant.THRESHOLD_STARTDT);
        stopDate = getMessageValue(messageEntry.getContent(), RtuMessageConstant.THRESHOLD_STOPDT);
        startCal = (startDate.equalsIgnoreCase("")) ? Calendar.getInstance(protocol.getTimeZone()) : getCalendarFromString(startDate);
        if (stopDate.equalsIgnoreCase("")) {
            stopCal = Calendar.getInstance();
            stopCal.setTime(startCal.getTime());
            stopCal.add(Calendar.YEAR, 1);
        } else {
            stopCal = getCalendarFromString(stopDate);
        }

        long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis())) / 1000;
        crDurationMsg[1] = (byte) (crDur >> 24);
        crDurationMsg[2] = (byte) (crDur >> 16);
        crDurationMsg[3] = (byte) (crDur >> 8);
        crDurationMsg[4] = (byte) crDur;
        byte[] byteDate = createByteDate(startCal);

        protocol.getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
        protocol.getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
        protocol.getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);
    }

    private Calendar getCalendarFromString(String strDate) throws IOException {
        try {
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.set(Calendar.DATE, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))));
            cal.set(Calendar.MONTH, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
            cal.set(Calendar.YEAR, Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
            cal.set(Calendar.MINUTE, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
            cal.set(Calendar.SECOND, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
            cal.clear(Calendar.MILLISECOND);
            return cal;
        } catch (NumberFormatException e) {
            throw new IOException("Invalid dateTime format for the applyThreshold message.");
        }
    }

    private byte[] createByteDate(Calendar calendar) {
        byte[] byteStartDateBuffer = new byte[14];

        byteStartDateBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteStartDateBuffer[1] = 12; // length
        byteStartDateBuffer[2] = (byte) (calendar.get(calendar.YEAR) >> 8);
        byteStartDateBuffer[3] = (byte) calendar.get(calendar.YEAR);
        byteStartDateBuffer[4] = (byte) (calendar.get(calendar.MONTH) + 1);
        byteStartDateBuffer[5] = (byte) calendar.get(calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(calendar.DAY_OF_WEEK);
        byteStartDateBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteStartDateBuffer[7] = (byte) calendar.get(calendar.HOUR_OF_DAY);
        byteStartDateBuffer[8] = (byte) calendar.get(calendar.MINUTE);
        byteStartDateBuffer[9] = (byte) calendar.get(calendar.SECOND);
        byteStartDateBuffer[10] = (byte) 0x0; // hundreds of seconds

        byteStartDateBuffer[11] = (byte) (0x80);
        byteStartDateBuffer[12] = (byte) 0;

        if (protocol.getTimeZone().inDaylightTime(calendar.getTime())) {
            byteStartDateBuffer[13] = (byte) 0x80; //0x00;
        } else {
            byteStartDateBuffer[13] = (byte) 0x00; //0x00;
        }

        return byteStartDateBuffer;
    }

    private void clearLoadLimit(MessageEntry messageEntry) throws BusinessException, SQLException, IOException {
        infoLog("Clear threshold for meter with serialnumber: " + messageEntry.getSerialNumber());
        String groupID = getMessageValue(messageEntry.getContent(), RtuMessageConstant.CLEAR_THRESHOLD);
        if (groupID.equalsIgnoreCase("")) {
            throw new BusinessException("No groupID was entered.");
        }
        int grID = 0;

        try {
            grID = Integer.parseInt(groupID);
            crGroupIDMsg[1] = (byte) (grID >> 8);
            crGroupIDMsg[2] = (byte) grID;

        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid groupID");
        }
        Calendar startCal;
        startCal = Calendar.getInstance(protocol.getTimeZone());

        long crDur = 0;
        crDurationMsg[1] = (byte) (crDur >> 24);
        crDurationMsg[2] = (byte) (crDur >> 16);
        crDurationMsg[3] = (byte) (crDur >> 8);
        crDurationMsg[4] = (byte) crDur;
        byte[] byteDate = createByteDate(startCal);

        protocol.getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
        protocol.getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
        protocol.getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);
    }

    private void configureLoadLimit(MessageEntry messageEntry) throws BusinessException, SQLException, IOException {
        infoLog("Sending threshold configuration for meter with serialnumber: " + messageEntry.getSerialNumber());
        String groupID = getMessageValue(messageEntry.getContent(), RtuMessageConstant.PARAMETER_GROUPID);
        if (groupID.equalsIgnoreCase("")) {
            throw new BusinessException("No groupID was entered.");
        }

        String thresholdPL = getMessageValue(messageEntry.getContent(), RtuMessageConstant.THRESHOLD_POWERLIMIT);
        String contractPL = getMessageValue(messageEntry.getContent(), RtuMessageConstant.CONTRACT_POWERLIMIT);
        if ((thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase(""))) {
            throw new BusinessException("Neighter contractual nor threshold limit was given.");
        }

        long conPL = 0;
        long limit = 0;
        int grID = -1;
        try {
            grID = Integer.parseInt(groupID);
            crMeterGroupIDMsg[1] = (byte) (grID >> 8);
            crMeterGroupIDMsg[2] = (byte) grID;

            if (!contractPL.equalsIgnoreCase("")) {
                conPL = Integer.parseInt(contractPL);
                contractPowerLimitMsg[1] = (byte) (conPL >> 24);
                contractPowerLimitMsg[2] = (byte) (conPL >> 16);
                contractPowerLimitMsg[3] = (byte) (conPL >> 8);
                contractPowerLimitMsg[4] = (byte) conPL;
            }
            if (!thresholdPL.equalsIgnoreCase("")) {
                limit = Integer.parseInt(thresholdPL);
                crPowerLimitMsg[1] = (byte) (limit >> 24);
                crPowerLimitMsg[2] = (byte) (limit >> 16);
                crPowerLimitMsg[3] = (byte) (limit >> 8);
                crPowerLimitMsg[4] = (byte) limit;
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid groupID");
        }
        protocol.getCosemObjectFactory().writeObject(crMeterGroupID, 1, 2, crMeterGroupIDMsg);
        if (!contractPL.equalsIgnoreCase("")) {
            protocol.getCosemObjectFactory().writeObject(contractPowerLimit, 3, 2, contractPowerLimitMsg);
        }
        if (!thresholdPL.equalsIgnoreCase("")) {
            protocol.getCosemObjectFactory().writeObject(crPowerLimit, 3, 2, crPowerLimitMsg);
        }
    }

    public MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = getLoadProfileRegisterMessageBuilder();
            builder.fromXml(msgEntry.getContent());
            LoadProfileReader reader = builder.getLoadProfileReader();
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!");
            }

            // The LoadProfileReader loaded from the xml doesn't contain any channelInfo.
            // Here we build up a list of ChannelInfos, based on the list of registers, as each register corresponds to a channel.
            List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
            for (Register register : builder.getRegisters()) {
                channelInfos.add(new ChannelInfo(channelInfos.size(), register.getObisCode().toString(), Unit.getUndefined(), register.getSerialNumber(),
                        this.readingTypeUtilService.getReadingTypeFrom(register.getObisCode(), Unit.getUndefined())));
            }
            LoadProfileReader lpr = constructDateTimeCorrectdLoadProfileReader(
                    new LoadProfileReader(
                            this.clock,
                            reader.getProfileObisCode(),
                            reader.getStartReadingTime(), reader.getEndReadingTime(),
                            reader.getLoadProfileId(),
                            reader.getDeviceIdentifier(),
                            channelInfos,
                            reader.getMeterSerialNumber(),
                            reader.getLoadProfileIdentifier()));

            List<LoadProfileConfiguration> loadProfileConfigurations = this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
            final List<ProfileData> profileDatas = this.protocol.getLoadProfileData(Arrays.asList(lpr));

            if (profileDatas.size() != 1) {
                return MessageResult.createFailed(msgEntry, "We are supposed to receive 1 LoadProfile configuration in this message, but we received " + profileDatas.size());
            }

            ProfileData pd = profileDatas.get(0);
            IntervalData id = null;
            for (IntervalData intervalData : pd.getIntervalDatas()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    id = intervalData;
                }
            }

            if (id == null) {
                return MessageResult.createFailed(msgEntry, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")");
            }

            MeterReadingData mrd = new MeterReadingData();
            for (Register register : builder.getRegisters()) {
                for (int i = 0; i < pd.getChannelInfos().size(); i++) {
                    final ChannelInfo channel = pd.getChannel(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), register.getRegisterSpecId());
                        mrd.add(registerValue);
                    }
                }
            }
            MeterData md = new MeterData();
            md.setMeterReadingData(mrd);

            infoLog("LoadProfileRegister message successful.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, "Could not parse the content of the xml message, probably incorrect message.");
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
        }
    }

    /**
     * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(final LoadProfileReader loadProfileReader) {
        Instant from = loadProfileReader.getStartReadingTime().minus(Duration.ofSeconds(5));
        Instant to = loadProfileReader.getEndReadingTime().plus(Duration.ofSeconds(5));
        return new LoadProfileReader(
                this.clock,
                loadProfileReader.getProfileObisCode(),
                from, to,
                loadProfileReader.getLoadProfileId(),
                loadProfileReader.getDeviceIdentifier(),
                loadProfileReader.getChannelInfos(),
                loadProfileReader.getMeterSerialNumber(),
                loadProfileReader.getLoadProfileIdentifier());
    }

    public MessageResult doReadPartialLoadProfile(final MessageEntry msgEntry) {
        try {
            LegacyPartialLoadProfileMessageBuilder builder = getPartialLoadProfileMessageBuilder();
            builder.fromXml(msgEntry.getContent());

            LoadProfileReader lpr = builder.getLoadProfileReader();
            this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
            final List<ProfileData> profileData = this.protocol.getLoadProfileData(Arrays.asList(lpr));

            if (profileData.isEmpty()) {
                return MessageResult.createFailed(msgEntry, "LoadProfile returned no data.");
            } else {
                for (ProfileData data : profileData) {
                    if (data.getIntervalDatas().isEmpty()) {
                        return MessageResult.createFailed(msgEntry, "LoadProfile returned no interval data.");
                    }
                }
            }
            MeterData md = new MeterData();
            for (ProfileData data : profileData) {
                data.sort();
                md.addProfileData(data);
            }
            infoLog("PartialLoadProfile message successful.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, "Could not parse the content of the xml message, probably incorrect message.");
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
        }
    }

    //*******************************************************************************************
    //    MBUS SECTION
    //*******************************************************************************************/


    public MbusDevice[] getMbusDevices() {
        return mbusDevices;
    }

    public void checkMbusDevices() throws IOException, SQLException, BusinessException {
        // TODO don't do this anymore!
//        String mSerial = "";
//        Device rtu = getRtuFromDatabaseBySerialNumber();
//        if (!((rtu.getDownstreamDevices().size() == 0) && (getProperties().getRtuType() == null))) {
//            for (int i = 0; i < MBUS_MAX; i++) {
//                int mbusAddress = (int) protocol.getCosemObjectFactory().getCosemObject(mbusPrimaryAddress[i]).getValue();
//                if (mbusAddress > 0) {
//                    mSerial = getMbusSerial(mbusCustomerID[i]);
//                    if (!mSerial.equals("")) {
//                        Unit mUnit = getMbusUnit(mbusUnit[i]);
//                        int mMedium = (int) protocol.getCosemObjectFactory().getCosemObject(mbusMedium[i]).getValue();
//                        Device mbusRtu = findOrCreateNewMbusDevice(mSerial);
//                        if (mbusRtu != null) {
//                            mbusDevices[i] = new MbusDevice(mbusAddress, i, mSerial, mMedium, mbusRtu, mUnit, protocol);
//                        } else {
//                            mbusDevices[i] = null;
//                        }
//                    } else {
//                        mbusDevices[i] = null;
//                    }
//                } else {
//                    mbusDevices[i] = null;
//                }
//            }
//        }
//        updateMbusDevices(rtu.getDownstreamDevices());
    }

    private String getMbusSerial(ObisCode oc) throws IOException {
        try {
            String str = "";
            byte[] data = protocol.getCosemObjectFactory().getData(oc).getRawValueAttr();
            byte[] parseStr = new byte[data.length - 2];
            System.arraycopy(data, 2, parseStr, 0, parseStr.length);
            if (ParseUtils.checkIfAllAreChars(parseStr)) {
                str = new String(parseStr);
            } else {
                str = ParseUtils.decimalByteToString(parseStr);
            }
            return str;
        } catch (IOException e) {
            throw new IOException("Could not retrieve the MBus serialNumber");
        }
    }

    private Unit getMbusUnit(ObisCode obisCode) throws IOException {
        try {
            String vifResult = Integer.toString((int) protocol.getCosemObjectFactory().getData(obisCode).getRawValueAttr()[2], 16);
            ValueInformationfieldCoding vif = ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding(Integer.parseInt(vifResult, 16), -1);
            return vif.getUnit();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Could not retrieve the MBus Unit");
        }
    }

    private BaseDevice findOrCreateNewMbusDevice(String customerID) throws SQLException, BusinessException, IOException {
        return null; // TODO Don't do this anymore!
//        List mbusList = mw().getDeviceFactory().findBySerialNumber(customerID);
//        ProtocolTools.closeConnection();
//        if (mbusList.size() == 1) {
//            Device mbusRtu = (Device) mbusList.get(0);
//            // Check if gateway has changed, and update if it has
//            if ((mbusRtu.getGateway() == null) || (mbusRtu.getGateway().getId() != getRtuFromDatabaseBySerialNumber().getId())) {
//                mbusRtu.updateGateway(getRtuFromDatabaseBySerialNumber());
//            }
//            return mbusRtu;
//        }
//        if (mbusList.size() > 1) {
//            String pattern = "Multiple meters where found with serial: {0}.  Data will not be read.";
//            protocol.getLogger().severe(new MessageFormat(pattern).format(new Object[]{customerID}));
//            return null;
//        }
//        DeviceType rtuType = getProperties().getRtuType();
//        if (rtuType == null) {
//            return null;
//        } else {
//            // we don't create any meters anymore!
//            return null;
////            return createMeter(getProperties().getDeviceType(), customerID);
//        }
    }

    // we don't create any meters anymore!
//    private Device createMeter(DeviceType type, String customerID) throws SQLException, BusinessException {
//        DeviceShadow shadow = type.getConfigurations().get(0).newDeviceShadow();
//        Date lastReading = shadow.getLastReading();
//
//        shadow.setName(customerID);
//        shadow.setSerialNumber(customerID);
//
//        String folderExtName = getProperties().getFolderExtName();
//        if (folderExtName != null) {
//            Folder result = mw().getFolderFactory().findByExternalName(folderExtName);
//            ProtocolTools.closeConnection();
//            if (result != null) {
//                shadow.setFolderId(result.getId());
//            } else {
//                infoLog("No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
//            }
//        } else {
//            infoLog("New meter will be placed in prototype folder.");
//        }
//
//        shadow.setGatewayId(getRtuFromDatabaseBySerialNumber().getId());
//        shadow.setLastReading(lastReading);
//        return mw().getDeviceFactory().create(shadow);
//    }

    private void updateMbusDevices(List<BaseDevice> downstreamRtus) throws SQLException, BusinessException {
        Iterator<BaseDevice> it = downstreamRtus.iterator();
        boolean present;
        while (it.hasNext()) {
            BaseDevice mbus = it.next();
            present = false;
            for (int i = 0; i < mbusDevices.length; i++) {
                if (mbusDevices[i] != null) {
                    if (mbus.getSerialNumber().equalsIgnoreCase(mbusDevices[i].getCustomerID())) {
                        present = true;
                        break;
                    }
                }
            }
            if (!present) {
//                mbus.updateGateway(null);
                //TODO don't do this anymore!
            }
        }
    }

    /**
     * NOTE: Updating the gateway of an RTU with NULL is not compatible with EIServer 7.x!!
     *
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred
     */
    private void clearMbusGateWays() throws SQLException, BusinessException {
        // TODO don't do this anymore
//        List slaves = getRtuFromDatabaseBySerialNumber().getDownstreamDevices();
//        Iterator it = slaves.iterator();
//        while (it.hasNext()) {
//            Device slave = (Device) it.next();
//            slave.updateGateway(null);
//        }
    }
    /** END OF MBUS SECTION **/

    //*******************************************************************************************
    //    WAKEUP SECTION
    //*******************************************************************************************/


    /**
     * Set the gsm mode to GSM.
     * Values:
     * - 0 : GSM
     * - 1 : GSM/PPP
     * - 2 : GPRS
     *
     * @throws SQLException
     * @throws BusinessException
     */
    private void activateWakeUp() throws IOException {
        try {
            Unsigned8 gsmMode = new Unsigned8(0);
            protocol.getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.10.255"), 2, 1).write(gsmMode.getBEREncodedByteArray());
        } catch (IOException e) {
            throw new IOException("Could not write the GSM mode");
        }
    }

    private void changeWakeUpInactivityTimeOut(MessageEntry messageEntry) throws IOException, NumberFormatException {
        infoLog("Changing inactivity timeout for meter with serialnumber: " + messageEntry.getSerialNumber());

        String timeout = getMessageValue(messageEntry.getContent(), RtuMessageConstant.WAKEUP_INACT_TIMEOUT);
        if (!ParseUtils.isInteger(timeout)) {
            throw new NumberFormatException("Value for timeout is not a number.");
        } else {
            TCPUDPSetup tcpUdpSetup = protocol.getCosemObjectFactory().getTCPUDPSetup();
            tcpUdpSetup.writeInactivityTimeout(Integer.parseInt(timeout));
        }
    }

    /**
     * Set the Managed numbers for the whitelist to the meter.
     * These numbers are allowed to set up a telnet session
     *
     * @param messageEntry - the message containing the numbers
     * @throws BusinessException if we failed to create an AMR journal entry
     * @throws SQLException      if we failed to create an AMR journal entry
     */
    private void addPhoneToManagedList(MessageEntry messageEntry) throws IOException {
        infoLog("Adding Managed numbers to whitelist for meter with serialnumber: " + messageEntry.getSerialNumber());

        AutoConnect autoConnect = protocol.getCosemObjectFactory().getAutoConnect();
        byte[] restrictions = protocol.getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.20.20.255")).getValueAttr().getOctetString().getOctetStr();
        Array list = protocol.getCosemObjectFactory().getAutoConnect().readDestinationList();    // the list from the meter
        Array newList = new Array();                                                            // the new list

        // copy the CSD numbers to the new list
        for (int i = 0; i < maxNumbersCSDWhiteList; i++) {
            if (i < list.nrOfDataTypes()) {
                newList.addDataType(list.getDataType(i));
            } else {
                newList.addDataType(OctetString.fromString(""));
            }
        }
        int offset = maxNumbersCSDWhiteList; //offset for managed numbers in the restriction list
        for (int i = 0; i < maxNumbersManagedWhiteList; i++) {
            if (!"".equalsIgnoreCase(getMessageValue(messageEntry.getContent(), RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1)))) {
                newList.addDataType(OctetString.fromString(getMessageValue(messageEntry.getContent(), RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1))));
                restrictions[i + offset] = (byte) 0x02;
            } else {
                newList.addDataType(OctetString.fromString(""));
                restrictions[i + offset] = (byte) 0x00;
            }
        }
        autoConnect.writeDestinationList(newList);
        protocol.getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.20.255"), 2, 1).write(OctetString.fromByteArray(restrictions).getBEREncodedByteArray());
    }

    /**
     * Set the numbers from the whitelist to the meter.
     * These numbers are allowed to make a CSD call to the meter
     *
     * @param messageEntry - the message containing the numbers
     * @throws BusinessException if we failed to create an AMR journal entry
     * @throws SQLException      if we failed to create an AMR journal entry
     */
    protected void addPhoneToWhiteList(MessageEntry messageEntry) throws IOException {
        infoLog("Adding numbers to whitelist for meter with serialnumber: " + messageEntry.getSerialNumber());
        AutoConnect autoConnect = protocol.getCosemObjectFactory().getAutoConnect();
        byte[] restrictions = protocol.getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.20.20.255")).getValueAttr().getOctetString().getOctetStr();
        Array list = protocol.getCosemObjectFactory().getAutoConnect().readDestinationList();    // the list from the meter
        Array newList = new Array();                                                    // the new list
        for (int i = 0; i < maxNumbersCSDWhiteList; i++) {
            if (!"".equalsIgnoreCase(getMessageValue(messageEntry.getContent(), RtuMessageConstant.WAKEUP_NR + (i + 1)))) {
                newList.addDataType(OctetString.fromString(getMessageValue(messageEntry.getContent(), RtuMessageConstant.WAKEUP_NR + (i + 1))));
                restrictions[i] = (byte) 0x03;
            } else {
                newList.addDataType(OctetString.fromString(""));
                restrictions[i] = (byte) 0x00;
            }
        }

        for (int i = 0; i < maxNumbersManagedWhiteList; i++) {
            if ((i + maxNumbersCSDWhiteList) <= list.nrOfDataTypes()) {
                newList.addDataType(list.getDataType(i + maxNumbersCSDWhiteList));
            }
        }

        autoConnect.writeDestinationList(newList);
        protocol.getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.20.255"), 2, 1).write(OctetString.fromByteArray(restrictions).getBEREncodedByteArray());
    }

    /**
     * END OF WAKEUP SECTION *
     */

    private IskraMX372Properties getProperties() {
        return ((IskraMX372Properties) protocol.getProperties());
    }
}