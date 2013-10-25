package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

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
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateUserFileAttributeName, firmwareUpdateActivationDateAttributeName));

        // activity calendar related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName, activityCalendarActivationDateAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND, new SpecialDayTableMessageEntry(activityCalendarCodeTableAttributeName));

        // security related
        registry.put(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION, new ActivateDlmsEncryptionMessageEntry(encryptionLevelAttributeName));
        registry.put(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL, new ChangeDlmsAuthenticationLevelMessageEntry(authenticationLevelAttributeName));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY, new ChangeNTADataTransportEncryptionKeyMessageEntry());
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY, new ChangeNTADataTransportAuthenticationKeyMessageEntry());
        registry.put(SecurityMessage.CHANGE_PASSWORD, new ChangeHLSSecretMessageEntry());

        // network and connectivity
        registry.put(NetworkConnectivityMessage.ACTIVATE_SMS_WAKEUP, new ActivateNTASmsWakeUpMessageEntry());
        registry.put(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP, new DeactivateNTASmsWakeUpMessageEntry());
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS, new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST, new AddPhoneNumbersToWhiteList(whiteListPhoneNumbersAttributeName));

        // display P1
        registry.put(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1, new ConsumerMessageCodeToPortP1(p1InformationAttributeName));
        registry.put(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1, new ConsumerMessageTextToPortP1(p1InformationAttributeName));

        // Device Actions
        registry.put(DeviceActionMessage.GLOBAL_METER_RESET, new GlobalMeterReset());

        // Load balance
        registry.put(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS, new ConfigureLoadLimitParameters(normalThresholdAttributeName, emergencyThresholdAttributeName, overThresholdDurationAttributeName, emergencyProfileIdAttributeName, emergencyProfileActivationDateAttributeName, emergencyProfileDurationAttributeName));
        registry.put(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS, new SetEmergencyProfileGroupIds(emergencyProfileIdLookupAttributeName));
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
        } else if (propertySpec.getName().equals(emergencyProfileIdLookupAttributeName)) {
            return String.valueOf(((Lookup) messageAttribute).getId());
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
