/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.engine.config;

import com.energyict.mdc.common.comserver.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface ComServerAliveStatus {

    ComServer getComServer();

    Instant getLastActiveTime();

    void update(Instant time, Integer updateFrequency, Instant blockedSince, Long blockTime);

    boolean isBlocked();

    boolean isRunning();

    void setRunning(boolean isRunning);

    Integer getUpdateFrequencyMinutes();

    Optional<Instant> getBlockedSince();

    Optional<Long> getBlockedTime();

}
