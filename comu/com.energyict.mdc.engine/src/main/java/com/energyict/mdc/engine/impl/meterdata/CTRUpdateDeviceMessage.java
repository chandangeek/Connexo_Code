package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceMessage;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import java.util.Optional;

/**
 * @author sva
 * @since 4/07/13 - 10:01
 */
public class CTRUpdateDeviceMessage extends UpdateDeviceMessage {

    public CTRUpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(messageAcknowledgement, comTaskExecution, serviceProvider);
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        Optional<OfflineDeviceMessage> deviceMessage = comServerDAO.findOfflineDeviceMessage(getMessageIdentifier());
        if (deviceMessage.isPresent()) {
           this.doExecute(comServerDAO, deviceMessage.get());
        } else {
            addIssue(CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(this, MessageSeeds.UNKNOWN_DEVICE_MESSAGE, getMessageIdentifier())
            );
        }
    }

    private void doExecute(ComServerDAO comServerDAO, OfflineDeviceMessage deviceMessage) {
        String messageProtocolInfo = deviceMessage.getProtocolInfo();
        Object protocolInfo = getMessageIdentifier().forIntrospection().getValue("protocolInfo");
        String wdb = ((String[]) protocolInfo)[1];
        if (messageProtocolInfo.split(",").length > 1) {
            // Protocol info contains multiple sms ids - we cannot set the message successful yet (we should wait until all SMSes are received)
            // instead, we update the protocol info (remove the id of the confirmed sms)
            messageProtocolInfo = messageProtocolInfo.replace(" " + wdb + ",", "");
            messageProtocolInfo = messageProtocolInfo.replace(", " + wdb, "");

            comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), DeviceMessageStatus.SENT, this.getClock().instant(), messageProtocolInfo);
        } else {
            comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), getDeviceMessageStatus(), this.getClock().instant(), getProtocolInfo());
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Update device message";
    }

}