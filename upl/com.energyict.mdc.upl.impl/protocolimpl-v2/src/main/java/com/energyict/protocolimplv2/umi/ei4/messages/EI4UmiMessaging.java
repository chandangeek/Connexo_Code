package com.energyict.protocolimplv2.umi.ei4.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.UmiwanDeviceMessage;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class EI4UmiMessaging implements DeviceMessageSupport {
    public static final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private EI4UmiMessageExecutor messageExecutor;
    private final EI4Umi protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public EI4UmiMessaging(EI4Umi protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    public EI4UmiMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new EI4UmiMessageExecutor(this.protocol, this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                /*LoadProfileMessage.READ_PROFILE_DATA.get(this.propertySpecService, this.nlsService, this.converter),
                LogBookDeviceMessage.ReadLogBook.get(this.propertySpecService, this.nlsService, this.converter),*/
                UmiwanDeviceMessage.WRITE_UMIWAN_CONFIGURATION.get(this.propertySpecService, this.nlsService, this.converter),
                UmiwanDeviceMessage.WRITE_UMIWAN_PROFILE_CONTROL.get(this.propertySpecService, this.nlsService, this.converter),
                UmiwanDeviceMessage.WRITE_UMIWAN_EVENT_CONTROL.get(this.propertySpecService, this.nlsService, this.converter),
                UmiwanDeviceMessage.READ_UMIWAN_STD_STATUS.get(this.propertySpecService, this.nlsService, this.converter),
                UmiwanDeviceMessage.READ_GSM_STD_STATUS.get(this.propertySpecService, this.nlsService, this.converter),

                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(this.propertySpecService, this.nlsService, this.converter)

        );
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
        if (messageAttribute instanceof Date) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }
}
