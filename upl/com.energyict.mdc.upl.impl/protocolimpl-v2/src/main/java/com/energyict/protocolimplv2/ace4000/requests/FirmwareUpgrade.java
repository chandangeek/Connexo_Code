package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
* Copyrights EnergyICT
* Date: 12/11/12
* Time: 14:00
* Author: khe
*/
public class FirmwareUpgrade extends AbstractRequest<OfflineDeviceMessage, CollectedMessage> {

    private int trackingId = -1;

    public FirmwareUpgrade(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        String path = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.URL_PATH).getDeviceMessageAttributeValue();
        int jarSize = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.JAR_FILE_SIZE).getDeviceMessageAttributeValue());
        int jadSize = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.JAD_FILE_SIZE).getDeviceMessageAttributeValue());
        trackingId = getAce4000().getObjectFactory().sendFirmwareUpgradeRequest(path, jarSize, jadSize);
    }

    @Override
    protected void parseResult() {
        ACE4000MessageExecutor messageExecutor = getAce4000().getMessageProtocol().getMessageExecutor();
        CollectedMessage collectedMessage = messageExecutor.createCollectedMessage(getInput());
        if (isFailedRequest(RequestType.Firmware, trackingId)) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, messageExecutor.createMessageFailedIssue(getInput(), "Firmware upgrade failed, meter returned NACK. " + getReasonDescription()));
            collectedMessage.setDeviceProtocolInformation("Firmware upgrade failed, meter returned NACK. " + getReasonDescription());
            setResult(collectedMessage);
        } else {
            //If no nak has been received, assume that the command was successful. The meter does not send confirmation.
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            setResult(collectedMessage);
        }
    }

}