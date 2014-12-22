package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

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