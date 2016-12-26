package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

/**
 * Copyrights EnergyICT
 * Date: 16.04.15
 * Time: 09:54
 */
public class ReadFirmwareVersion extends AbstractRequest<DeviceIdentifier, CollectedFirmwareVersion> {

    private final IssueFactory issueFactory;

    public ReadFirmwareVersion(ACE4000Outbound ace4000, IssueFactory issueFactory) {
        super(ace4000);
        this.issueFactory = issueFactory;
    }

    @Override
    protected void doBefore() {
        // intentionally left blank
    }

    @Override
    protected void doRequest() {
        getAce4000().getObjectFactory().sendFirmwareRequest();
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.FirmwareVersion)) {
            setResult(getAce4000().getObjectFactory().createCollectedFirmwareVersions());
        } else if (isFailedRequest(RequestType.FirmwareVersion)) {
            Issue issue = this.issueFactory.createProblem(getInput(), "issue.protocol.readingOfFirmwareFailed", getReasonDescription());
            setResult(getAce4000().getObjectFactory().createFailedFirmwareVersions(issue));
        }
    }
}