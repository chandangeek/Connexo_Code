/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.comserver;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

@ConsumerType
public interface ComServerAliveStatus {

    ComServer getComServer();

    Instant getLastActiveTime();

    void update(Instant time, Integer updateFrequency, Instant blockedSince, Integer blockTime);

    boolean isBlocked();

    boolean isRunning();

    void setRunning(boolean isRunning);

    Integer getUpdateFrequencyMinutes();

    Optional<Instant> getBlockedSince();

    Optional<Integer> getBlockedTime();

}
