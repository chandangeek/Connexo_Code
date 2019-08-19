package com.energyict.protocolimplv2.dlms.hon.as300n.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.*;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;

public class AS300NMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected AS300NMessageExecutor messageExecutor;
    private CollectedDataFactory collectedDataFactory = null;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    protected final DeviceMessageFileExtractor messageFileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public AS300NMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol);
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
        } else if (propertySpec.getName().equals(activityCalendarAttributeName) || propertySpec.getName().equals(specialDaysAttributeName) || propertySpec.getName().equals(fullActivityCalendarAttributeName)) {
            this.calendarExtractor.threadContext().setDevice(offlineDevice);
            this.calendarExtractor.threadContext().setMessage(offlineDeviceMessage);
            return convertCodeTableToXML((TariffCalendar) messageAttribute, this.calendarExtractor, 0, "0");
        } else if (propertySpec.getName().equals(configUserFileAttributeName)) {
            DeviceMessageFile userFile = (DeviceMessageFile) messageAttribute;
            return ProtocolTools.getHexStringFromBytes(this.messageFileExtractor.binaryContents(userFile), "");  //Bytes of the userFile, as a hex string
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(monitoredValueAttributeName)) {
            return String.valueOf(MonitoredValue.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenUnderThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.actionWhenOverThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || (propertySpec.getName().equals(capturePeriodAttributeName))
                || (propertySpec.getName().equals(underThresholdDurationAttributeName))
                || (propertySpec.getName().equals(emergencyProfileDurationAttributeName))) {
            return String.valueOf(((Duration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(((Duration) messageAttribute).getSeconds() / 60);  //Minutes
        } else if (propertySpec.getName().equals(DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeName)) {
            return (((Date) messageAttribute)).getTime() + "";
        } else if (propertySpec.getName().equals(DeviceMessageConstants.passwordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newMasterKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newPSKAttributeName)) {
            return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
        }
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }


    protected AS300NMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AS300NMessageExecutor(getProtocol(), this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

}

