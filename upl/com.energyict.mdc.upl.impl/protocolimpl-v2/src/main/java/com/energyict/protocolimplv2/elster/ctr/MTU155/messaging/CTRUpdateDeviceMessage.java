package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.comserver.commands.UpdateDeviceMessage;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierByDeviceAndProtocolInfoParts;

/**
 * @author sva
 * @since 4/07/13 - 10:01
 */
public class CTRUpdateDeviceMessage extends UpdateDeviceMessage {

    public CTRUpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement) {
        super(messageAcknowledgement);
    }

    @Override
    public void execute(ComServerDAO comServerDAO) {
        String messageProtocolInfo = comServerDAO.findDeviceMessage(getMessageIdentifier()).getProtocolInfo();
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
    }
}
