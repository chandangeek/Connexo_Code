package com.energyict.protocolimplv2.dlms.idis.am500.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class IDISMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected IDISMessageExecutor messageExecutor;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public IDISMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol);
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    protected IssueFactory getIssueFactory() {
        return issueFactory;
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

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new IDISMessageExecutor(getProtocol(), this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(this.propertySpecService, this.nlsService, this.converter),
                    ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(this.propertySpecService, this.nlsService, this.converter),
                    AlarmConfigurationMessage.RESET_ALL_ALARM_BITS.get(this.propertySpecService, this.nlsService, this.converter),
                    AlarmConfigurationMessage.RESET_ALL_ERROR_BITS.get(this.propertySpecService, this.nlsService, this.converter),
                    AlarmConfigurationMessage.WRITE_ALARM_FILTER.get(this.propertySpecService, this.nlsService, this.converter),
                    GeneralDeviceMessage.WRITE_FULL_CONFIGURATION.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CLOSE_RELAY.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.OPEN_RELAY.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                    ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.propertySpecService, this.nlsService, this.converter),
                    LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS.get(this.propertySpecService, this.nlsService, this.converter),
                    LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR.get(this.propertySpecService, this.nlsService, this.converter),
                    LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1.get(this.propertySpecService, this.nlsService, this.converter),
                    LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2.get(this.propertySpecService, this.nlsService, this.converter),
                    MBusSetupDeviceMessage.Commission.get(this.propertySpecService, this.nlsService, this.converter),
                    PLCConfigurationDeviceMessage.SetTimeoutNotAddressed.get(this.propertySpecService, this.nlsService, this.converter),
                    FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION.get(this.propertySpecService, this.nlsService, this.converter));
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
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            this.calendarExtractor.threadContext().setDevice(offlineDevice);
            this.calendarExtractor.threadContext().setMessage(offlineDeviceMessage);
            return convertCodeTableToXML((TariffCalendar) messageAttribute, this.calendarExtractor);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((TariffCalendar) messageAttribute, this.calendarExtractor);
        } else if (propertySpec.getName().equals(configUserFileAttributeName)
                || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            DeviceMessageFile userFile = (DeviceMessageFile) messageAttribute;
            return ProtocolTools.getHexStringFromBytes(this.messageFileExtractor.binaryContents(userFile), "");  //Bytes of the userFile, as a hex string
        } else if (propertySpec.getName().equals(monitoredValueAttributeName)) {
            return String.valueOf(MonitoredValue.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenUnderThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || (propertySpec.getName().equals(capturePeriodAttributeName))
                || (propertySpec.getName().equals(underThresholdDurationAttributeName))
                || (propertySpec.getName().equals(emergencyProfileDurationAttributeName))) {
            return String.valueOf(((Duration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(((Duration) messageAttribute).getSeconds() / 60);  //Minutes
        }
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

}