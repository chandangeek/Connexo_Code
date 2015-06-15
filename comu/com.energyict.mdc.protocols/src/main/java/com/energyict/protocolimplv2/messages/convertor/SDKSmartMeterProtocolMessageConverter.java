package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.lookups.Lookup;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the SDKSmartMeterProtocol protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SDKSmartMeterProtocolMessageConverter extends AbstractMessageConverter {

    private final TopologyService topologyService;

    @Inject
    public SDKSmartMeterProtocolMessageConverter(TopologyService topologyService) {
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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
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
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // contactor related
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new DisconnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // firmware upgrade related
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateFileAttributeName, firmwareUpdateActivationDateAttributeName));

        // activity calendar related
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND, new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName, activityCalendarActivationDateAttributeName));
        registry.put(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, new SpecialDayTableMessageEntry(activityCalendarCodeTableAttributeName));

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

        // LoadProfiles
        registry.put(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));

        // clock related
        registry.put(DeviceMessageId.CLOCK_SET_TIME, new SetTimeMessageEntry(meterTimeAttributeName));
        return registry;
    }

}