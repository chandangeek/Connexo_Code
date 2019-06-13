package com.energyict.protocolimplv2.dlms.idis.am122.messages;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.util.List;


public class AM122Messaging extends AM540Messaging{
    public AM122Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }
    @Override
    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM122MessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        super.addSupportedDeviceMessages(supportedMessages);

        supportedMessages = removeNotSupportedDeviceMessages(supportedMessages);

        supportedMessages.add(DeviceActionMessage.BillingDateConfiguration.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        return supportedMessages;
    }

    private List<DeviceMessageSpec> removeNotSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.remove(DeviceActionMessage.BillingResetWithActivationDate); //supported by AM54 but not for AM122

        return supportedMessages;
    }
}
