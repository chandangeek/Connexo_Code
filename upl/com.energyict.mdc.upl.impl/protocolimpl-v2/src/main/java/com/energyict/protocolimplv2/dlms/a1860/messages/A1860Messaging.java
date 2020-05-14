package com.energyict.protocolimplv2.dlms.a1860.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class A1860Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final LoadProfileExtractor loadProfileExtractor;
    private A1860MessageExecutor messageExecutor;

    public A1860Messaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService,
                          Converter converter, LoadProfileExtractor loadProfileExtractor) {
        super(protocol);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.singletonList(
                LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.get(propertySpecService, nlsService, converter)
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
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  // Epoch (millis)
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
            default:
                return messageAttribute.toString();
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    protected A1860MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new A1860MessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory());
        }
        return messageExecutor;
    }

}
