package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

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
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

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

    public Dsmr23Messaging(AbstractMessageExecutor messageExecutor, Extractor extractor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messageExecutor.getProtocol(), extractor);
        this.messageExecutor = messageExecutor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }

    protected DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>();
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE));
        supportedMessages.add(this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1));
        supportedMessages.add(this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND));
        supportedMessages.add(this.get(ClockDeviceMessage.SET_TIME));
        supportedMessages.add(this.get(AdvancedTestMessage.XML_CONFIG));
        supportedMessages.add(this.get(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD));
        supportedMessages.add(this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        supportedMessages.add(this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));

        // contactor related
        if (getProtocol().hasBreaker()) {
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
            supportedMessages.add(this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));
        }

        // Load balance
        if (supportLimiter) {
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS));
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS));
            supportedMessages.add(this.get(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION));
        }

        // Device Actions
        if (supportMeterReset) {
            supportedMessages.add(this.get(DeviceActionMessage.GLOBAL_METER_RESET));
        }

        // network and connectivity
        if (supportGPRS) {
            supportedMessages.add(this.get(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM));
            supportedMessages.add(this.get(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP));
            supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS));
            supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS));
            supportedMessages.add(this.get(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST));
        }

        // MBus setup
        if (supportMBus) {
            supportedMessages.add(this.get(MBusSetupDeviceMessage.Commission_With_Channel));
            supportedMessages.add(this.get(MBusSetupDeviceMessage.MBusClientRemoteCommission));
            supportedMessages.add(this.get(MBusSetupDeviceMessage.ChangeMBusAttributes));
        }

        // reset
        supportedMessages.add(this.get(DeviceActionMessage.ALARM_REGISTER_RESET));
        if (supportResetWindow) {
            supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow));
        }
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
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
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
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