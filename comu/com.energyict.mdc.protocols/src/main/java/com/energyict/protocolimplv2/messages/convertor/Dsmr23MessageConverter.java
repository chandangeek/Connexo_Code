package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.Password;
import com.elster.jupiter.time.TimeDuration;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.lookups.Lookup;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
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
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.p1InformationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class Dsmr23MessageConverter extends AbstractMessageConverter {

    private static final String TEST_FILE = "Test_File";
    private static final String TEST_MESSAGE = "Test_Message";
    private static final String RESET_ALARM_REGISTER = "Reset_Alarm_Register";
    private static final String DEFAULT_RESET_WINDOW = "Default_Reset_Window";

    private final TopologyService topologyService;

    @Inject
    public Dsmr23MessageConverter(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorModeAttributeName)
                || propertySpec.getName().equals(activityCalendarNameAttributeName)
                || propertySpec.getName().equals(usernameAttributeName)
                || propertySpec.getName().equals(apnAttributeName)
                || propertySpec.getName().equals(whiteListPhoneNumbersAttributeName)
                || propertySpec.getName().equals(p1InformationAttributeName)
                || propertySpec.getName().equals(normalThresholdAttributeName)
                || propertySpec.getName().equals(xmlConfigAttributeName)
                || propertySpec.getName().equals(emergencyThresholdAttributeName)
                || propertySpec.getName().equals(emergencyProfileIdAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); // WebRTU format of the dateTime is milliseconds
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName) || propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return String.valueOf(((Code) messageAttribute).getId());
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(passwordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(emergencyProfileGroupIdListAttributeName)) {
            return String.valueOf(((Lookup) messageAttribute).getId());
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || propertySpec.getName().equals(emergencyProfileDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(loadProfileAttributeName)) {
            return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.topologyService);
        } else if (propertySpec.getName().equals(fromDateAttributeName)
                || propertySpec.getName().equals(toDateAttributeName)) {
            return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        }
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        this.initializeRegistry(registry);
        return registry;
    }

    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        // contactor related
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // firmware upgrade related
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateFileAttributeName, firmwareUpdateActivationDateAttributeName));

        // activity calendar related
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND, new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName, activityCalendarActivationDateAttributeName));
        registry.put(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, new SpecialDayTableMessageEntry(specialDaysCodeTableAttributeName));

        // security related
        registry.put(DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION, new ActivateDlmsEncryptionMessageEntry(encryptionLevelAttributeName));
        registry.put(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL, new ChangeDlmsAuthenticationLevelMessageEntry(authenticationLevelAttributeName));
        registry.put(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY, new ChangeNTADataTransportEncryptionKeyMessageEntry());
        registry.put(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY, new ChangeNTADataTransportAuthenticationKeyMessageEntry());
        registry.put(DeviceMessageId.SECURITY_CHANGE_PASSWORD, new ChangeHLSSecretMessageEntry());

        // network and connectivity
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM, new ActivateNTASmsWakeUpMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP, new DeactivateNTASmsWakeUpMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS, new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST, new AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName));

        // display P1
        registry.put(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1, new ConsumerMessageCodeToPortP1(p1InformationAttributeName));
        registry.put(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1, new ConsumerMessageTextToPortP1(p1InformationAttributeName));

        // Device Actions
        registry.put(DeviceMessageId.DEVICE_ACTIONS_GLOBAL_METER_RESET, new GlobalMeterReset());

        // Load balance
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS, new ConfigureLoadLimitParameters(normalThresholdAttributeName, emergencyThresholdAttributeName, overThresholdDurationAttributeName, emergencyProfileIdAttributeName, emergencyProfileActivationDateAttributeName, emergencyProfileDurationAttributeName));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS, new SetEmergencyProfileGroupIds(emergencyProfileGroupIdListAttributeName));
        registry.put(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION, new ClearLoadLimitConfigurations());

        // Advanced test
        registry.put(DeviceMessageId.ADVANCED_TEST_XML_CONFIG, new XmlConfigMessageEntry(xmlConfigAttributeName));
        registry.put(DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG, new MultipleAttributeMessageEntry(TEST_MESSAGE, TEST_FILE));

        // LoadProfiles
        registry.put(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));

        // clock related
        registry.put(DeviceMessageId.CLOCK_SET_TIME, new SetTimeMessageEntry(meterTimeAttributeName));

        // reset
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW, new MultipleAttributeMessageEntry(DEFAULT_RESET_WINDOW, DEFAULT_RESET_WINDOW));
        registry.put(DeviceMessageId.DEVICE_ACTIONS_ALARM_REGISTER_RESET, new OneTagMessageEntry(RESET_ALARM_REGISTER));
    }

}