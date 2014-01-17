package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

import com.energyict.mdc.engine.model.OutboundComPort;
import com.google.inject.Provider;
import java.util.Objects;

/**
 * Serves as the root for all {@link com.energyict.mdc.engine.model.InboundComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (17:00)
 */
public abstract class InboundComPortImpl extends ComPortImpl implements ServerInboundComPort {

    private final Reference<InboundComPortPool> comPortPool = ValueReference.absent();

    protected InboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMemberImpl> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
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

//    static protected class ComPortBuilderImpl<B extends ComPort.Builder<B, C>, C extends ComPort> implements ComPort.Builder<B, C> {

    static class InboundComPortBuilderImpl<B extends InboundComPortBuilder<B,C>, C extends InboundComPort>
            extends ComPortBuilderImpl<B, C> implements InboundComPortBuilder<B,C> {
        protected InboundComPortBuilderImpl(Class<B> clazz, Provider<C> inboundComPortProvider) {
            super(clazz, inboundComPortProvider.get());
        }

        @Override
        public InboundComPortBuilder comPortPool(InboundComPortPool comPortPool) {
            comPort.setComPortPool(comPortPool);
            return this;
        }

    }

}