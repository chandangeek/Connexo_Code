package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
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
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class Dsmr23Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {


    private final AbstractMessageExecutor messageExecutor;
    private List<DeviceMessageSpec> supportedMessages;

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

    public Dsmr23Messaging(AbstractMessageExecutor messageExecutor) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();

            // firmware upgrade related
            supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
            supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);

            // display P1
            supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1);
            supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1);

            // activity calendar related
            supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND);
            supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
            supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);

            // clock related
            supportedMessages.add(ClockDeviceMessage.SET_TIME);

            // Advanced test
            supportedMessages.add(AdvancedTestMessage.XML_CONFIG);

            // security related
            supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
            supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
            supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
            supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
            supportedMessages.add(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD);

            // LoadProfiles
            supportedMessages.add(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
            supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);

            // contactor related
            if (getProtocol().hasBreaker()) {
                supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
                supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
                supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
                supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
                supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
            }

            // Load balance
            if (supportLimiter) {
                supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
                supportedMessages.add(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
                supportedMessages.add(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);
            }

            // Device Actions
            if (supportMeterReset) {
                supportedMessages.add(DeviceActionMessage.GLOBAL_METER_RESET);
            }

            // network and connectivity
            if (supportGPRS) {
                supportedMessages.add(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
                supportedMessages.add(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
                supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
                supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
                supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
            }

            // MBus setup
            if (supportMBus) {
                supportedMessages.add(MBusSetupDeviceMessage.Commission_With_Channel);
            }

            // reset
            supportedMessages.add(DeviceActionMessage.ALARM_REGISTER_RESET);
            if (supportResetWindow) {
                supportedMessages.add(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow);
            }
        }
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case UserFileConfigAttributeName:
            case firmwareUpdateUserFileAttributeName:
                return ProtocolTools.getHexStringFromBytes(((UserFile) messageAttribute).loadFileInByteArray(), "");
            case activityCalendarCodeTableAttributeName:
                return convertCodeTableToXML((Code) messageAttribute);
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            case emergencyProfileGroupIdListAttributeName:
                return convertLookupTable((Lookup) messageAttribute);
            case encryptionLevelAttributeName:
                return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
            case overThresholdDurationAttributeName:
                return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
            case newEncryptionKeyAttributeName:
            case newWrappedEncryptionKeyAttributeName:
            case newPasswordAttributeName:
            case newAuthenticationKeyAttributeName:
            case passwordAttributeName:
            case newWrappedAuthenticationKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case meterTimeAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays(((Code) messageAttribute));
            case loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
            case fromDateAttributeName:
            case toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case contactorActivationDateAttributeName:
            case activityCalendarActivationDateAttributeName:
            case emergencyProfileActivationDateAttributeName:
            case firmwareUpdateActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
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
