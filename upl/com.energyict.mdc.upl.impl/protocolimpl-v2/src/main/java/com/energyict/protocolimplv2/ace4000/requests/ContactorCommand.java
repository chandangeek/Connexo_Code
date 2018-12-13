package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ContactorCommand extends AbstractRequest<OfflineDeviceMessage, CollectedMessage> {

    private int trackingId = -1;
    private int command;    //0 = connect, 1 = disconnect
    private boolean useActivationDate = false;

    public ContactorCommand(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        Date date = null;
        if(useActivationDate){
            Long epoch = Long.valueOf(getInput().getDeviceMessageAttributes().get(0).getValue());
            date = new Date(epoch);  //EIServer system timezone
        }
        trackingId = getAce4000().getObjectFactory().sendContactorCommand(date, command);
    }

    @Override
    protected void parseResult() {
        ACE4000MessageExecutor messageExecutor = getAce4000().getMessageProtocol().getMessageExecutor();
        CollectedMessage collectedMessage = messageExecutor.createCollectedMessage(getInput());

        if (getResult() != null) {
            return;           //Don't set the result again if it failed already (due to invalid arguments in the message)
        }

        if (isFailedRequest(RequestType.Contactor, trackingId)) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, messageExecutor.createMessageFailedIssue(getInput(), "Contactor command failed, meter returned NACK. " + getReasonDescription()));
            collectedMessage.setDeviceProtocolInformation("Contactor command failed, meter returned NACK " + getReasonDescription());
            setResult(collectedMessage);
        } else {
            //If no nak has been received, assume that the command was successful. The meter does not send confirmation.
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            setResult(collectedMessage);
        }
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public void useActivationDate(boolean useActivationDate) {
        this.useActivationDate = useActivationDate;
    }

}