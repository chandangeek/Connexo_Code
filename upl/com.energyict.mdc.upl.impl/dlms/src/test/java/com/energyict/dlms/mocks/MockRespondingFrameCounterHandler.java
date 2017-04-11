package com.energyict.dlms.mocks;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 2/02/12
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class MockRespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    private long responseFrameCounter = -1;

    /**
     * Check and process the received frameCounter.
     *
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws com.energyict.dlms.DLMSConnectionException
     *          if the received frameCounter is not valid
     */
    public Long checkRespondingFrameCounter(final long receivedFrameCounter) throws DLMSConnectionException {
        if (receivedFrameCounter == FC_MAX_VALUE) {
            throw new DLMSConnectionException("FrameCounter reached the maximum value: "+ FC_MAX_VALUE + "! This must be prevented by changeing the encryption key on time!", DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        } else if (responseFrameCounter != -1) {
            if (receivedFrameCounter != this.responseFrameCounter + 1) {
                throw new DLMSConnectionException("Received incorrect FrameCounter: " +receivedFrameCounter+ " expected "+(this.responseFrameCounter + 1), DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
            } else {
                this.responseFrameCounter = receivedFrameCounter;
            }
        } else {
            this.responseFrameCounter = receivedFrameCounter;
        }
        return this.responseFrameCounter;
    }

    @Override
    public void setRespondingFrameCounter(long initialValue) {
        responseFrameCounter = initialValue;
    }

}
