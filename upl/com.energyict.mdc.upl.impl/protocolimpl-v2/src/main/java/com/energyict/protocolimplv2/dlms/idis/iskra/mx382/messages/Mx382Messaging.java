package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;
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

    public Mx382Messaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new Mx382MessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            addSupportedDeviceMessages(supportedMessages);
        }
        return supportedMessages;
    }

    @Override
    protected void addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addContactorDeviceMessages(supportedMessages);
        addBillingResetDeviceMessages(supportedMessages);
    }

    private void addBillingResetDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(DeviceActionMessage.BILLING_RESET);
    }

    protected void addContactorDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
    }
}
