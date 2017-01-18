package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class FirmwareUpgrade extends AbstractRequest<MessageEntry, MessageResult> {

    private int trackingId = -1;

    public FirmwareUpgrade(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        String[] parts = getInput().getContent().split("=");
        String path = parts[1].substring(1).split("\"")[0];
        int jarSize = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int jadSize = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        trackingId = getAce4000().getObjectFactory().sendFirmwareUpgradeRequest(path, jarSize, jadSize);
    }

    @Override
    protected void parseResult() {
        if (isFailedRequest(RequestType.Firmware, trackingId)) {
            setResult(MessageResult.createFailed(getInput(), "Firmware upgrade failed, meter returned NACK." + getReasonDescription()));
        } else {
            //If no nak has been received, assume that the command was successful. The meter does not send confirmation.
            setResult(MessageResult.createSuccess(getInput()));
        }
    }
}