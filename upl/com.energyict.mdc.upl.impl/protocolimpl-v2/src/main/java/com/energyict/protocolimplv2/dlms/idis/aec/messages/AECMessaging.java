package com.energyict.protocolimplv2.dlms.idis.aec.messages;

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
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.List;

public class AECMessaging extends AM540Messaging {
    public AECMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AECMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ClockDeviceMessage.SetDST.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        return supportedMessages;
    }
}
