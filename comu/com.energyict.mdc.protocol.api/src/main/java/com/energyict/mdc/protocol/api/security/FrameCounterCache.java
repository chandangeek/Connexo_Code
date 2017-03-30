/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;


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
