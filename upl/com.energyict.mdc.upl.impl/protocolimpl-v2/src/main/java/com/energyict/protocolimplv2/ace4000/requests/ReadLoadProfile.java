package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.protocol.IntervalData;
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

    public ReadLoadProfile(ACE4000Outbound ace4000) {
        super(ace4000);
        multiFramedAnswer = true;
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        Date fromDate = getInput().getStartReadingTime();
        Date toDate = getInput().getEndReadingTime();

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
            Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createProblem(getInput(), "Requested LP data, meter returned NACK." + getReasonDescription(), getInput().getProfileObisCode());
            collectedLoadProfiles.get(0).setFailureInformation(ResultType.NotSupported, problem);
            setResult(collectedLoadProfiles);
        }
    }
}