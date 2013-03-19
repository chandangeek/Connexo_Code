package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivateDlmsEncryptionMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarConfigWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ChangeDlmsAuthenticationLevelMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.FirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.FirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDayTableMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartWebRtuKpMessageConverter extends AbstractMessageConverter {

    private static final String contactorActivationDateAttributeName = "ContactorDeviceMessage.activationdate";
    private static final String contactorModeAttributeName = "ContactorDeviceMessage.changemode.mode";
    private static final String firmwareUpdateActivationDateAttributeName = "FirmwareDeviceMessage.upgrade.activationdate";
    private static final String firmwareUpdateUserFileAttributeName = "FirmwareDeviceMessage.upgrade.userfile";
    private static final String activityCalendarNameAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.name";
    private static final String activityCalendarCodeTableAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.codetable";
    private static final String activityCalendarActivationDateAttributeName = "ActivityCalendarDeviceMessage.activitycalendar.activationdate";
    private static final String encryptionLevelAttributeName = "SecurityMessage.dlmsencryption.encryptionlevel";
    private static final String authenticationLevelAttributeName = "SecurityMessage.dlmsauthentication.authenticationlevel";

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));

        // firmware upgrade related
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUpgradeWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE, new FirmwareUpgradeWithUserFileActivationDateMessageEntry(firmwareUpdateUserFileAttributeName, firmwareUpdateActivationDateAttributeName));

        // activity calendar related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE, new ActivityCalendarConfigWithActivationDateMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName, activityCalendarActivationDateAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND, new SpecialDayTableMessageEntry(activityCalendarCodeTableAttributeName));

        // security related
        registry.put(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION, new ActivateDlmsEncryptionMessageEntry(encryptionLevelAttributeName));
        registry.put(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL, new ChangeDlmsAuthenticationLevelMessageEntry(authenticationLevelAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartWebRtuKpMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); // WebRTU format of the dateTime is milliseconds
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return String.valueOf(((UserFile) messageAttribute).getId());
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return String.valueOf(((Code) messageAttribute).getId());
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        }
        return null;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
