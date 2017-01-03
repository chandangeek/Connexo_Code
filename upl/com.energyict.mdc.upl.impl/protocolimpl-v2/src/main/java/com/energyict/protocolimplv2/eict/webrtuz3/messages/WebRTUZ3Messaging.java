package com.energyict.protocolimplv2.eict.webrtuz3.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/04/2015 - 13:17
 */
public class WebRTUZ3Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected WebRTUZ3MessageExecutor messageExecutor;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;

    public WebRTUZ3Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, NumberLookupExtractor numberLookupExtractor) {
        super(protocol);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
    }

    protected WebRTUZ3MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new WebRTUZ3MessageExecutor(getProtocol(), this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

    private DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT),
                    this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE),
                    this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT),
                    this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE),
                    this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE),
                    this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE),
                    this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE),
                    this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND),
                    this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME),
                    this.get(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND),
                    this.get(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION),
                    this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY),
                    this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY),
                    this.get(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD),
                    this.get(ClockDeviceMessage.SET_TIME),
                    this.get(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM),
                    this.get(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP),
                    this.get(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS),
                    this.get(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS),
                    this.get(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST),
                    this.get(DeviceActionMessage.GLOBAL_METER_RESET),
                    this.get(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS),
                    this.get(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS),
                    this.get(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION),
                    this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1),
                    this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1),
                    this.get(AdvancedTestMessage.XML_CONFIG));
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(firmwareUpdateActivationDateAttributeName)
                || propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)
                || propertySpec.getName().equals(meterTimeAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return ProtocolTools.getHexStringFromBytes(this.messageFileExtractor.binaryContents((DeviceMessageFile) messageAttribute), "");
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            ActivityCalendarMessage parser = new ActivityCalendarMessage((TariffCalendar) messageAttribute, this.calendarExtractor, null);
            return convertCodeTableToAXDR(parser);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return parseSpecialDays((TariffCalendar) messageAttribute, this.calendarExtractor);
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
            return String.valueOf(((Duration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(emergencyProfileGroupIdListAttributeName)) {
            return convertLookupTable((NumberLookup) messageAttribute, this.numberLookupExtractor);
        }

        return messageAttribute.toString();
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
    }

}