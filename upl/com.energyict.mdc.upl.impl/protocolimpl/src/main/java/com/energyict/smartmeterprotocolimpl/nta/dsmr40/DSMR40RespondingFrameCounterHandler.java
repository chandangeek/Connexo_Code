package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

/**
 * DSMR 4.0 defines that the InitializationVector of the AES-GCM algorithm is unique over the lifeTime of the used key.
 * The FrameCounter is part of this InitializationVector, therefor they have defined that the frameCounter must be
 * incremented for each message with exactly one!
 */
@Deprecated
public class DSMR40RespondingFrameCounterHandler implements RespondingFrameCounterHandler{

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
