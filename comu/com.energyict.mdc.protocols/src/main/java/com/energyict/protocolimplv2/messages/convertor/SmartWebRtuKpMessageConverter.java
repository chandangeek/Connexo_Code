package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ClearLoadLimitConfigurations;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConfigureLoadLimitParameters;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConsumerMessageCodeToPortP1;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConsumerMessageTextToPortP1;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GlobalMeterReset;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetTimeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDayTableMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.XmlConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.p1InformationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartWebRtuKpMessageConverter extends AbstractMessageConverter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    private final SimpleDateFormat dateFormatWithoutTimeZone = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // firmware upgrade related
//        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
//        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE, new FirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateUserFileAttributeName, firmwareUpdateActivationDateAttributeName));

        // activity calendar related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName, activityCalendarActivationDateAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND, new SpecialDayTableMessageEntry(activityCalendarCodeTableAttributeName));

        // security related
//        registry.put(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION, new ActivateDlmsEncryptionMessageEntry(encryptionLevelAttributeName));
//        registry.put(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL, new ChangeDlmsAuthenticationLevelMessageEntry(authenticationLevelAttributeName));
//        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY, new ChangeNTADataTransportEncryptionKeyMessageEntry());
//        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY, new ChangeNTADataTransportAuthenticationKeyMessageEntry());
//        registry.put(SecurityMessage.CHANGE_PASSWORD, new ChangeHLSSecretMessageEntry());

        // network and connectivity
//        registry.put(NetworkConnectivityMessage.ACTIVATE_SMS_WAKEUP, new ActivateNTASmsWakeUpMessageEntry());
//        registry.put(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP, new DeactivateNTASmsWakeUpMessageEntry());
//        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS, new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName));
//        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
//        registry.put(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST, new AddPhoneNumbersToWhiteList(whiteListPhoneNumbersAttributeName));

        // display P1
        registry.put(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1, new ConsumerMessageCodeToPortP1(p1InformationAttributeName));
        registry.put(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1, new ConsumerMessageTextToPortP1(p1InformationAttributeName));

        // Device Actions
        registry.put(DeviceActionMessage.GLOBAL_METER_RESET, new GlobalMeterReset());

        // Load balance
        registry.put(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS, new ConfigureLoadLimitParameters(normalThresholdAttributeName, emergencyThresholdAttributeName, overThresholdDurationAttributeName, emergencyProfileIdAttributeName, emergencyProfileActivationDateAttributeName, emergencyProfileDurationAttributeName));
//        registry.put(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS, new SetEmergencyProfileGroupIds(emergencyProfileIdLookupAttributeName));
        registry.put(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION, new ClearLoadLimitConfigurations());

        // Advanced test
        registry.put(AdvancedTestMessage.XML_CONFIG, new XmlConfigMessageEntry(xmlConfigAttributeName));

        // LoadProfiles
        registry.put(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));

        // clock related
        registry.put(ClockDeviceMessage.SET_TIME, new SetTimeMessageEntry(meterTimeAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartWebRtuKpMessageConverter() {
        super();
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
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return String.valueOf(((UserFile) messageAttribute).getId());
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return String.valueOf(((Code) messageAttribute).getId());
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(passwordAttributeName)) {
            return ((Password) messageAttribute).getValue();
//        } else if (propertySpec.getName().equals(emergencyProfileIdLookupAttributeName)) {
//            return String.valueOf(((Lookup) messageAttribute).getId());
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || propertySpec.getName().equals(emergencyProfileDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(loadProfileAttributeName)) {
            return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
        } else if (propertySpec.getName().equals(fromDateAttributeName)
                || propertySpec.getName().equals(toDateAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateFormatWithoutTimeZone.format((Date) messageAttribute);
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
