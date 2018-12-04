package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocolimplv2.nta.dsmr40.DSMR40RespondingFrameCounterHandler;


public class ESMR50RespondingFrameCounterHandler extends DSMR40RespondingFrameCounterHandler {

    private Long responseFrameCounter = null;

    /**
     * Indicates whether the FrameCounter needs to be validated with a +1
     */
    private boolean frameCounterInitialized = false;

    @Override
    public Long checkRespondingFrameCounter(final long receivedFrameCounter) throws DLMSConnectionException {
        if (isFrameCounterInitialized()) {
            if (this.responseFrameCounter == -1 && receivedFrameCounter == 0) { // rollover
                this.responseFrameCounter = receivedFrameCounter;
            } else if (this.responseFrameCounter == -1 && receivedFrameCounter != 0) {
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
            //} else if (receivedFrameCounter != this.responseFrameCounter + 1) {
            } else if (!(receivedFrameCounter > this.responseFrameCounter)) {  //Greater than previous FC, gaps are allowed. No more +1 restriction!
                throw new DLMSConnectionException("Received incorrect frame-counter [ESMR5]: "+receivedFrameCounter+" instead of "+this.responseFrameCounter, DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
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
