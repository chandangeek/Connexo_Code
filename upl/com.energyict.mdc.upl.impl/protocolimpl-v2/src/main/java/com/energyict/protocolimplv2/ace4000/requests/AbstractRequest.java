package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.protocol.exception.ProtocolRuntimeException;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestState;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;
import com.energyict.protocolimplv2.ace4000.requests.tracking.Tracker;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:56
 * Author: khe
 */
public abstract class AbstractRequest<Input, Result> {

    //Indicates if the response can come in multiple frames. In this case, keep listening until a timeout occurs!
    protected boolean multiFramedAnswer = false;
    private ACE4000Outbound ace4000;
    private Input input;
    private Result result = null;
    private String reasonDescription = "";

    public AbstractRequest(ACE4000Outbound ace4000) {
        this.ace4000 = ace4000;
    }

    public ACE4000Outbound getAce4000() {
        return ace4000;
    }

    protected Input getInput() {
        return input;
    }

    protected Result getResult() {
        return result;
    }

    protected void setResult(Result result) {
        this.result = result;
    }

    /**
     * Extra info received with the NACK frame, used for the proper issue/problem
     */
    protected String getReasonDescription() {
        return reasonDescription == null ? "" : " Reason: " + reasonDescription;
    }

    /**
     * Method that contain request specific behaviour
     */
    protected abstract void doBefore();

    /**
     * Parse the incoming responses
     */
    protected abstract void parseResult();

    /**
     * The actual request
     */
    protected abstract void doRequest();

    /**
     * Some requests can return partial data (e.g. some registers, some events).
     * Others just throw the exception.
     */
    protected void handleException(RuntimeException e) {
        if (e != null) {
            throw e;
        }
    }

    /**
     * The actual request (done in the object factory) is wrapped with a retry mechanism and timeout handling
     * Takes in a specific argument and returns a specific result, depending on the sub class implementation
     */
    public Result request(Input input) {
        this.input = input;
        this.result = null;

        doBefore();
        RuntimeException exception = null;
        getAce4000().getObjectFactory().setRequestAttemptNumber(0);

        while (getAce4000().getObjectFactory().getRequestAttemptNumber() <= getAce4000().getProperties().getRetries()) {

            doRequest();
            if (result != null) {   //E.g. unknown message tag, no requests to send!
                return result;
            }

            try {
                //Keep reading in frames until the correct response has been received (result is parsed and returned), or a timeout occurs.
                while (true) {
                    List<String> xmlFrames = getAce4000().getAce4000Connection().readFrames(multiFramedAnswer);
                    for (String frame : xmlFrames) {
                        getAce4000().getObjectFactory().parseXML(frame);
                    }
                    parseResult();      //Check if everything necessary has been received.
                    if (result != null) {
                        return result;
                    }
                }
            } catch (ProtocolRuntimeException e) {
                exception = e;
            }
            getAce4000().getObjectFactory().increaseRequestAttemptNumber();    //Retry in case of timeout
        }

        //After X retries
        handleException(exception);
        return result;
    }

    /**
     * Check the request received a proper response (not nack)
     */
    protected boolean isSuccessfulRequest(RequestType type) {
        return isSuccessfulRequest(type, -1);
    }

    /**
     * Check the request received a proper response (not nack)
     */
    protected boolean isSuccessfulRequest(RequestType type, int trackingId) {
        RequestState requestState = getRequestState(type, trackingId);
        return RequestState.Success.equals(requestState);
    }

    private RequestState getRequestState(RequestType type, int trackingId) {
        for (Tracker tracker : getAce4000().getObjectFactory().getRequestStates().keySet()) {
            if (tracker.equals(new Tracker(type, trackingId))) {
                return getAce4000().getObjectFactory().getRequestStates().get(tracker);
            }
        }
        return null;
    }

    /**
     * Check if the request received a nack response
     */
    protected boolean isFailedRequest(RequestType type) {
        return isFailedRequest(type, -1);
    }

    /**
     * Check if the request received a nack response. Additionally, fetch the reason description for logging purposes
     */
    protected boolean isFailedRequest(RequestType type, int trackingId) {
        RequestState requestState = getRequestState(type, trackingId);
        String reason = getReasonDescription(type, trackingId);

        reasonDescription = reason == null ? reasonDescription : reason;
        return RequestState.Fail.equals(requestState);
    }

    private String getReasonDescription(RequestType type, int trackingId) {
        for (Tracker tracker : getAce4000().getObjectFactory().getReasonDescriptions().keySet()) {
            if (tracker.equals(new Tracker(type, trackingId))) {
                return getAce4000().getObjectFactory().getReasonDescriptions().get(tracker);
            }
        }
        return null;
    }

    /**
     * Check if a response (nack or proper result) has been received for the request
     */
    protected boolean isReceivedRequest(RequestType type) {
        return isFailedRequest(type) || isSuccessfulRequest(type);
    }

    /**
     * Check if a response (nack or proper result) has been received for the request with a specific trackingId
     */
    protected boolean isReceivedRequest(RequestType type, int trackingId) {
        return isFailedRequest(type, trackingId) || isSuccessfulRequest(type, trackingId);
    }
}