package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.common.CommonCryptoMessaging;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CryptoHS3300Messaging extends HS3300Messaging {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private CommonCryptoMessaging commonCryptoMessaging;

    public CryptoHS3300Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                                 PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                                 TariffCalendarExtractor calendarExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                                 DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter,
                calendarExtractor, certificateWrapperExtractor, messageFileExtractor, keyAccessorTypeExtractor);
        commonCryptoMessaging = new CommonCryptoMessaging(propertySpecService, nlsService, converter, keyAccessorTypeExtractor);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>(super.getSupportedMessages());
        supportedMessages.add(SecurityMessage.AGREE_NEW_ENCRYPTION_KEY.get(propertySpecService, nlsService, converter));
        supportedMessages.add(SecurityMessage.AGREE_NEW_AUTHENTICATION_KEY.get(propertySpecService, nlsService, converter));
        return supportedMessages;
    }

    @Override
    protected HS3300MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new CryptoHS3300MessageExecutor(getProtocol(), getCollectedDataFactory(),
                    getKeyAccessorTypeExtractor(), getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        String formattedString = commonCryptoMessaging.format(offlineDeviceMessage, propertySpec, messageAttribute);
        return formattedString == null ? super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute) : formattedString;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        String context = commonCryptoMessaging.prepareMessageContext(device, deviceMessage);
        return context == null ? super.prepareMessageContext(device, offlineDevice, deviceMessage) : Optional.ofNullable(context);
    }

}
