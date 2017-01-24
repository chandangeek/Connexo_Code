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

    private Integer responseFrameCounter = null;

    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;

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
    public Integer checkRespondingFrameCounter(final int receivedFrameCounter) throws DLMSConnectionException {
        if (isFrameCounterInitialized()) {
            if (this.responseFrameCounter == -1 && receivedFrameCounter == 0) { // rollover
                this.responseFrameCounter = receivedFrameCounter;
            } else if (this.responseFrameCounter == -1 && receivedFrameCounter < 0) {
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", errorHandling);
            } else if (!(receivedFrameCounter > this.responseFrameCounter)) {  //Greater than previous FC, gaps are allowed. No more +1 restriction!
                throw new DLMSConnectionException("Received incorrect frame counter '" + receivedFrameCounter + "'. Should be greater than the previous frame counter: '" + responseFrameCounter + "'", errorHandling);
            } else {
                this.responseFrameCounter = receivedFrameCounter;
            }
        } else {
            this.responseFrameCounter = receivedFrameCounter;
            setFrameCounterInitialized(true);
        }
        return this.responseFrameCounter;
    }

    @Override
    public void resetRespondingFrameCounter(int initialValue) {
        responseFrameCounter = initialValue;
    }

    private boolean isFrameCounterInitialized() {
        return frameCounterInitialized;
    }

    private void setFrameCounterInitialized(boolean frameCounterInitialized) {
        this.frameCounterInitialized = frameCounterInitialized;
    }
}
