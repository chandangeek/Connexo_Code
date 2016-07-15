package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public abstract class AbstractConfigMessage extends AbstractRequest<OfflineDeviceMessage, CollectedMessage> {

    protected final ACE4000MessageExecutor messageExecutor;
    protected CollectedMessage collectedMessage;
    protected int trackingId = -1;

    protected AbstractConfigMessage(ACE4000Outbound ace4000) {
        super(ace4000);
        this.messageExecutor = getAce4000().getMessageProtocol().getMessageExecutor();
        multiFramedAnswer = true;       //We expect to receive an ACK for the request, and then either a CAK or a NAK or a REJECT to the configuration change
    }

    @Override
    protected void doBefore() {
        this.collectedMessage = messageExecutor.createCollectedMessage(getInput());
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
    }

    protected void failMessage(String msg) {
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setFailureInformation(ResultType.InCompatible, messageExecutor.createMessageFailedIssue(getInput(), msg));
        collectedMessage.setDeviceProtocolInformation(msg);
        setResult(collectedMessage);
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.Config, trackingId)) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            setResult(collectedMessage);
        } else if (isFailedRequest(RequestType.Config, trackingId)) {
            String msg = "Config request returned NACK. " + getReasonDescription();
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, messageExecutor.createMessageFailedIssue(getInput(), msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            setResult(collectedMessage);
        } else {
            String msg = "Meter didn't respond to config request";
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, messageExecutor.createMessageFailedIssue(getInput(), msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            setResult(collectedMessage);
        }
        getAce4000().getObjectFactory().resetReject();
    }

    protected Integer convertToSubIntervalDurationCode(int subIntervalDuration) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(30, 0);
        map.put(60, 1);
        map.put(300, 2);
        map.put(600, 3);
        map.put(900, 4);
        map.put(1200, 5);
        map.put(1800, 6);
        map.put(3600, 7);
        return map.get(subIntervalDuration);
    }

    protected Integer convertToNumberOfSubIntervalsCode(int numberOfSubIntervals) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        map.put(4, 4);
        map.put(5, 5);
        map.put(10, 6);
        map.put(15, 7);
        return map.get(numberOfSubIntervals);
    }
}