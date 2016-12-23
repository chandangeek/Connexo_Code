package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.issue.Issue;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
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

    public ReadMeterEvents(ACE4000Outbound ace4000) {
        super(ace4000);
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
            List<CollectedLogBook> result = new ArrayList<CollectedLogBook>();
            result.add(getAce4000().getObjectFactory().getDeviceLogBook(getInput().getLogBookIdentifier()));
            setResult(result);
        } else if (isFailedRequest(RequestType.Events)) {
            List<CollectedLogBook> result = new ArrayList<CollectedLogBook>();
            CollectedLogBook deviceLogBook = getAce4000().getObjectFactory().getDeviceLogBook(getInput().getLogBookIdentifier());
            ResultType resultType = ResultType.InCompatible;
            if (deviceLogBook.getCollectedMeterEvents().size() == 0) {
                resultType = ResultType.NotSupported;
            }
            Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createProblem(getInput(), "loadProfileXIssue", getInput().getLogBookObisCode().toString(), "Requested events, meter returned NACK. " + getReasonDescription());
            deviceLogBook.setFailureInformation(resultType, problem);
            result.add(deviceLogBook);
            setResult(result);
        }
    }
}