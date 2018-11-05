package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

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
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HS3300Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    protected final DeviceMessageFileExtractor messageFileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private HS3300MessageExecutor messageExecutor;

    public HS3300Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
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
                PLCConfigurationDeviceMessage.WRITE_MAC_TONE_MASK.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_G3_PLC_BANDPLAN.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_7_Parameters.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_ADP_LQI_RANGE.get(propertySpecService, nlsService, converter)
        );
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    protected HS3300MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new HS3300MessageExecutor(getProtocol(), this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

}
