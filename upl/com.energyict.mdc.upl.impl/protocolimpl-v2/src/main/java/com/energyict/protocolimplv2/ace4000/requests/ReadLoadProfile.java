package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ReadLoadProfile extends AbstractRequest<LoadProfileReader, List<CollectedLoadProfile>> {

    private final Date from;
    private final Date to;
    private final IssueFactory issueFactory;

    public ReadLoadProfile(ACE4000Outbound ace4000, IssueFactory issueFactory) {
        this(ace4000, null, null, issueFactory);
    }

    public ReadLoadProfile(ACE4000Outbound ace4000, Date from, Date to, IssueFactory issueFactory) {
        super(ace4000);
        this.issueFactory = issueFactory;
        multiFramedAnswer = true;
        this.from = from;
        this.to = to;
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        Date fromDate = from != null ? from : getInput().getStartReadingTime();
        Date toDate = to != null ? to : getInput().getEndReadingTime();
        getAce4000().getObjectFactory().sendLoadProfileRequest(fromDate, toDate);
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.LoadProfile)) {
            setResult(getAce4000().getObjectFactory().createCollectedLoadProfiles(getInput().getProfileObisCode()));
        } else if (isFailedRequest(RequestType.LoadProfile)) {
            List<CollectedLoadProfile> collectedLoadProfiles = getAce4000().getObjectFactory().createCollectedLoadProfiles(getInput().getProfileObisCode());
            Issue problem = this.issueFactory.createProblem(getInput(), "loadProfileXIssue", getInput().getProfileObisCode().toString(), "Requested LP data, meter returned NACK. " + getReasonDescription());
            collectedLoadProfiles.get(0).setFailureInformation(ResultType.InCompatible, problem);
            setResult(collectedLoadProfiles);
        }
    }

}