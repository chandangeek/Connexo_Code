package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * UkHub defines that the InitializationVector of the AES-GCM algorithm is unique over the lifeTime of the used key.
 * The FrameCounter is part of this InitializationVector, therefor they have defined that the frameCounter must be
 * incremented for each message with exactly one!
 */
public class AM110RRespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    private long responseFrameCounter = -1;

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
