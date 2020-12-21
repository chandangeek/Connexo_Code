/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

public interface FrameCounterCache {

    long DEFAULT_FC= -1;

    /**
     * Update the TX frame counter for given client ID.
     *
     * @param clientId Client ID.
     * @param frameCounter New frame counter.
     */
    void setTXFrameCounter(final int clientId, final long frameCounter);

    /**
     * Returns the TX frame counter for given client ID.
     *
     * @param clientId Client ID.
     * @return TX frame counter for given client if present, -1 otherwise.
     */
    long getTXFrameCounter(final int clientId);

}