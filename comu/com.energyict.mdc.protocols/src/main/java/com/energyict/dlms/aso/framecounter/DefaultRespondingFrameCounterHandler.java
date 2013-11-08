package com.energyict.dlms.aso.framecounter;


import com.energyict.dlms.DLMSConnectionException;

/**
 * The default frameCounter handler will not verify the received frameCounter, but just return the received one...
 */
public class DefaultRespondingFrameCounterHandler implements RespondingFrameCounterHandler {

    /**
     * Check and process the received frameCounter.
     *
     * @param receivedFrameCounter the frameCounter received from the device.
     * @return the processed frameCounter
     * @throws com.energyict.dlms.DLMSConnectionException
     *          if the received frameCounter is not valid
     */
    public Integer checkRespondingFrameCounter(final int receivedFrameCounter) throws DLMSConnectionException {
        return receivedFrameCounter;
    }
}
