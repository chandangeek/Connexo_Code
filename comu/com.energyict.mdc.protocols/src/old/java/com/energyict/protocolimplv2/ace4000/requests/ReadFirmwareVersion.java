package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

/**
 * Copyrights EnergyICT
 * Date: 16.04.15
 * Time: 09:54
 */
public class ReadFirmwareVersion extends AbstractRequest<DeviceIdentifier, CollectedFirmwareVersion> {

    public ReadFirmwareVersion(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
    }

    @Override
    protected void doBefore() {
        // intentionally left blank
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.FirmwareVersion)) {
            setResult(getAce4000().getObjectFactory().createCollectedFirmwareVersions());
        } else if (isFailedRequest(RequestType.FirmwareVersion)) {
            Issue issue = this.getIssueService()
                    .newIssueCollector()
                    .addProblem(getInput(), "Could not fetch the FirmwareVersions:" + getReasonDescription());
            setResult(getAce4000().getObjectFactory().createFailedFirmwareVersions(issue));
        }
    }

    @Override
    protected void doRequest() {
        getAce4000().getObjectFactory().sendFirmwareRequest();
    }
}
