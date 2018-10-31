/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.data.Device;

import java.time.Duration;
import java.time.Instant;

/**
 * Models neighbor information that is maintained by a {@link Device}
 * that uses G3 specific power line carrier technology to communicate.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (14:17)
 */
@ProviderType
public interface G3Neighbor extends PLCNeighbor {

    public int getTxGain();

    public int getTxResolution();

    public int getTxCoefficient();

    public int getLinkQualityIndicator();

    public Duration getTimeToLive();

    public long getToneMap();

    /**
     * Gets the time to live of the tone map.
     *
     * @return The time to live of the tone map
     * @see #getToneMap()
     */
    public Duration getToneMapTimeToLive();

    public PhaseInfo getPhaseInfo();

    G3NodeState getState();

    long getMacPANId();

    String getNodeAddress();

    int getShortAddress();

    Instant getLastUpdate();

    Instant getLastPathRequest();

    long getRoundTrip();

    int getLinkCost();

}