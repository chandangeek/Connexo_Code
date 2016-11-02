package com.energyict.mdc.protocol.api.security;


/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/04/2016 - 11:05
 */
public interface FrameCounterCache {

    /**
     * Update the TX frame counter for given client ID.
     *
     * @param clientId     Client ID.
     * @param frameCounter New frame counter.
     */
    void setTXFrameCounter(final int clientId, final int frameCounter);

    /**
     * Returns the TX frame counter for given client ID.
     *
     * @param clientId Client ID.
     * @return TX frame counter for given client if present, -1 otherwise.
     */
    long getTXFrameCounter(final int clientId);

}
