package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fullActivityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.keyAccessorTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class Dsmr23Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final AbstractMessageExecutor messageExecutor;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final TariffCalendarExtractor calendarExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    /**
     * Boolean indicating whether or not to show the MBus related messages in EIServer
     */
    protected boolean supportMBus = true;

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
    protected boolean supportLimiter = true;

    /**
     * Boolean indicating whether or not to show the message to reset the alarm window in EIServer
     */
    protected boolean supportResetWindow = true;

    public Dsmr23Messaging(AbstractMessageExecutor messageExecutor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.calendarExtractor = calendarExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected NumberLookupExtractor getNumberLookupExtractor() {
        return numberLookupExtractor;
    }

    protected LoadProfileExtractor getLoadProfileExtractor() {
        return loadProfileExtractor;
    }

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    protected DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>();
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1));
        supportedMessages.add(this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_SEND));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND));
        supportedMessages.add(this.get(ClockDeviceMessage.SET_TIME));
        supportedMessages.add(this.get(AdvancedTestMessage.XML_CONFIG));
        supportedMessages.add(this.get(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD));
        supportedMessages.add(this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        supportedMessages.add(this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
        supportedMessages.add(this.get(SecurityMessage.KEY_RENEWAL));

        // contactor related
        if (getProtocol().hasBreaker()) {
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));
        }

        // Load balance
        if (supportLimiter) {
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS));
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS));
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION));
        }

        // Device Actions
        if (supportMeterReset) {
            supportedMessages.add(this.get(DeviceActionMessage.GLOBAL_METER_RESET));
        }

        // network and connectivity
        if (supportGPRS) {
            supportedMessages.add(this.get(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM));
            supportedMessages.add(this.get(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP));
            supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS));
            supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS));
            supportedMessages.add(this.get(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST));
        }

        // MBus setup
        if (supportMBus) {
            supportedMessages.add(this.get(MBusSetupDeviceMessage.Commission_With_Channel));
            supportedMessages.add(this.get(MBusSetupDeviceMessage.MBusClientRemoteCommission));
            supportedMessages.add(this.get(MBusSetupDeviceMessage.ChangeMBusAttributes));
        }

        // reset
        supportedMessages.add(this.get(DeviceActionMessage.ALARM_REGISTER_RESET));
        if (supportResetWindow) {
            supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow));
        }
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case UserFileConfigAttributeName:
                return ProtocolTools.getHexStringFromBytes(this.messageFileExtractor.binaryContents((DeviceMessageFile) messageAttribute), "");
            case firmwareUpdateFileAttributeName:
                return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
            case activityCalendarAttributeName: {
                this.calendarExtractor.threadContext().setDevice(offlineDevice);
                this.calendarExtractor.threadContext().setMessage(offlineDeviceMessage);
                return convertCodeTableToXML((TariffCalendar) messageAttribute, this.calendarExtractor, 0, "0");
            }
            case fullActivityCalendarAttributeName: {
                this.calendarExtractor.threadContext().setDevice(offlineDevice);
                this.calendarExtractor.threadContext().setMessage(offlineDeviceMessage);
                String activityCalendar = convertCodeTableToXML((TariffCalendar) messageAttribute, this.calendarExtractor, 0, "0");
                String specialDays = parseSpecialDays((TariffCalendar) messageAttribute, this.calendarExtractor);
                return activityCalendar + SEPARATOR + specialDays;
            }
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            case emergencyProfileGroupIdListAttributeName:
                return convertLookupTable((NumberLookup) messageAttribute, this.numberLookupExtractor);
            case encryptionLevelAttributeName:
                return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
            case overThresholdDurationAttributeName:
                return String.valueOf(((Duration) messageAttribute).getSeconds());
            case newEncryptionKeyAttributeName:
            case newPasswordAttributeName:
            case newAuthenticationKeyAttributeName:
            case passwordAttributeName:
                return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
            case meterTimeAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case specialDaysAttributeName:
                return parseSpecialDays((TariffCalendar) messageAttribute, this.calendarExtractor);
            case loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
            case fromDateAttributeName:
            case toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case contactorActivationDateAttributeName:
            case activityCalendarActivationDateAttributeName:
            case emergencyProfileActivationDateAttributeName:
            case firmwareUpdateActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            case keyAccessorTypeAttributeName:
                return convertKeyAccessorType((KeyAccessorType) messageAttribute, this.keyAccessorTypeExtractor);

            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    private String convertKeyAccessorType(KeyAccessorType messageAttribute, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        String[] values = new String[]{keyAccessorTypeExtractor.name(messageAttribute), this.keyAccessorTypeExtractor.passiveValueContent(messageAttribute)};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(values);
            return DatatypeConverter.printHexBinary(out.toByteArray());
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
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

    public void setSupportLimiter(boolean supportsLimiter) {
        this.supportLimiter = supportsLimiter;
    }

    public void setSupportResetWindow(boolean supportResetWindow) {
        this.supportResetWindow = supportResetWindow;
    }

    protected AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }

}