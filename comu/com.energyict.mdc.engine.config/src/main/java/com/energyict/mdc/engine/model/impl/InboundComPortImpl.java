package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPoolMember;
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

    protected InboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
    }

    public InboundComPortPool getComPortPool () {
        return this.comPortPool.get();
    }

    public void setComPortPool(InboundComPortPool comPortPool) {
        validateNotNull(comPortPool, "inboundComPort.comPortPool");
        validateNotNull(this.getComPortType(), "type");
        validateComPortType(comPortPool);
        this.comPortPool.set(comPortPool);
    }

    private void validateComPortType(InboundComPortPool comPortPool) {
        if (comPortPool.getComPortType()!=this.getComPortType()) {
            throw new TranslatableApplicationException("comPortTypeOfComPortXDoesNotMatchWithComPortPoolY", "The ComPortType of ComPort {0} does not match with that of the ComPortPool {1}",
                    new Object[] {this.getComPortType(), comPortPool.getComPortType()});
        }
    }

    protected void validate() {
        super.validate();
        validateNotNull(comPortPool.orNull(), "inboundComPort.comPortPool");
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

    static class InboundComPortBuilderImpl<B extends InboundComPortBuilder<B,C>, C extends ServerInboundComPort>
            extends ComPortBuilderImpl<B, C> implements InboundComPortBuilder<B,C> {
        protected InboundComPortBuilderImpl(Class<B> clazz, Provider<C> inboundComPortProvider) {
            super(clazz, inboundComPortProvider.get());
        }

        @Override
        public B comPortPool(InboundComPortPool comPortPool) {
            comPort.setComPortPool(comPortPool);
            return self;
        }

    }

}