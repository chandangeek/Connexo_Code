package com.energyict.protocolimplv2.dlms.idis.am540.messaging;

import com.energyict.common.CommonCryptoMessaging;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
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
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 10:44
 */
public class CryptoAM540Messaging extends AM540Messaging {

    private CommonCryptoMessaging commonCryptoMessaging;

    public CryptoAM540Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
        commonCryptoMessaging = new CommonCryptoMessaging(propertySpecService, nlsService, converter);
    }

    @Override
    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new CryptoAM540MessageExecutor(getProtocol(), getCollectedDataFactory(), getIssueFactory());
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