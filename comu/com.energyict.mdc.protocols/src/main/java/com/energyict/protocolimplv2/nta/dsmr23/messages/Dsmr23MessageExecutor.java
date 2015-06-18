package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 29/11/13 - 16:04
 */
public class Dsmr23MessageExecutor extends AbstractMessageExecutor {

    public static final String SEPARATOR = ";";
    private static final byte[] DEFAULT_MONITORED_ATTRIBUTE = new byte[]{1, 0, 90, 7, 0, (byte) 255};    // Total current, instantaneous value

    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;

    public Dsmr23MessageExecutor(AbstractDlmsProtocol protocol, Clock clock, TopologyService topologyService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
        this.clock = clock;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            Dsmr23MbusMessageExecutor mbusMessageExecutor = new Dsmr23MbusMessageExecutor(getProtocol(), clock, this.getIssueService(), this.getReadingTypeUtilService(), this.topologyService, this.getCollectedDataFactory(), this.loadProfileFactory);
            mbusMessageExecutor
                    .executePendingMessages(mbusMessages)
                    .getCollectedMessages()
                    .forEach(result::addCollectedMessages);
        }

        for (OfflineDeviceMessage pendingMessage : masterMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN)) {
                    doDisconnect();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, 1);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE)) {
                    doConnect();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, 2);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE)) {
                    changeControlMode(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE)) {
                    upgradeFirmware(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE)) {
                    upgradeFirmwareWithActivationDate(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDER_SEND)) {
                    activityCalendar(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME)) {
                    activityCalendarWithActivationDate(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND)) {
                    writeSpecialDays(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION)) {
                    activateDlmsEncryption(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                    changeAuthLevel(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeEncryptionKey(pendingMessage, 0);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthenticationKey(pendingMessage, 2);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_PASSWORD_WITH_NEW_PASSWORD)) {
                    changePassword(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM)) {
                    activateWakeUp();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP)) {
                    deactivateWakeUp();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS)) {
                    changeGPRSSettings(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS)) {
                    changeGPRSParameters(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST)) {
                    addPhoneNumberToWhiteList(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1)) {
                    codeToP1(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1)) {
                    textToP1(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_GLOBAL_METER_RESET)) {
                    globalMeterReset();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS)) {
                    configureLoadLimitParameters(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS)) {
                    setEmergencyProfileGroupIds(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION)) {
                    clearLoadLimitConfiguration();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ADVANCED_TEST_XML_CONFIG)) {
                    xmlConfiguration(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST)) {
                    collectedMessage = partialLoadProfileRequest(pendingMessage);    //This message returns a result
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST)) {
                    collectedMessage = loadProfileRegisterRequest(pendingMessage);    //This message returns a result
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CLOCK_SET_TIME)) {
                    setTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW)) {
                    changeDefaultResetWindow(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_ALARM_REGISTER_RESET)) {
                    resetAlarmRegister();
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private void changeDefaultResetWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer defaultResetWindow = Integer.valueOf(getDeviceMessageAttributeValue(pendingMessage, DefaultResetWindowAttributeName));
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(defaultResetWindow));
    }

    private void resetAlarmRegister() throws IOException {
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(0));
    }

    private void setTime(OfflineDeviceMessage pendingMessage) throws IOException {
        Date time = new Date(Long.valueOf(getDeviceMessageAttributeValue(pendingMessage, meterTimeAttributeName)));
        getProtocol().setTime(time);
    }

    private void xmlConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        String xml = getDeviceMessageAttributeValue(pendingMessage, xmlConfigAttributeName);
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xml));
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

    private CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
        String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();

        String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
                "LoadProfileRegister",
                getDefaultDateFormatter().format(new Date(Long.parseLong(fromDateEpoch))),
                loadProfileContent
        );
        Instant fromDate = Instant.ofEpochMilli(Long.valueOf(fromDateEpoch));
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, loadProfileFactory);
            builder.fromXml(fullLoadProfileContent);
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(
                        ResultType.ConfigurationMisMatch,
                        createMessageFailedIssue(pendingMessage, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode()));
            }

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), builder.getMeterSerialNumber());
            LoadProfileReader fullLpr =
                    new LoadProfileReader(
                            this.clock,
                            lpr.getProfileObisCode(),
                            fromDate, this.clock.instant(),
                            lpr.getLoadProfileId(),
                            lpr.getDeviceIdentifier(),
                            lpr.getChannelInfos(),
                            lpr.getMeterSerialNumber(),
                            lpr.getLoadProfileIdentifier());

            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(
                            ResultType.NotSupported,
                            createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));

            CollectedLoadProfile collectedLoadProfile = loadProfileData.get(0);
            IntervalData intervalDatas = null;
            for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    intervalDatas = intervalData;
                }
            }

            if (intervalDatas == null) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(
                        ResultType.DataIncomplete,
                        createMessageFailedIssue(pendingMessage, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")"));
                return collectedMessage;
            }

            Register previousRegister = null;
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < collectedLoadProfile.getChannelInfo().size(); i++) {
                    final ChannelInfo channel = collectedLoadProfile.getChannelInfo().get(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(intervalDatas.get(i), channel.getUnit()), intervalDatas.getEndTime(), null, intervalDatas.getEndTime(), new Date(), register.getRegisterSpecId());
                        collectedRegisters.add(createCollectedRegister(registerValue, pendingMessage));
                    }
                }
                previousRegister = register;
            }
            CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    private DateFormat getDefaultDateFormatter() {
        return new SimpleDateFormat();
    }

    private CollectedMessage partialLoadProfileRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
            String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();
            String toDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, toDateAttributeName).getDeviceMessageAttributeValue();
            String fullLoadProfileContent = LoadProfileMessageUtils.createPartialLoadProfileMessage("PartialLoadProfile", "fromDate", "toDate", loadProfileContent);
            Instant fromDate = Instant.ofEpochMilli(Long.valueOf(fromDateEpoch));
            Instant toDate = Instant.ofEpochMilli(Long.valueOf(toDateEpoch));

            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, loadProfileFactory);
            builder.fromXml(fullLoadProfileContent);

            LoadProfileReader lpr = builder.getLoadProfileReader();  //Does not contain the correct from & to date yet, they were stored in separate attributes
            LoadProfileReader fullLpr =
                    new LoadProfileReader(
                            this.clock,
                            lpr.getProfileObisCode(),
                            fromDate, toDate,
                            lpr.getLoadProfileId(),
                            lpr.getDeviceIdentifier(),
                            lpr.getChannelInfos(),
                            lpr.getMeterSerialNumber(),
                            lpr.getLoadProfileIdentifier());

            fullLpr = checkLoadProfileReader(fullLpr, builder.getMeterSerialNumber());
            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(
                            ResultType.NotSupported,
                            createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));
            CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, loadProfileData.get(0));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {              //Failed to parse XML data
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    /**
     * The Mbus Hourly gasProfile needs to change the B-field in the ObisCode to readout the correct profile. Herefor we use the serialNumber of the Message.
     *
     * @param lpr the reader to change
     * @return the addapted LoadProfileReader
     */
    private LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
            return new LoadProfileReader(
                    this.clock,
                    lpr.getProfileObisCode(),
                    lpr.getStartReadingTime(), lpr.getEndReadingTime(),
                    lpr.getLoadProfileId(),
                    lpr.getDeviceIdentifier(),
                    lpr.getChannelInfos(),
                    serialNumber,
                    lpr.getLoadProfileIdentifier());
        } else {
            return lpr;
        }
    }

    private void changeEncryptionKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        byte[] key = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName), "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void changeAuthenticationKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        byte[] key = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName), "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void globalMeterReset() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
    }

    private void setEmergencyProfileGroupIds(OfflineDeviceMessage pendingMessage) throws IOException {
        String[] groupIds = getDeviceMessageAttributeValue(pendingMessage, emergencyProfileGroupIdListAttributeName).split(SEPARATOR);
        Array idArray = new Array();
        for (String groupId : groupIds) {
            idArray.addDataType(new Unsigned16(Integer.valueOf(groupId)));

        }
        getCosemObjectFactory().getLimiter().writeEmergencyProfileGroupIdList(idArray);
    }

    // first do it the Iskra way, if it fails do it our way
    private void clearLoadLimitConfiguration() throws IOException {
        Limiter clearLLimiter = getCosemObjectFactory().getLimiter();
        Structure emptyStruct = new Structure();
        emptyStruct.addDataType(new Unsigned16(0));
        emptyStruct.addDataType(OctetString.fromByteArray(new byte[14]));
        emptyStruct.addDataType(new Unsigned32(0));
        try {
            clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
        } catch (DataAccessResultException e) {
            if (e.getDataAccessResult() == DataAccessResultCode.TYPE_UNMATCHED.getResultCode()) {
                emptyStruct = new Structure();
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
            } else {
                throw e;
            }
        }
    }

    private void configureLoadLimitParameters(OfflineDeviceMessage pendingMessage) throws IOException {

        String normalThreshold = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getDeviceMessageAttributeValue();
        String emergencyThreshold = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyThresholdAttributeName).getDeviceMessageAttributeValue();
        String overThresholdDuration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileId = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileIdAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileActivationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileActivationDateAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileDuration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileDurationAttributeName).getDeviceMessageAttributeValue();

        byte theMonitoredAttributeType = -1;
        Limiter loadLimiter = getCosemObjectFactory().getLimiter();

        if (theMonitoredAttributeType == -1) {    // check for the type of the monitored value
            Limiter.ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
            if (valueDefinitionType.getClassId().getValue() == 0) {
                setMonitoredValue(loadLimiter);
                valueDefinitionType = loadLimiter.readMonitoredValue();
            }
            theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
        }

        // Write the normalThreshold
        if (normalThreshold != null) {
            loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, normalThreshold));
        }

        // Write the emergencyThreshold
        if (emergencyThreshold != null) {
            loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, emergencyThreshold));
        }

        // Write the minimumOverThresholdDuration
        if (overThresholdDuration != null) {
            loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(overThresholdDuration)));
        }

        // Construct the emergencyProfile
        Structure emergencyProfile = new Structure();
        if (emergencyProfileId != null) {    // The EmergencyProfileID
            emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(emergencyProfileId)));
        }
        if (emergencyProfileActivationDate != null) {    // The EmergencyProfileActivationTime
            emergencyProfile.addDataType(new OctetString(convertUnixToGMTDateTime(emergencyProfileActivationDate).getBEREncodedByteArray(), 0, true));
        }
        if (emergencyProfileDuration != null) {        // The EmergencyProfileDuration
            emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(emergencyProfileDuration)));
        }
        if ((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)) {    // If all three elements are correct, then send it, otherwise throw error
            throw new ProtocolException("The complete emergecy profile must be filled in before sending it to the meter.");
        } else {
            if (emergencyProfile.nrOfDataTypes() > 0) {
                loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
            }
        }
    }

    /**
     * Convert a given epoch timestamp in SECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in seconds sinds 1th jan 1970 00:00:00
     * @return the AXDRDateTime of the given time
     */
    public AXDRDateTime convertUnixToGMTDateTime(String time) {
        return convertUnixToDateTime(time, TimeZone.getTimeZone("GMT"));
    }

    public AXDRDateTime convertUnixToDateTime(String time, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(Long.parseLong(time) * 1000);
        return new AXDRDateTime(cal);
    }

    /**
     * Convert the value to write to the Limiter object to the correct monitored value type ...
     */
    protected AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws ProtocolException {

        final AxdrType axdrType = AxdrType.fromTag(theMonitoredAttributeType);
        switch (axdrType) {
            case NULL: {
                return new NullData();
            }
            case BOOLEAN: {
                return new BooleanObject("1".equalsIgnoreCase(value));
            }
            case BIT_STRING: {
                return new BitString(Integer.parseInt(value));
            }
            case DOUBLE_LONG: {
                return new Integer32(Integer.parseInt(value));
            }
            case DOUBLE_LONG_UNSIGNED: {
                return new Unsigned32(Integer.parseInt(value));
            }
            case OCTET_STRING: {
                return OctetString.fromString(value);
            }
            case VISIBLE_STRING: {
                return new VisibleString(value);
            }
            case INTEGER: {
                return new Integer8(Integer.parseInt(value));
            }
            case LONG: {
                return new Integer16(Integer.parseInt(value));
            }
            case UNSIGNED: {
                return new Unsigned8(Integer.parseInt(value));
            }
            case LONG_UNSIGNED: {
                return new Unsigned16(Integer.parseInt(value));
            }
            case LONG64: {
                return new Integer64(Integer.parseInt(value));
            }
            case ENUM: {
                return new TypeEnum(Integer.parseInt(value));
            }
            default:
                throw new ProtocolException("convertToMonitoredtype error, unknown type.");
        }
    }

    private byte getMonitoredAttributeType(Limiter.ValueDefinitionType vdt) throws IOException {

        if (getMeterConfig().getClassId(vdt.getObisCode()) == com.energyict.dlms.cosem.Register.CLASSID) {
            return getCosemObjectFactory().getRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == ExtendedRegister.CLASSID) {
            return getCosemObjectFactory().getExtendedRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return getCosemObjectFactory().getDemandRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == Data.CLASSID) {
            return getCosemObjectFactory().getData(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else {
            throw new ProtocolException("WebRtuKP, getMonitoredAttributeType, invalid classID " + getMeterConfig().getClassId(vdt.getObisCode()) + " for obisCode " + vdt.getObisCode().toString());
        }
    }

    private void setMonitoredValue(Limiter loadLimiter) throws IOException {
        Limiter.ValueDefinitionType vdt = loadLimiter.new ValueDefinitionType();
        vdt.addDataType(new Unsigned16(3));
        OctetString os = OctetString.fromByteArray(DEFAULT_MONITORED_ATTRIBUTE);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        loadLimiter.writeMonitoredValue(vdt);
    }

    private void codeToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getDeviceMessageAttributeValue(pendingMessage, p1InformationAttributeName)));
    }

    private void textToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getDeviceMessageAttributeValue(pendingMessage, p1InformationAttributeName)));

    }

    private void changeGPRSSettings(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        String numbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getDeviceMessageAttributeValue();
        Array list = new Array();
        for (String number : numbers.split(SEPARATOR)) {
            list.addDataType(OctetString.fromString(number));
        }
        getCosemObjectFactory().getAutoConnect().writeDestinationList(list);
    }

    private void activateWakeUp() throws IOException {   //Enable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    private void writeGprsSettings(String userName, String password) throws IOException {
        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (userName != null) {
            pppat.setUserName(userName);
        }
        if (password != null) {
            pppat.setPassWord(password);
        }
        if ((userName != null) || (password != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }
    }

    private void deactivateWakeUp() throws IOException {   //Disable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(1);
    }

    private void changePassword(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] newPassword = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newPasswordAttributeName), "");
        if (getProtocol().getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            aln.changeHLSSecret(newPassword);
        } else if (getProtocol().getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            asn.changeSecret(newPassword);
        }
    }

    private void upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(ProtocolTools.getBytesFromHexString(userFile, ""));
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
        Array dateArray = convertEpochToDateTimeArray(activationDate);
        sas.writeExecutionTime(dateArray);
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeValue = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateFileAttributeName);
        byte[] image = ProtocolTools.getBytesFromHexString(attributeValue, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(image);
        it.imageActivation();
    }


    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        int level = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, encryptionLevelAttributeName));
        getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(level));
    }

    private void changeAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, authenticationLevelAttributeName));
        AssociationLN aln = getCosemObjectFactory().getAssociationLN();
        AbstractDataType adt = aln.readAuthenticationMechanismName();
        if (adt.isOctetString()) {
            byte[] octets = ((OctetString) adt).getOctetStr();
            if (octets[octets.length - 1] != newAuthLevel) {
                octets[octets.length - 1] = (byte) newAuthLevel;
                aln.writeAuthenticationMechanismName(new OctetString(octets, 0));
            }
        } else if (adt.isStructure()) {
            Structure struct = (Structure) adt;
            Unsigned8 u8 = (Unsigned8) struct.getDataType(struct.nrOfDataTypes() - 1);
            if (u8.intValue() != newAuthLevel) {
                u8 = new Unsigned8(newAuthLevel);
                struct.setDataType(struct.nrOfDataTypes() - 1, u8);
                aln.writeAuthenticationMechanismName(struct);
            }
        } else {
            throw new ProtocolException("Returned AuthenticationMechanismName is not of the type OctetString, nor Structure.");
        }
    }

    private void activityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        activityCalendarController.writeCalendarActivationTime(null);   //Activate now
    }

    private void writeSpecialDays(OfflineDeviceMessage pendingMessage) throws IOException {
        String specialDayArrayBEREncodedBytes = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        Array sdArray = AXDRDecoder.decode(ProtocolTools.getBytesFromHexString(specialDayArrayBEREncodedBytes, ""), Array.class);
        SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());

        if (sdArray.nrOfDataTypes() != 0) {
            sdt.writeSpecialDays(sdArray);
        }
    }

    private void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    private void changeControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int controlMode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, contactorModeAttributeName));
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
        connectorMode.writeControlMode(new TypeEnum(controlMode));
    }

    private void doTimedControlAction(OfflineDeviceMessage pendingMessage, int action) throws IOException {
        Array executionTimeArray = convertEpochToDateTimeArray(getDeviceMessageAttributeValue(pendingMessage, contactorActivationDateAttributeName));
        SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(action));    // method '1' is the 'remote_disconnect' method , '2' is the 'remote_connect' method.

        sasDisconnect.writeExecutedScript(scriptStruct);
        sasDisconnect.writeExecutionTime(executionTimeArray);
    }

    private void doDisconnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteDisconnect();
    }

    private void doConnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteReconnect();
    }

    /**
     * Extract all OfflineDeviceMessages who belong to the master device
     *
     * @param deviceMessages a List of all OfflineDeviceMessages
     * @return the list of OfflineDeviceMessages who belong to the master device
     */
    protected List<OfflineDeviceMessage> getMessagesOfMaster(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> messages = new ArrayList<>();

        for (OfflineDeviceMessage pendingMessage : deviceMessages) {
            if (getProtocol().getSerialNumber().equals(pendingMessage.getDeviceSerialNumber())) {
                messages.add(pendingMessage);
            }
        }
        return messages;
    }

    /**
     * Extract all OfflineDeviceMessages who belong to a MBus slave device
     *
     * @param deviceMessages a List of all OfflineDeviceMessages
     * @return the list of OfflineDeviceMessages who belong to a MBus slave device
     */
    protected List<OfflineDeviceMessage> getMbusMessages(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> mbusMessages = new ArrayList<>();

        for (OfflineDeviceMessage pendingMessage : deviceMessages) {
            if (!getProtocol().getSerialNumber().equals(pendingMessage.getDeviceSerialNumber())) {
                mbusMessages.add(pendingMessage);
            }
        }
        return mbusMessages;
    }

    public Clock getClock() {
        return clock;
    }

    public TopologyService getTopologyService() {
        return topologyService;
    }

    public LoadProfileFactory getLoadProfileFactory() {
        return loadProfileFactory;
    }
}