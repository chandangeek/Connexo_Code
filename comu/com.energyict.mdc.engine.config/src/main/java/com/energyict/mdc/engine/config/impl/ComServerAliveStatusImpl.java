/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.ComServerAliveStatus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class ComServerAliveStatusImpl implements ComServerAliveStatus {

    private Instant lastActiveTime;
    private Instant blockedSince;
    private Integer blockTime;
    private Integer updateFreq;
    private boolean running;
    private final Reference<ComServer> comServer = ValueReference.absent();
    private final DataModel dataModel;

    final static int DEFAULT_FREQUENCY_MINUTES = 1;

    public enum FieldNames {
        LAST_ACTIVE_TIME("lastActiveTime"),
        BLOCKED_SINCE("blockedSince"),
        BLOCK_TIME("blockTime"),
        UPDATE_FREQ("updateFreq");

        private final String name;

        FieldNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    ComServerAliveStatusImpl initialize(ComServer comServer, Instant time, Integer updateFrequency) {
        this.comServer.set(comServer);
        this.lastActiveTime = time;
        this.updateFreq = updateFrequency;
        return this;
    }

    @Inject
    public ComServerAliveStatusImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ComServer getComServer() {
        return comServer.get();
    }

    @Override
    public Instant getLastActiveTime() {
        return lastActiveTime;
    }

    @Override
    public boolean isBlocked() {
        return blockedSince != null;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setRunning(boolean isRunning) {
        this.running = isRunning;
    }

    @Override
    public Integer getUpdateFrequencyMinutes() {
        return updateFreq;
    }

    @Override
    public Optional<Instant> getBlockedSince() {
        return Optional.ofNullable(blockedSince);
    }

    @Override
    public Optional<Integer> getBlockedTime() {
        return Optional.ofNullable(blockTime);
    }

    @Override
    public void update(Instant time, Integer updateFrequency, Instant blockedSince, Integer blockTime) {
        this.lastActiveTime = time;
        this.blockedSince = blockedSince;
        this.blockTime = blockTime;
        this.updateFreq = updateFrequency;
        Save.UPDATE.save(dataModel, this);
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }
}
