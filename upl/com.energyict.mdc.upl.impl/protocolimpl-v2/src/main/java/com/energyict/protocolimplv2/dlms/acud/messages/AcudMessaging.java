package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AcudMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final NlsService nlsService;
    private final Converter converter;
    private final PropertySpecService propertySpecService;
    private final TariffCalendarExtractor calendarExtractor;
    protected final DeviceMessageFileExtractor messageFileExtractor;
    private AcudMessageExecutor messageExecutor;

    public AcudMessaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol);
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                CreditDeviceMessage.UPDATE_MONEY_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                CreditDeviceMessage.UPDATE_CONSUMPTION_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                CreditDeviceMessage.UPDATE_TIME_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                CreditDeviceMessage.UPDATE_CREDIT_AMOUNT.get(this.propertySpecService, this.nlsService, this.converter),
                CreditDeviceMessage.UPDATE_CREDIT_DAYS_LIMIT.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.ACTIVATE_PASSIVE_UNIT_CHARGE.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.UPDATE_UNIT_CHARGE.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.SWITCH_CHARGE_MODE.get(getPropertySpecService(), getNlsService(), getConverter()),
                ChargeDeviceMessage.CHANGE_CHARGE_PERIOD.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.SWITCH_TAX_AND_STEP_TARIFF.get(getPropertySpecService(), getNlsService(), getConverter()),
                ChargeDeviceMessage.CHANGE_STEP_TARIFF.get(getPropertySpecService(), getNlsService(), getConverter()),
                ChargeDeviceMessage.CHANGE_TAX_RATES.get(getPropertySpecService(), getNlsService(), getConverter()),
                ChargeDeviceMessage.CHANGE_CHARGE_PROPORTION.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.UPDATE_UNIT_CHARGE.get(this.propertySpecService, this.nlsService, this.converter),
                ChargeDeviceMessage.FRIENDLY_DAY_PERIOD_UPDATE.get(getPropertySpecService(), getNlsService(), getConverter()),
                ChargeDeviceMessage.FRIENDLY_WEEKDAYS_UPDATE.get(getPropertySpecService(), getNlsService(), getConverter()),
                ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(getPropertySpecService(), getNlsService(), getConverter()),
                ConfigurationChangeDeviceMessage.SPECIAL_DAY_CSV_STRING.get(getPropertySpecService(), getNlsService(), getConverter()),
                ConfigurationChangeDeviceMessage.WRITE_CONFIGURATION_TEXT.get(getPropertySpecService(), getNlsService(), getConverter()),
                ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.activationDate))
            return String.valueOf(((Date) messageAttribute).getTime());
        if (propertySpec.getName().equals(DeviceMessageConstants.specialDaysAttributeName))
            return parseSpecialDays((TariffCalendar) messageAttribute, this.calendarExtractor);
        if (propertySpec.getName().equals(DeviceMessageConstants.passiveUnitChargeActivationTime))
            return String.valueOf(((Date) messageAttribute).getTime());
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName))
            return String.valueOf(((Date) messageAttribute).getTime());
        if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName))
            return messageAttribute.toString();
        return messageAttribute.toString();
    }

    protected AcudMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = createMessageExecutor();
        }
        return messageExecutor;
    }

    protected AcudMessageExecutor createMessageExecutor() {
        return new AcudMessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory());
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    public Converter getConverter() {
        return converter;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
}