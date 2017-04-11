package com.energyict.dlms.aso.framecounter;

import com.energyict.dlms.DLMSConnectionException;

/**
 * The responding FrameCounter handler defines what to do with the received frameCounter from the meter.
 */
public interface RespondingFrameCounterHandler {

    public static final long FC_MAX_VALUE = 0xFFFFFFFFl;
    /**
     * Check and process the received frameCounter.
     *
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws DLMSConnectionException if the received frameCounter is not valid
     */
    Long checkRespondingFrameCounter(long receivedFrameCounter) throws DLMSConnectionException;

    /**
     * When the encryption key is changed, the frame counters reset.
     * This method is also used in case of dedicated ciphering to make the checkRespondingFrameCounter accept any FC value
     * that comes in, except com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler#FC_MAX_VALUE
     * @param initialValue
     */
    void setRespondingFrameCounter(long initialValue);

}
