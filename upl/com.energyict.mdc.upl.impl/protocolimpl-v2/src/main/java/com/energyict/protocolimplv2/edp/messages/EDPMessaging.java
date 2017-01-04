package com.energyict.protocolimplv2.edp.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PublicLightingDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

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
public class EDPMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final AbstractMessageExecutor messageExecutor;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public EDPMessaging(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, AbstractMessageExecutor messageExecutor, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(messageExecutor.getProtocol());
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageExecutor = messageExecutor;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
    }

    private DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    this.get(ContactorDeviceMessage.CLOSE_RELAY),
                    this.get(ContactorDeviceMessage.OPEN_RELAY),
                    this.get(ContactorDeviceMessage.SET_RELAY_CONTROL_MODE),
                    this.get(PublicLightingDeviceMessage.SET_RELAY_OPERATING_MODE),
                    this.get(PublicLightingDeviceMessage.SET_TIME_SWITCHING_TABLE),
                    this.get(PublicLightingDeviceMessage.SET_THRESHOLD_OVER_CONSUMPTION),
                    this.get(PublicLightingDeviceMessage.SET_OVERALL_MINIMUM_THRESHOLD),
                    this.get(PublicLightingDeviceMessage.SET_OVERALL_MAXIMUM_THRESHOLD),
                    this.get(PublicLightingDeviceMessage.SET_RELAY_TIME_OFFSETS_TABLE),
                    this.get(PublicLightingDeviceMessage.WRITE_GPS_COORDINATES),
                    this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE),
                    this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT),
                    this.get(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME),
                    this.get(DeviceActionMessage.BILLING_RESET),
                    this.get(DeviceActionMessage.BILLING_RESET_CONTRACT_1),
                    this.get(DeviceActionMessage.BILLING_RESET_CONTRACT_2),
                    this.get(DeviceActionMessage.SET_PASSIVE_EOB_DATETIME));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case activityCalendarCodeTableAttributeName:
                EDPActivityCalendarParser parser = new EDPActivityCalendarParser((TariffCalendar) messageAttribute, this.calendarExtractor);
                return convertCodeTableToAXDR(parser);
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays((TariffCalendar) messageAttribute, this.calendarExtractor);
            case configUserFileAttributeName:
            case firmwareUpdateUserFileAttributeName:
                return this.messageFileExtractor.contents((DeviceMessageFile) messageAttribute);
            case activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
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

    public AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }
}
