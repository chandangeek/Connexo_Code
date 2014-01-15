package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;

import java.util.Objects;
import javax.inject.Inject;

/**
 * Link table between ComPort and ComPortPool
 */
public class ComPortPoolMemberImpl implements ComPortPoolMember {

    private final DataModel dataModel;
    private Reference<ComPortPool> comPortPool;
    private Reference<ComPort> comPort;

    @Inject
    ComPortPoolMemberImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ComPortPool getComPortPool() {
        return comPortPool.get();
    }

    @Override
    public void setComPortPool(ComPortPool comPortPool) {
        Objects.requireNonNull(comPortPool);
        this.comPortPool.set(comPortPool);
    }

    @Override
    public ComPort getComPort() {
        return comPort.get();
    }

    @Override
    public void setComPort(ComPort comPort) {
        Objects.requireNonNull(comPort);
        this.comPort.set(comPort);
    }

    @Override
    public void remove() {
        dataModel.remove(this);
    }
}
