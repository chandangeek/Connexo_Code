package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
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
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DeactivateNTASmsWakeUpMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GlobalMeterReset;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GprsUserCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetTimeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDayTableMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.XmlConfigMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUZ3 protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class WebRTUZ3MessageConverter extends AbstractMessageConverter {

    private final TariffCalendarExtractor tariffCalendarExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public WebRTUZ3MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor tariffCalendarExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter);
        this.tariffCalendarExtractor = tariffCalendarExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorModeAttributeName)
                || propertySpec.getName().equals(activityCalendarNameAttributeName)
                || propertySpec.getName().equals(usernameAttributeName)
                || propertySpec.getName().equals(apnAttributeName)
                || propertySpec.getName().equals(whiteListPhoneNumbersAttributeName)
                || propertySpec.getName().equals(xmlConfigAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); // WebRTU format of the dateTime is milliseconds
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(activityCalendarAttributeName) || propertySpec.getName().equals(specialDaysAttributeName)) {
            return this.tariffCalendarExtractor.id((TariffCalendar) messageAttribute);
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(newEncryptionKeyAttributeName) ||
                propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newPasswordAttributeName) ||
                propertySpec.getName().equals(passwordAttributeName)) {
            return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // contactor related
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new ConnectControlModeMessageEntry(contactorModeAttributeName))
                //TODO: add optional activation date and outputId!

                // firmware upgrade related
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new WebRTUFirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateFileAttributeName))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateFileAttributeName, firmwareUpdateActivationDateAttributeName))

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

                // clock related
                .put(messageSpec(ClockDeviceMessage.SET_TIME), new SetTimeMessageEntry(meterTimeAttributeName))

                // network and connectivity
                .put(messageSpec(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM), new ActivateNTASmsWakeUpMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP), new DeactivateNTASmsWakeUpMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS), new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST), new AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName))

                // Device Actions
                .put(messageSpec(DeviceActionMessage.GLOBAL_METER_RESET), new GlobalMeterReset())

                // Advanced test
                .put(messageSpec(AdvancedTestMessage.XML_CONFIG), new XmlConfigMessageEntry(xmlConfigAttributeName))
                .build();
    }
}
