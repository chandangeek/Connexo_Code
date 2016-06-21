package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

/**
* Copyrights EnergyICT
* Date: 12/11/12
* Time: 14:00
* Author: khe
*/
public class WriteConfiguration extends AbstractRequest<OfflineDeviceMessage, CollectedMessage> {

    private int trackingId = -1;
    ACE4000MessageExecutor messageExecutor;
    CollectedMessage collectedMessage;

    public WriteConfiguration(ACE4000Outbound ace4000) {
        super(ace4000);
        messageExecutor = getAce4000().getMessageProtocol().getMessageExecutor();
        collectedMessage = messageExecutor.createCollectedMessage(getInput());
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        //Retry if necessary
        if ((trackingId != -1) && !isReceivedRequest(RequestType.Config, trackingId)) {
            trackingId = getAce4000().getMessageProtocol().getMessageExecutor().sendConfigurationMessage(getInput());
        }

        //The initial request
        if (trackingId == -1) {
            //Message executor parses the tags and executes the right configuration request
            Integer status = getAce4000().getMessageProtocol().getMessageExecutor().sendConfigurationMessage(getInput());
            if (status == null) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.NotSupported, messageExecutor.createUnsupportedWarning(getInput()));
                collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                return;
            }
            trackingId = status;
        }
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.Config, trackingId)) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            setResult(collectedMessage);
        } else if (isFailedRequest(RequestType.Config, trackingId)) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, messageExecutor.createMessageFailedIssue(getInput(), "Config request returned NACK. " + getReasonDescription()));
            collectedMessage.setDeviceProtocolInformation("Config request returned NACK.");
            setResult(collectedMessage);
        } else {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, messageExecutor.createMessageFailedIssue(getInput(), "Meter didn't respond to config request" + getReasonDescription()));
            collectedMessage.setDeviceProtocolInformation("Meter didn't respond to config request");
            setResult(collectedMessage);
        }
    }
}