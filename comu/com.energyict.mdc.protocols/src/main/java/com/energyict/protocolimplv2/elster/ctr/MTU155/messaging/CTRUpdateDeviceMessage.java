package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.comserver.commands.UpdateDeviceMessage;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.issues.WarningImpl;
import com.energyict.mdc.journal.CompletionCode;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierByDeviceAndProtocolInfoParts;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdw.offline.OfflineDeviceMessage;

/**
 * @author sva
 * @since 4/07/13 - 10:01
 */
public class CTRUpdateDeviceMessage extends UpdateDeviceMessage {

    public CTRUpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement, ComTaskExecution comTaskExecution) {
        super(messageAcknowledgement, comTaskExecution);
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        OfflineDeviceMessage deviceMessage = comServerDAO.findOfflineDeviceMessage(getMessageIdentifier());
        if (deviceMessage != null) {
            String messageProtocolInfo = deviceMessage.getProtocolInfo();
            String wdb = ((DeviceMessageIdentifierByDeviceAndProtocolInfoParts) getMessageIdentifier()).getMessageProtocolInfoParts()[1];

            if (messageProtocolInfo.split(",").length > 1) {
                // Protocol info contains multiple sms ids - we cannot set the message successful yet (we should wait until all smses are received)
                // instead, we update the protocol info (remove the id of the confirmed sms)
                messageProtocolInfo = messageProtocolInfo.replace(" " + wdb + ",", "");
                messageProtocolInfo = messageProtocolInfo.replace(", " + wdb, "");

                comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), DeviceMessageStatus.SENT, messageProtocolInfo);
            } else {
                comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), getDeviceMessageStatus(), getProtocolInfo());
            }
        } else {
            addIssueToExecutionLogger(comServerDAO, CompletionCode.ConfigurationWarning,
                    new WarningImpl(this, "unknownDeviceMessageCollected", getMessageIdentifier())
            );
        }
    }
}
