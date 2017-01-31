/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import java.time.Instant;
import java.util.Objects;

/**
 * Link table between ComPort and ComPortPool.
 */
public class ComPortPoolMemberImpl implements ComPortPoolMember {

    private final Reference<OutboundComPortPool> comPortPool = ValueReference.absent();
    private final Reference<OutboundComPort> comPort = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Override
    public OutboundComPortPool getComPortPool() {
        return comPortPool.get();
    }

    @Override
    public void setComPortPool(OutboundComPortPool comPortPool) {
        Objects.requireNonNull(comPortPool);
        this.comPortPool.set(comPortPool);
    }

    @Override
    public OutboundComPort getComPort() {
        return comPort.get();
    }

    @Override
    public void setComPort(OutboundComPort comPort) {
        Objects.requireNonNull(comPort);
        this.comPort.set(comPort);
    }

}