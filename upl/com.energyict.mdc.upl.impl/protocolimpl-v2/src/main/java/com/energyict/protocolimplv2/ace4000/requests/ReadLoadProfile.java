package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.MdcManager;
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

    public ReadLoadProfile(ACE4000Outbound ace4000) {
        this(ace4000, null, null);
    }

    public ReadLoadProfile(ACE4000Outbound ace4000, Date from, Date to) {
        super(ace4000);
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
            Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createProblem(getInput(), "loadProfileXIssue", getInput().getProfileObisCode().toString(), "Requested LP data, meter returned NACK. " + getReasonDescription());
            collectedLoadProfiles.get(0).setFailureInformation(ResultType.InCompatible, problem);
            setResult(collectedLoadProfiles);
        }
    }

}