package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivateDlmsEncryptionMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivateNTASmsWakeUpMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.AddPhoneNumbersToWhiteListMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ApnCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ChangeDlmsAuthenticationLevelMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ChangeHLSSecretMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ChangeNTADataTransportAuthenticationKeyMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ChangeNTADataTransportEncryptionKeyMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ClearLoadLimitConfigurations;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConfigureLoadLimitParameters;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConsumerMessageCodeToPortP1;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConsumerMessageTextToPortP1;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DeactivateNTASmsWakeUpMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GlobalMeterReset;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GprsUserCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetEmergencyProfileGroupIds;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetTimeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDayTableMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.XmlConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.ChangeMBusClientAttributesEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MBusClientRemoteCommissionEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.google.common.collect.ImmutableMap;

import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.p1InformationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class Dsmr23MessageConverter extends AbstractMessageConverter {

    private static final String TEST_FILE = "Test_File";
    private static final String TEST_MESSAGE = "Test_Message";
    private static final String RESET_ALARM_REGISTER = "Reset_Alarm_Register";
    private static final String DEFAULT_RESET_WINDOW = "Default_Reset_Window";
    private final LoadProfileExtractor loadProfileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final TariffCalendarExtractor calendarExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public Dsmr23MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, NumberLookupExtractor numberLookupExtractor, TariffCalendarExtractor calendarExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter);
        this.loadProfileExtractor = loadProfileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.calendarExtractor = calendarExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime() / 1000); // WebRTU format of the dateTime is seconds
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(activityCalendarAttributeName) || propertySpec.getName().equals(specialDaysAttributeName)) {
            return this.calendarExtractor.id((TariffCalendar) messageAttribute);
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(newEncryptionKeyAttributeName) ||
                propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newPasswordAttributeName) ||
                propertySpec.getName().equals(passwordAttributeName)) {
            return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else if (propertySpec.getName().equals(emergencyProfileGroupIdListAttributeName)) {
            return this.numberLookupExtractor.id((NumberLookup) messageAttribute);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || propertySpec.getName().equals(emergencyProfileDurationAttributeName)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else if (propertySpec.getName().equals(loadProfileAttributeName)) {
            return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
        } else if (propertySpec.getName().equals(fromDateAttributeName)
                || propertySpec.getName().equals(toDateAttributeName)) {
            return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        }
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // contactor related
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE), new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE), new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new ConnectControlModeMessageEntry(contactorModeAttributeName))

                // firmware upgrade related
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new WebRTUFirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateFileAttributeName))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateFileAttributeName, firmwareUpdateActivationDateAttributeName))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateFileAttributeName, firmwareUpdateActivationDateAttributeName, firmwareUpdateImageIdentifierAttributeName))

                // activity calendar related
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND), new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME), new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarAttributeName, activityCalendarActivationDateAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND), new SpecialDayTableMessageEntry(specialDaysAttributeName))

                // security related
                .put(messageSpec(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION), new ActivateDlmsEncryptionMessageEntry(encryptionLevelAttributeName))
                .put(messageSpec(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL), new ChangeDlmsAuthenticationLevelMessageEntry(authenticationLevelAttributeName))
                .put(messageSpec(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY), new ChangeNTADataTransportEncryptionKeyMessageEntry(newEncryptionKeyAttributeName))
                .put(messageSpec(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY), new ChangeNTADataTransportAuthenticationKeyMessageEntry(newAuthenticationKeyAttributeName))
                .put(messageSpec(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD), new ChangeHLSSecretMessageEntry(newPasswordAttributeName))

                // network and connectivity
                .put(messageSpec(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM), new ActivateNTASmsWakeUpMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP), new DeactivateNTASmsWakeUpMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS), new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST), new AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName))

                // display P1
                .put(messageSpec(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1), new ConsumerMessageCodeToPortP1(p1InformationAttributeName))
                .put(messageSpec(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1), new ConsumerMessageTextToPortP1(p1InformationAttributeName))

                // Device Actions
                .put(messageSpec(DeviceActionMessage.GLOBAL_METER_RESET), new GlobalMeterReset())

                // Load balance
                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS), new ConfigureLoadLimitParameters(normalThresholdAttributeName, emergencyThresholdAttributeName, overThresholdDurationAttributeName, emergencyProfileIdAttributeName, emergencyProfileActivationDateAttributeName, emergencyProfileDurationAttributeName))
                .put(messageSpec(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS), new SetEmergencyProfileGroupIds(emergencyProfileGroupIdListAttributeName))
                .put(messageSpec(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION), new ClearLoadLimitConfigurations())

                // Advanced test
                .put(messageSpec(AdvancedTestMessage.XML_CONFIG), new XmlConfigMessageEntry(xmlConfigAttributeName))
                .put(messageSpec(AdvancedTestMessage.USERFILE_CONFIG), new MultipleAttributeMessageEntry(TEST_MESSAGE, TEST_FILE))

                // LoadProfiles
                .put(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName))
                .put(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName))

                // clock related
                .put(messageSpec(ClockDeviceMessage.SET_TIME), new SetTimeMessageEntry(meterTimeAttributeName))

                // reset
                .put(messageSpec(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow), new MultipleAttributeMessageEntry(DEFAULT_RESET_WINDOW, DEFAULT_RESET_WINDOW))
                .put(messageSpec(DeviceActionMessage.ALARM_REGISTER_RESET), new OneTagMessageEntry(RESET_ALARM_REGISTER))

                //MBus setup
                .put(messageSpec(MBusSetupDeviceMessage.Commission_With_Channel), new MultipleAttributeMessageEntry(RtuMessageConstant.MBUS_INSTALL, RtuMessageConstant.MBUS_INSTALL_CHANNEL))
                .put(messageSpec(MBusSetupDeviceMessage.MBusClientRemoteCommission), new MBusClientRemoteCommissionEntry(RtuMessageConstant.MBUS_INSTALL_CHANNEL, RtuMessageConstant.MBUS_SHORT_ID))
                .put(messageSpec(MBusSetupDeviceMessage.ChangeMBusAttributes), new ChangeMBusClientAttributesEntry(RtuMessageConstant.MBUS_INSTALL_CHANNEL, RtuMessageConstant.MBUS_CLIENT_IDENTIFICATION_NUMBER, RtuMessageConstant.MBUS_CLIENT_MANUFACTURER_ID, RtuMessageConstant.MBUS_CLIENT_VERSION, RtuMessageConstant.MBUS_CLIENT_DEVICE_TYPE))
                .build();
    }
}
