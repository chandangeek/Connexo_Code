package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 1/26/2016.
 */
public class Mx382Messaging extends AM130Messaging{

    public Mx382Messaging(AbstractDlmsProtocol protocol, Extractor extractor, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(protocol, extractor, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new Mx382MessageExecutor(getProtocol(), getCollectedDataFactory(), getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return addSupportedDeviceMessages(new ArrayList<>());
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addContactorDeviceMessages(supportedMessages);
        addBillingResetDeviceMessages(supportedMessages);
        return supportedMessages;
    }

    private void addBillingResetDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    protected void addContactorDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

}