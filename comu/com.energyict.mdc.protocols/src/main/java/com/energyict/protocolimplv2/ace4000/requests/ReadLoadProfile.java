package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ResultType;

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

    public ReadLoadProfile(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
        multiFramedAnswer = true;
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        Date fromDate = Date.from(getInput().getStartReadingTime());
        Date toDate = Date.from(getInput().getEndReadingTime());

        List<IntervalData> intervalDatas = getAce4000().getObjectFactory().getLoadProfile().getProfileData().getIntervalDatas();
        if (intervalDatas.size() > 1) {
            //Send request for remaining LP entries
            Date fromReceived = intervalDatas.get(0).getEndTime();     //TODO is this the earliest or the latest entry???
            if (fromReceived.after(fromDate)) {
                getAce4000().getObjectFactory().sendLoadProfileRequest(fromDate, fromReceived);
            }
        } else {
            //Send request for all needed LP entries
            getAce4000().getObjectFactory().sendLoadProfileRequest(fromDate, toDate);
        }
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.LoadProfile)) {
            setResult(getAce4000().getObjectFactory().createCollectedLoadProfiles(getInput().getProfileObisCode()));
        } else if (isFailedRequest(RequestType.LoadProfile)) {
            List<CollectedLoadProfile> collectedLoadProfiles = getAce4000().getObjectFactory().createCollectedLoadProfiles(getInput().getProfileObisCode());
            Issue problem = this.getIssueService()
                    .newIssueCollector()
                    .addProblem(getInput(), "Requested LP data, meter returned NACK." + getReasonDescription(), getInput().getProfileObisCode());
            collectedLoadProfiles.get(0).setFailureInformation(ResultType.NotSupported, problem);
            setResult(collectedLoadProfiles);
        }
    }
}