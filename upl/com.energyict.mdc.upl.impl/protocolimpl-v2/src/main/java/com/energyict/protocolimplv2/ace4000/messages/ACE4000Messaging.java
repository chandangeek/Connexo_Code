package com.energyict.protocolimplv2.ace4000.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeTableBase64Builder;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CODE_TABLE_ID;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;

public class ACE4000Messaging implements DeviceMessageSupport {

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private ACE4000MessageExecutor messageExecutor;
    private final ACE4000Outbound protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;

    public ACE4000Messaging(ACE4000Outbound protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
    }

    public ACE4000MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new ACE4000MessageExecutor(this.protocol, this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
            //Load Profile messages
            LoadProfileMessage.READ_PROFILE_DATA.get(this.propertySpecService, this.nlsService, this.converter),

            //Events message
            LogBookDeviceMessage.ReadLogBook.get(this.propertySpecService, this.nlsService, this.converter),

            //Configuration messages
            ConfigurationChangeDeviceMessage.SendShortDisplayMessage.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.SendLongDisplayMessage.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ResetDisplayMessage.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureLCDDisplay.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureLoadProfileDataRecording.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureSpecialDataMode.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureMaxDemandSettings.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureConsumptionLimitationsSettings.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureEmergencyConsumptionLimitation.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.ConfigureTariffSettings.get(this.propertySpecService, this.nlsService, this.converter),

            //General messages
            FirmwareDeviceMessage.FirmwareUpgradeWithUrlJarJadFileSize.get(this.propertySpecService, this.nlsService, this.converter),
            ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter),
            ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
            ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
            ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.ACTIVATION_DATE) ||
                propertySpec.getName().equals(DeviceMessageConstants.fromDateAttributeName) ||
                propertySpec.getName().equals(DeviceMessageConstants.toDateAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(CODE_TABLE_ID)) {
            TariffCalendar codeTable = ((TariffCalendar) messageAttribute);
            return CodeTableBase64Builder.getXmlStringFromCodeTable(codeTable, this.calendarExtractor);
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

}