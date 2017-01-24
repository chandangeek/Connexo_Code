package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.frame.RequestFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.ResponseFrame;

/**
 * @author sva
 * @since 05/06/2014 - 15:50
 */
public interface Connection {

    /**
     * Send a frame to the meter.
     * This is the place where the retry mechanism is implemented.
     * After every error, we clear the input buffer and try again
     * for the number of retries.
     */
    void sendFrame(RequestFrame frame) throws GarnetException;

    /**
     * Send a frame, and wait for the response from the meter.
     * This is the place where the retry mechanism is implemented.
     * After every error, we clear the input buffer and try again
     * for the number of retries.
     */
    ResponseFrame sendFrameGetResponse(RequestFrame frame) throws GarnetException;

    /**
     * Sets the session key
     */
    public void setSessionKey(byte[] sessionKey);
}
