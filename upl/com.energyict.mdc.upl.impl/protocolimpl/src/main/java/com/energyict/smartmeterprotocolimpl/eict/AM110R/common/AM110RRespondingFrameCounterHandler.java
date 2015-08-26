package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * UkHub defines that the InitializationVector of the AES-GCM algorithm is unique over the lifeTime of the used key.
 * The FrameCounter is part of this InitializationVector, therefor they have defined that the frameCounter must be
 * incremented for each message with exactly one!
 */
public class AM110RRespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    private Integer responseFrameCounter = null;

    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;


    public Integer checkRespondingFrameCounter(final int receivedFrameCounter) throws DLMSConnectionException {
        if (isFrameCounterInitialized()) {
            if (this.responseFrameCounter == -1 && receivedFrameCounter == 0) { // rollover
                this.responseFrameCounter = receivedFrameCounter;
            } else if (this.responseFrameCounter == -1 && receivedFrameCounter != 0) {
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", DLMSConnectionException.REASON_SECURITY);
            } else if (receivedFrameCounter != this.responseFrameCounter + 1) {
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
