package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.Iskra.Mx382;
import com.energyict.protocolimplv2.nta.dsmr23.Iskra.Mx382Cache;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Cache;
import com.energyict.sercurity.KeyRenewalInfo;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.AdministrativeStatusAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DefaultResetWindowAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fullActivityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.keyAccessorTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.mbusChannel;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.p1InformationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * @author sva
 * @since 29/11/13 - 16:04
 */
public class Dsmr23MessageExecutor extends AbstractMessageExecutor {

    public static final String SEPARATOR = ";";
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.x.24.1.0.255");
    public static final int REMOTE_DISCONNECT = 1;
    public static final int REMOTE_RECONNECT = 2;

    protected final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private Dsmr23MbusMessageExecutor mbusMessageExecutor;

    public Dsmr23MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory);
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        for (OfflineDeviceMessage pendingMessage : masterMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            getProtocol().journal("DSMR23 Message executor processing " + pendingMessage.getSpecification().getName());
            try {
                DeviceMessageSpec messageSpecification = pendingMessage.getSpecification();
                if (messageSpecification.equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
                    doDisconnect();
                } else if (messageSpecification.equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, REMOTE_DISCONNECT);
                } else if (messageSpecification.equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
                    doConnect();
                } else if (messageSpecification.equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, REMOTE_RECONNECT);
                } else if (messageSpecification.equals(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE)) {
                    changeControlMode(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE)) {
                    upgradeFirmware(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE)) {
                    upgradeFirmwareWithActivationDate(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (messageSpecification.equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (messageSpecification.equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND)) {
                    String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
                    String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarAttributeName).getValue();
                    activityCalendar(calendarName, activityCalendarContents);
                } else if (messageSpecification.equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME)) {
                    String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
                    String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();
                    String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarAttributeName).getValue();
                    activityCalendarWithActivationDate(calendarName, epoch, activityCalendarContents);
                } else if (messageSpecification.equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_SEND)) {
                    fullActivityCalendar(pendingMessage);
                } else if (messageSpecification.equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME)) {
                    fullActivityCalendarWithActivationDate(pendingMessage);
                } else if (messageSpecification.equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
                    String calendarWithSpecialDays = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, specialDaysAttributeName).getValue();
                    String[] calendarParts = calendarWithSpecialDays.split(AbstractDlmsMessaging.SEPARATOR);
                    if (calendarParts.length > 0) {
                        writeSpecialDays(calendarParts[0]);
                    } else {
                        getProtocol().journal("No content to write special days.");
                    }
                } else if (messageSpecification.equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
                    activateDlmsEncryption(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                    changeAuthLevel(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeEncryptionKey(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthenticationKey(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY)) {
                    changeHLSSecretUsingServiceKey(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY)) {
                    changeAuthenticationKeyUsingServiceKey(pendingMessage, 2);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY)) {
                    changeEncryptionKeyUsingServiceKey(pendingMessage, 0);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD)) {
                    changePassword(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.CHANGE_LLS_SECRET)) {
                    changeLLSSecret(pendingMessage);
                } else if (messageSpecification.equals(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM)) {
                    activateWakeUp();
                } else if (messageSpecification.equals(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP)) {
                    deactivateWakeUp();
                } else if (messageSpecification.equals(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS)) {
                    changeGPRSSettings(pendingMessage);
                } else if (messageSpecification.equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
                    changeGPRSParameters(pendingMessage);
                } else if (messageSpecification.equals(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST)) {
                    addPhoneNumberToWhiteList(pendingMessage);
                } else if (messageSpecification.equals(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1)) {
                    codeToP1(pendingMessage);
                } else if (messageSpecification.equals(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1)) {
                    textToP1(pendingMessage);
                } else if (messageSpecification.equals(DeviceActionMessage.GLOBAL_METER_RESET)) {
                    globalMeterReset();
                } else if (messageSpecification.equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS)) {
                    configureLoadLimitParameters(pendingMessage);
                } else if (messageSpecification.equals(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS)) {
                    setEmergencyProfileGroupIds(pendingMessage);
                } else if (messageSpecification.equals(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION)) {
                    clearLoadLimitConfiguration();
                } else if (messageSpecification.equals(AdvancedTestMessage.XML_CONFIG)) {
                    doXmlConfiguration(pendingMessage);
                } else if (messageSpecification.equals(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST)) {
                    collectedMessage = partialLoadProfileRequest(pendingMessage);    //This message returns a result
                } else if (messageSpecification.equals(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST)) {
                    collectedMessage = loadProfileRegisterRequest(pendingMessage);    //This message returns a result
                } else if (messageSpecification.equals(ClockDeviceMessage.SET_TIME)) {
                    setTime(pendingMessage);
                } else if (messageSpecification.equals(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow)) {
                    changeDefaultResetWindow(pendingMessage);
                } else if (messageSpecification.equals(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus)){
                    changeAdministrativeStatus(pendingMessage);
                } else if (messageSpecification.equals(DeviceActionMessage.ALARM_REGISTER_RESET)) {
                    resetAlarmRegister();
                } else if (messageSpecification.equals(DeviceActionMessage.ERROR_REGISTER_RESET)) {
                    resetErrorRegister();
                } else if (messageSpecification.equals(MBusSetupDeviceMessage.Commission_With_Channel)) {
                    mbusCommission(pendingMessage);
                } else if (messageSpecification.equals(MBusSetupDeviceMessage.Reset_MBus_Client)) {
                    mbusReset(pendingMessage);
                } else if (messageSpecification.equals(SecurityMessage.KEY_RENEWAL)) {
                    renewKey(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                    getProtocol().journal("Message is not supported or configured serial number does not match with device reported serial number.");
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
                getProtocol().journal(Level.SEVERE,"Error while executing message " + pendingMessage.getSpecification().getName()+": " + e.getLocalizedMessage());
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    protected void changeEncryptionKeyUsingServiceKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        throw new IOException("Received message to write the encryption key, but Cryptoserver usage is not supported in this protocol");
    }

    protected void changeAuthenticationKeyUsingServiceKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        throw new IOException("Received message to write the authentication key, but Cryptoserver usage is not supported in this protocol");
    }

    protected void changeHLSSecretUsingServiceKey(OfflineDeviceMessage pendingMessage) throws IOException {
        throw new IOException("Received message to write the password, but Cryptoserver usage is not supported in this protocol");
    }

    private void changeDefaultResetWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        int defaultResetWindow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DefaultResetWindowAttributeName));
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(defaultResetWindow));
    }

    private void changeAdministrativeStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        int status = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, AdministrativeStatusAttributeName));
        getCosemObjectFactory().getData(Dsmr40RegisterFactory.AdministrativeStatusObisCode).setValueAttr(new TypeEnum(status));
    }

    protected void resetAlarmRegister() throws IOException {
        getCosemObjectFactory().getData(Dsmr23RegisterFactory.ALARM_REGISTER).setValueAttr(new Unsigned32(0));
    }

    private void resetErrorRegister() throws IOException {
        getCosemObjectFactory().getData(Dsmr23RegisterFactory.ERROR_REGISTER).setValueAttr(new Unsigned32(0));
    }

    private void setTime(OfflineDeviceMessage pendingMessage) throws IOException {
        Date time = new Date(Long.parseLong(getDeviceMessageAttributeValue(pendingMessage, meterTimeAttributeName)));
        getProtocol().setTime(time);
    }

    private void doXmlConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        String xml = getDeviceMessageAttributeValue(pendingMessage, xmlConfigAttributeName);
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xml));
    }

    private void mbusCommission(OfflineDeviceMessage pendingMessage) throws IOException {
        int installChannel = getIntegerAttribute(pendingMessage, mbusChannel);
        int mBusPhysicalAddress = getMBusPhysicalAddress(installChannel);
        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) (installChannel));
        getProtocol().journal("Installing slave on channel " + installChannel + " using M-Bus Client obis code "+mbusClientObisCode+" and physical address "+mBusPhysicalAddress);
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(mbusClientObisCode, MBusClient.VERSION.VERSION0_D_S_M_R_23_SPEC);
        mbusClient.installSlave(mBusPhysicalAddress);
    }

    protected void mbusReset(OfflineDeviceMessage pendingMessage) throws IOException {
        String mbusSerialNumberAttributeValue = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.mbusSerialNumber);
        MBusClient mbusClient = getMBusClient(mbusSerialNumberAttributeValue);
        mbusClient.setIdentificationNumber(new Unsigned32(0));
        mbusClient.setManufacturerID(new Unsigned16(0));
        mbusClient.setVersion(0);
        mbusClient.setDeviceType(0);
    }



    /**
     * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(final LoadProfileReader loadProfileReader) {
        Date from = new Date(loadProfileReader.getStartReadingTime().getTime() - 5000);
        Date to = new Date(loadProfileReader.getEndReadingTime().getTime() + 5000);
        return new LoadProfileReader(loadProfileReader.getProfileObisCode(), from, to, loadProfileReader.getLoadProfileId(), loadProfileReader.getMeterSerialNumber(), loadProfileReader.getChannelInfos());
    }

    protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getValue();
        String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getValue();

        String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
                "LoadProfileRegister",
                getDefaultDateFormatter().format(new Date(Long.parseLong(fromDateEpoch))),
                loadProfileContent
        );
        Date fromDate = new Date(Long.parseLong(fromDateEpoch));
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = LegacyLoadProfileRegisterMessageBuilder.fromXml(fullLoadProfileContent);
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String errorMessage = "Unable to execute the message, there are no channels attached under LoadProfile " + builder .getProfileObisCode() + "!";
                collectedMessage.setDeviceProtocolInformation(errorMessage);
                collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, errorMessage));
            }

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), builder.getMeterSerialNumber());
            LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, new Date(), lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Collections.singletonList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String errorMessage = "Load profile with obiscode " + config.getObisCode() + " is not supported by the device";
                    collectedMessage.setDeviceProtocolInformation(errorMessage);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMessage));
                    getProtocol().journal(errorMessage);
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Collections.singletonList(fullLpr));

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
                String errorMessage = "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")";
                collectedMessage.setDeviceProtocolInformation(errorMessage);
                collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, errorMessage));
                getProtocol().journal(errorMessage);
                return collectedMessage;
            }

            com.energyict.protocol.Register previousRegister = null;
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (com.energyict.protocol.Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < collectedLoadProfile.getChannelInfo().size(); i++) {
                    final ChannelInfo channel = collectedLoadProfile.getChannelInfo().get(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(intervalDatas.get(i), channel.getUnit()), intervalDatas.getEndTime(), null, intervalDatas.getEndTime(), new Date(), register
                                .getRtuRegisterId());
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
            collectedMessage.setDeviceProtocolInformation(e.getLocalizedMessage());
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            getProtocol().journal(e.getLocalizedMessage());
            return collectedMessage;
        }
    }

    private DateFormat getDefaultDateFormatter() {
        return AbstractMessageConverter.dateTimeFormatWithTimeZone;
    }

    private CollectedMessage partialLoadProfileRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getValue();
            String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getValue();
            String toDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, toDateAttributeName).getValue();
            Date fromDate = new Date(Long.parseLong(fromDateEpoch));
            Date toDate = new Date(Long.parseLong(toDateEpoch));
            SimpleDateFormat formatter = AbstractMessageConverter.dateTimeFormatWithTimeZone;
            String fullLoadProfileContent = LoadProfileMessageUtils.createPartialLoadProfileMessage("PartialLoadProfile", formatter.format(fromDate), formatter.format(toDate), loadProfileContent);

            LegacyPartialLoadProfileMessageBuilder builder = LegacyPartialLoadProfileMessageBuilder.fromXml(fullLoadProfileContent);

            LoadProfileReader lpr = builder.getLoadProfileReader();  //Does not contain the correct from & to date yet, they were stored in separate attributes
            LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, toDate, lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

            fullLpr = checkLoadProfileReader(fullLpr, builder.getMeterSerialNumber());
            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Collections.singletonList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String errorMessage = "Load profile with obiscode " + config.getObisCode() + " is not supported by the device";
                    collectedMessage.setDeviceProtocolInformation(errorMessage);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMessage));
                    getProtocol().journal(errorMessage);
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Collections.singletonList(fullLpr));
            CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, loadProfileData.get(0), fullLpr);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {              //Failed to parse XML data
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getLocalizedMessage());
            getProtocol().journal(e.getLocalizedMessage());
            return collectedMessage;
        }
    }

    /**
     * The Mbus Hourly gasProfile needs to change the B-field in the ObisCode to readout the correct profile. Herefor we use the serialNumber of the Message.
     *
     * @param lpr the reader to change
     * @return the addapted LoadProfileReader
     */
    protected LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
        } else {
            return lpr;
        }
    }

    private void changeEncryptionKey(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] wrappedKey = getWrappedKey(pendingMessage, newEncryptionKeyAttributeName);
        renewKey(wrappedKey, 0);
    }

    private void changeAuthenticationKey(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] wrappedKey = getWrappedKey(pendingMessage, newAuthenticationKeyAttributeName);
        renewKey(wrappedKey, 2);
    }

    protected void renewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String keyAccessorTypeNameAndTempValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, keyAccessorTypeAttributeName).getValue();
        if (keyAccessorTypeNameAndTempValue == null) {
            throw new ProtocolException("The security accessor corresponding to the provided keyAccessorType does not have a valid passive value.");
        }

        String[] values;
        ByteArrayInputStream in = new ByteArrayInputStream(DatatypeConverter.parseHexBinary(keyAccessorTypeNameAndTempValue));
        try {
            values = (String[]) new ObjectInputStream(in).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw DataParseException.generalParseException(e);
        }
        String keyAccessorName = values[0];
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(values[1]);
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");

        Optional<String> securityAttribute = this.keyAccessorTypeExtractor.correspondingSecurityAttribute(
                keyAccessorName,
                getProtocol().getDlmsSessionProperties().getSecurityPropertySet().getName()
        );
        if (securityAttribute.isPresent() && securityAttribute.get().equals(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.getKey())) {
            renewKey(wrappedKey, 2);
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(newSymmetricKey);
        } else if (securityAttribute.isPresent() && securityAttribute.get().equals(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.getKey())) {
            renewKey(wrappedKey, 0);
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newSymmetricKey);
            resetFC();
            resetCachedFC();
        } else {
            throw new ProtocolException("The security accessor corresponding to the provided keyAccessorType is not used as authentication or encryption key in the security setting. Therefore it is not clear which key should be renewed.");
        }
    }

    private void resetFC() {
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        securityContext.setFrameCounter(1);
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
    }

    private void resetCachedFC() {
        DLMSCache dlmsCache = getProtocol().getDeviceCache();
        if (dlmsCache instanceof ESMR50Cache) {
            ((ESMR50Cache) dlmsCache).setFrameCounter(1);
        } else if (dlmsCache instanceof Mx382Cache) {
            ((Mx382Cache) dlmsCache).setFrameCounter(1);
        }
    }

    private void globalMeterReset() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
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
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getValue();
        writeGprsSettings(userName, password);
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getValue();
        writeGprsSettings(userName, password);
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    protected void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        String numbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getValue();
        Array list = new Array();
        for (String number : numbers.split(SEPARATOR)) {
            list.addDataType(OctetString.fromString(number));
        }
        getCosemObjectFactory().getAutoConnect().writeDestinationList(list);
    }

    protected void activateWakeUp() throws IOException {   //Enable the wake up via SMS
        getProtocol().journal("Activating wake-up via SMS");
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    protected void deactivateWakeUp() throws IOException {   //Disable the wake up via SMS
        getProtocol().journal("Disabling wake-up via SMS");
        getCosemObjectFactory().getAutoConnect().writeMode(1);
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

    private void changeLLSSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO: COMMUNICATION-2766. where is the functionality from 8.11 implemented in connexo?
        if (getProtocol().getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();
//            aln.writeSecret(getProtocol().getDlmsSession().getProperties().getSecurityProvider().getNEWHLSSecret());
        } else if (getProtocol().getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();
            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            //asn.changeSecret(newPassword);
        }
    }

    protected void upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getValue();

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        try (final RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {
            it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), false, ImageTransfer.DEFAULT_IMAGE_NAME, false);
        }
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
        Array dateArray = convertEpochToDateTimeArray(activationDate);
        sas.writeExecutionTime(dateArray);
    }

    protected void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateFileAttributeName);
        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {
            ImageTransfer it = getCosemObjectFactory().getImageTransfer();
            it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), true, ImageTransfer.DEFAULT_IMAGE_NAME, false);
            it.imageActivation();
        }

    }

    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();   // Will return empty string if the MessageAttribute could not be found
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName)
                .getValue(); // Will return empty string if the MessageAttribute could not be found

        boolean additionalZeros = false;
        if (getProtocol() instanceof Mx382) {
            additionalZeros = true;
        }

        getProtocol().journal("Using firmware file: "+path);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                getProtocol().journal("Resuming transfer from block: "+lastTransferredBlockNumber);
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        it.setBooleanValue(getBooleanValue());
        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);

        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {
            String actualIdentifier = ImageTransfer.DEFAULT_IMAGE_NAME;
            if (!imageIdentifier.isEmpty()) {
                actualIdentifier = imageIdentifier;
            }
            getProtocol().journal("Starting block transfer of image file using identifier "+actualIdentifier);
            it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), additionalZeros, actualIdentifier, false);
            getProtocol().journal("Block transfer finished");
        }

        if (activationDate.isEmpty()) {
            try {
                getProtocol().journal("Activating immediately");
                it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    getProtocol().journal("Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    getProtocol().journal(Level.WARNING, e.getLocalizedMessage());
                    throw e;
                }
            }
        } else {
            getProtocol().journal("Setting future activation date: "+activationDate);
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertActivationDateEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    /**
     * Convert the given epoch activation date to a proper DateTimeArray
     */
    protected Array convertActivationDateEpochToDateTimeArray(String strDate) {
        return super.convertEpochToDateTimeArray(strDate);
    }

    protected boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }

    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, resumeFirmwareUpdateAttributeName).getValue());
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

    protected void activityCalendar(String calendarName, String activityCalendarContents) throws IOException {
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = newActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        activityCalendarController.writeCalendarActivationTime(null);   //Activate now
    }

    protected void fullActivityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fullActivityCalendarAttributeName).getValue();

        String[] calendarParts = activityCalendarContents.split(AbstractDlmsMessaging.SEPARATOR);
        if (calendarParts.length>0) {
            getProtocol().journal("Sending calendar name:" + calendarName);
            activityCalendar(calendarName, calendarParts[0]);
        } else {
            getProtocol().journal("Skipping calendar because it's empty");
        }

        if (calendarParts.length>1) {
            getProtocol().journal("Sending special days");
            writeSpecialDays(calendarParts[0]);
        } else {
            getProtocol().journal("Skipping special days part because it's empty");
        }

    }

    protected void fullActivityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().journal("Processing full calendar with activation date message");
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fullActivityCalendarAttributeName).getValue();

        String[] calendarParts = activityCalendarContents.split(AbstractDlmsMessaging.SEPARATOR);
        if (calendarParts.length>0) {
            getProtocol().journal("Sending calendar name:" + calendarName + ", activation date=" + epoch);
            activityCalendarWithActivationDate(calendarName, epoch, calendarParts[0]);
        } else {
            getProtocol().journal("Skipping calendar because it's empty");
        }

        if (calendarParts.length>1) {
            getProtocol().journal("Sending special days");
            writeSpecialDays(calendarParts[0]);
        } else {
            getProtocol().journal("Skipping special days part because it's empty");
        }
    }

    protected void writeSpecialDays(String calendarXml) throws IOException {
        ActivityCalendarController activityCalendarController = newActivityCalendarController();
        activityCalendarController.parseContent(calendarXml);
        activityCalendarController.writeSpecialDaysTable();
    }

    protected ActivityCalendarController newActivityCalendarController() {
        return new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone(), false);
    }

    protected void activityCalendarWithActivationDate(String calendarName, String epoch, String activityCalendarContents) throws IOException {
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = newActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        getProtocol().journal("Writing calendar name: "+calendarName);
        activityCalendarController.writeCalendarName(calendarName);
        getProtocol().journal("Writing calendar content");
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        getProtocol().journal("Writing calendar activation date:"+activationCal.getTime().toString());
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    private void changeControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        if (!getProtocol().hasBreaker()) {
            throw new IOException("Cannot write connect mode, breaker is not supported!");
        }
        int controlMode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, contactorModeAttributeName));
        if ((controlMode >= 0) && (controlMode <= 6)) {
            Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
            connectorMode.writeControlMode(new TypeEnum(controlMode));
        } else {
            throw new IOException("Mode is not a valid entry, value must be between 0 and 6");
        }
    }

    private void doTimedControlAction(OfflineDeviceMessage pendingMessage, int action) throws IOException {
        if (!getProtocol().hasBreaker()) {
            throw new IOException("Cannot execute connect/disconnect message, breaker is not supported!");
        }

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
        if (!getProtocol().hasBreaker()) {
            throw new IOException("Cannot execute connect message, breaker is not supported!");
        }
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteDisconnect();
    }

    private void doConnect() throws IOException {
        if (!getProtocol().hasBreaker()) {
            throw new IOException("Cannot execute disconnect message, breaker is not supported!");
        }
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

    protected AbstractMessageExecutor getMbusMessageExecutor() {
        if (this.mbusMessageExecutor == null) {
            this.mbusMessageExecutor = new Dsmr23MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.mbusMessageExecutor;
    }


    protected int getMBusPhysicalAddress(int installChannel) {
        int physicalAddress;
        if (installChannel == 0) {
            physicalAddress = (byte) this.getProtocol().getMeterTopology().searchNextFreePhysicalAddress();
            this.getProtocol().journal("Channel: " + physicalAddress + " will be used as MBUS install channel.");
        } else {
            physicalAddress = installChannel;
        }
        return physicalAddress;
    }

}
