package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import java.util.Objects;

/**
 * Link table between ComPort and ComPortPool
 */
public class ComPortPoolMemberImpl implements ComPortPoolMember {
    private Reference<ComPortPool> comPortPool;
    private Reference<ComPort> comPort;

    public ComPortPoolMemberImpl() {
    }

    public ComPortPoolMemberImpl(ComPortPool comPortPool, ComPort comPort) {
        Objects.requireNonNull(comPortPool);
        Objects.requireNonNull(comPort);
        this.comPortPool = ValueReference.of(comPortPool);
        this.comPort = ValueReference.of(comPort);
    }

    @Override
    public ComPortPool getComPortPool() {
        return comPortPool.get();
    }

    @Override
    public void setComPortPool(ComPortPool comPortPool) {
        Objects.requireNonNull(comPortPool);
        this.comPortPool = ValueReference.of(comPortPool);
    }

    @Override
    public ComPort getComPort() {
        return comPort.get();
    }

    @Override
    public void setComPort(ComPort comPort) {
        Objects.requireNonNull(comPort);
        this.comPort = ValueReference.of(comPort);
    }
}
