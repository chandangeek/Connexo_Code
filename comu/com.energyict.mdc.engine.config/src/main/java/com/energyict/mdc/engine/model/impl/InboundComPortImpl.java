package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

import java.util.Objects;

/**
 * Serves as the root for all {@link com.energyict.mdc.engine.model.InboundComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (17:00)
 */
public abstract class InboundComPortImpl extends ComPortImpl implements InboundComPort {

    private Reference<InboundComPortPool> comPortPool;

    protected InboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    public InboundComPortPool getComPortPool () {
        return this.comPortPool.get();
    }

    public void setComPortPool(InboundComPortPool comPortPool) {
        Objects.requireNonNull(comPortPool);
        this.comPortPool.set(comPortPool);
    }

    protected void validate() {
        super.validate();
        Objects.requireNonNull(comPortPool);
    }

    @Override
    public boolean isInbound () {
        return true;
    }

    @Override
    public boolean isTCPBased () {
        return false;
    }

    @Override
    public boolean isUDPBased() {
        return false;
    }

    @Override
    public boolean isModemBased() {
        return false;
    }

    @Override
    public boolean isServletBased () {
        return false;
    }
}