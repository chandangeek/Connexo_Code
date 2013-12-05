package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.meterdata.identifiers.CanFindLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
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
public class ReadMeterEvents extends AbstractRequest<CanFindLogBook, List<CollectedLogBook>> {

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
            result.add(getAce4000().getObjectFactory().getDeviceLogBook(getInput()));
            setResult(result);
        } else if (isFailedRequest(RequestType.Events)) {
            List<CollectedLogBook> result = new ArrayList<CollectedLogBook>();
            CollectedLogBook deviceLogBook = getAce4000().getObjectFactory().getDeviceLogBook(getInput());
            ResultType resultType = ResultType.DataIncomplete;
            if (deviceLogBook.getCollectedMeterEvents().isEmpty()) {
                resultType = ResultType.NotSupported;
            }
            deviceLogBook.setFailureInformation(
                    resultType,
                    MdcManager.getIssueCollector().addProblem(
                            getInput(),
                            "Requested events, meter returned NACK." + getReasonDescription(),
                            getInput().getLogBook().getLogBookSpec().getDeviceObisCode()));
            result.add(deviceLogBook);
            setResult(result);
        }
    }
}