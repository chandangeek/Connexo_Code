package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * The frame counter must increment, but gaps are allowed. It should not be exactly 1 higher for every request.
 */
public class G3RespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    private Integer responseFrameCounter = null;

    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;

    /**
     * Check and process the received frameCounter.
     *
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws com.energyict.dlms.DLMSConnectionException
     *          if the received frameCounter is not valid
     */
    public Integer checkRespondingFrameCounter(final int receivedFrameCounter) throws DLMSConnectionException {
        if (isFrameCounterInitialized()) {
            if (this.responseFrameCounter == -1 && receivedFrameCounter == 0) { // rollover
                this.responseFrameCounter = receivedFrameCounter;
            } else if (this.responseFrameCounter == -1 && receivedFrameCounter < 0) {
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", DLMSConnectionException.REASON_SECURITY);
            } else if (!(receivedFrameCounter > this.responseFrameCounter)) {  //Greater than previous FC, gaps are allowed. No more +1 restriction!
                throw new DLMSConnectionException("Received incorrect FrameCounter.", DLMSConnectionException.REASON_SECURITY);
            } else {
                this.responseFrameCounter = receivedFrameCounter;
            }
        } else {
            this.responseFrameCounter = receivedFrameCounter;
            setFrameCounterInitialized(true);
        }
        return this.responseFrameCounter;
    }


    private boolean isFrameCounterInitialized() {
        return frameCounterInitialized;
    }

    private void setFrameCounterInitialized(boolean frameCounterInitialized) {
        this.frameCounterInitialized = frameCounterInitialized;
    }
}
