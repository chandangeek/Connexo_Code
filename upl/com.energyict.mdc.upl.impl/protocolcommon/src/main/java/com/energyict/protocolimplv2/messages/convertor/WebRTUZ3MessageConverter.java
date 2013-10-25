package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy WebRTUZ3 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class WebRTUZ3MessageConverter extends AbstractMessageConverter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
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

        // clock related
        registry.put(ClockDeviceMessage.SET_TIME, new SetTimeMessageEntry(meterTimeAttributeName));

        // network and connectivity
        registry.put(NetworkConnectivityMessage.ACTIVATE_SMS_WAKEUP, new ActivateNTASmsWakeUpMessageEntry());
        registry.put(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP, new DeactivateNTASmsWakeUpMessageEntry());
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS, new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST, new AddPhoneNumbersToWhiteList(whiteListPhoneNumbersAttributeName));

        // Device Actions
        registry.put(DeviceActionMessage.GLOBAL_METER_RESET, new GlobalMeterReset());

        // Advanced test
        registry.put(AdvancedTestMessage.XML_CONFIG, new XmlConfigMessageEntry(xmlConfigAttributeName));

    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public WebRTUZ3MessageConverter() {
        super();
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
        } else if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
