package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ReadMeterEvents extends AbstractRequest<LogBookReader, List<CollectedLogBook>> {

    private final IssueFactory issueFactory;

    public ReadMeterEvents(ACE4000Outbound ace4000, IssueFactory issueFactory) {
        super(ace4000);
        this.issueFactory = issueFactory;
        multiFramedAnswer = true;
    }

    protected void doBefore() {
    }

    /**
     * Only send a new request if events have not been received earlier
     */
    @Override
    protected void doRequest() {
        if (getAce4000().getObjectFactory().getEventData().getMeterEvents() == null) {
            getAce4000().getObjectFactory().sendEventRequest();
        }
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.Events)) {
            List<CollectedLogBook> result = new ArrayList<>();
            result.add(getAce4000().getObjectFactory().getDeviceLogBook(getInput().getLogBookIdentifier()));
            setResult(result);
        } else if (isFailedRequest(RequestType.Events)) {
            List<CollectedLogBook> result = new ArrayList<>();
            CollectedLogBook deviceLogBook = getAce4000().getObjectFactory().getDeviceLogBook(getInput().getLogBookIdentifier());
            ResultType resultType = ResultType.InCompatible;
            if (deviceLogBook.getCollectedMeterEvents().isEmpty()) {
                resultType = ResultType.NotSupported;
            }
            Issue problem = this.issueFactory.createProblem(getInput(), "loadProfileXIssue", getInput().getLogBookObisCode().toString(), "Requested events, meter returned NACK. " + getReasonDescription());
            deviceLogBook.setFailureInformation(resultType, problem);
            result.add(deviceLogBook);
            setResult(result);
        }
    }
}