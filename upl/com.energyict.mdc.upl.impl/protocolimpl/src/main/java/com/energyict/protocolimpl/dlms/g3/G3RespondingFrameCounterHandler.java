package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * The frame counter must increment, but gaps are allowed. It should not be exactly 1 higher for every request.
 * This check should happen for every G3PLC (UDP) protocol.
 */
public class G3RespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    /**
     * See {@link DLMSConnectionException} for the possibilities.
     * The error handling in case of an invalid frame counter is based on this.
     */
    private final short errorHandling;

    private long responseFrameCounter = -1;

    public G3RespondingFrameCounterHandler(short errorHandling) {
        this.errorHandling = errorHandling;
    }

    /**
     * Check and process the received frameCounter.
     *
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws com.energyict.dlms.DLMSConnectionException if the received frameCounter is not valid
     */
    public Long checkRespondingFrameCounter(final long receivedFrameCounter) throws DLMSConnectionException {
        if (receivedFrameCounter == FC_MAX_VALUE) {
            throw new DLMSConnectionException("FrameCounter reached the maximum value: "+ FC_MAX_VALUE + "! This must be prevented by changeing the encryption key on time!", DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        } else if (responseFrameCounter != -1) {
            if (!(receivedFrameCounter > this.responseFrameCounter)) {  //Greater than previous FC, gaps are allowed. No more +1 restriction!
                throw new DLMSConnectionException("Received incorrect frame counter '" + receivedFrameCounter + "'. Should be greater than the previous frame counter: '" + responseFrameCounter + "'", errorHandling);
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
