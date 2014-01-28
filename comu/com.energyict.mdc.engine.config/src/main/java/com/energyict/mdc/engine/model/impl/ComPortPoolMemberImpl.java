package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;

import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import javax.inject.Inject;
import java.util.Objects;

/**
 * Link table between ComPort and ComPortPool
 */
public class ComPortPoolMemberImpl implements ComPortPoolMember {

    private final DataModel dataModel;
    private final Reference<OutboundComPortPool> comPortPool = ValueReference.absent();
    private final Reference<OutboundComPort> comPort = ValueReference.absent();

    @Inject
    ComPortPoolMemberImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

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
