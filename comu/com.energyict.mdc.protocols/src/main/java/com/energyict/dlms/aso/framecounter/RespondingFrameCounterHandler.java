package com.energyict.dlms.aso.framecounter;

import com.energyict.dlms.DLMSConnectionException;

/**
 * The responding FrameCounter handler defines what to do with the received frameCounter from the meter.
 */
public interface RespondingFrameCounterHandler {

    /**
     * Check and process the received frameCounter.
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws DLMSConnectionException if the received frameCounter is not valid
     */
    public Integer checkRespondingFrameCounter(int receivedFrameCounter) throws DLMSConnectionException;


    /**
     * When the encryption key is changed, the frame counters reset.
     */
    void resetRespondingFrameCounter(int initialValue);
}
