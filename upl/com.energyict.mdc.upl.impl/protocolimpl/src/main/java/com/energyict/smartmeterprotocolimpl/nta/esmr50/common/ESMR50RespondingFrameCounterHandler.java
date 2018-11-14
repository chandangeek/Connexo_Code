package com.energyict.smartmeterprotocolimpl.nta.esmr50.common;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RespondingFrameCounterHandler;

@Deprecated
public class ESMR50RespondingFrameCounterHandler extends DSMR40RespondingFrameCounterHandler {

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
                throw new DLMSConnectionException("Received incorrect overFlow FrameCounter.", DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER); //todo check exception type change
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
