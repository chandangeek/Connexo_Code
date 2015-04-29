package com.energyict.protocolimplv2.eict.webrtuz3.messages;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/04/2015 - 13:17
 */
public class WebRTUZ3Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    static {
        supportedMessages = new ArrayList<>();

        // contactor related
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

        // firmware upgrade related
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);

        // activity calendar related
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND);
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);

        // security related
        supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD);

        // clock related
        supportedMessages.add(ClockDeviceMessage.SET_TIME);

        // network and connectivity
        supportedMessages.add(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        supportedMessages.add(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);

        // Device Actions
        supportedMessages.add(DeviceActionMessage.GLOBAL_METER_RESET);

        // Load limiter
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
        supportedMessages.add(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
        supportedMessages.add(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);

        // display P1
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1);
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1);

        // Advanced test
        supportedMessages.add(AdvancedTestMessage.XML_CONFIG);
    }

    protected WebRTUZ3MessageExecutor messageExecutor;

    public WebRTUZ3Messaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected WebRTUZ3MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new WebRTUZ3MessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return getMessageExecutor().updateSentMessages(offlineDeviceMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)
                || propertySpec.getName().equals(meterTimeAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return ProtocolTools.getHexStringFromBytes(((UserFile) messageAttribute).loadFileInByteArray(), "");
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            ActivityCalendarMessage parser = new ActivityCalendarMessage((Code) messageAttribute, null);
            return convertCodeTableToAXDR(parser);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return parseSpecialDays((Code) messageAttribute);
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(newEncryptionKeyAttributeName) ||
                propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newPasswordAttributeName) ||
                propertySpec.getName().equals(passwordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(emergencyProfileGroupIdListAttributeName)) {
            return convertLookupTable((Lookup) messageAttribute);
        }

        return messageAttribute.toString();
    }
}