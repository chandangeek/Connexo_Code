package com.energyict.protocolimplv2.nta.dsmr40;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;


/**
 * DSMR 4.0 defines that the InitializationVector of the AES-GCM algorithm is unique over the lifeTime of the used key.
 * The FrameCounter is part of this InitializationVector, therefor they have defined that the frameCounter must be
 * incremented for each message with exactly one!
 */
public class DSMR40RespondingFrameCounterHandler implements RespondingFrameCounterHandler{

    private Long responseFrameCounter = null;

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
    public Long checkRespondingFrameCounter(final long receivedFrameCounter) throws DLMSConnectionException {
        if (isFrameCounterInitialized()) {
            if (this.responseFrameCounter == -1 && receivedFrameCounter == 0) { // rollover
                this.responseFrameCounter = receivedFrameCounter;
            } else if (this.responseFrameCounter == -1 && receivedFrameCounter != 0) {
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
            } else  if (!(receivedFrameCounter > this.responseFrameCounter)) {
                throw new DLMSConnectionException("Received incorrect frame-counter [DSMR4]: "+receivedFrameCounter+" instead of "+this.responseFrameCounter, DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
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
    public void setRespondingFrameCounter(long initialValue) {
        this.responseFrameCounter = initialValue;
    }


    private boolean isFrameCounterInitialized() {
        return frameCounterInitialized;
    }

    private void setFrameCounterInitialized(boolean frameCounterInitialized) {
        this.frameCounterInitialized = frameCounterInitialized;
    }
}
