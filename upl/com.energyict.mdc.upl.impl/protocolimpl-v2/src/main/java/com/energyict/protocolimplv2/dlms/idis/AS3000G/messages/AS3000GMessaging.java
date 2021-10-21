package com.energyict.protocolimplv2.dlms.idis.AS3000G.messages;

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
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.List;

public class AS3000GMessaging extends AM540Messaging {

    public AS3000GMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AS3000GMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));


        return supportedMessages;
    }

}
