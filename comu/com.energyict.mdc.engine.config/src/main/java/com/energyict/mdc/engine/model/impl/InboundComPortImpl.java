package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.google.inject.Provider;

/**
 * Serves as the root for all {@link com.energyict.mdc.engine.model.InboundComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (17:00)
 */
public abstract class InboundComPortImpl extends ComPortImpl implements ComPort, InboundComPort {

    private final Reference<InboundComPortPool> comPortPool = ValueReference.absent();

    protected InboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    public InboundComPortPool getComPortPool () {
        return this.comPortPool.orNull();
    }

    public void setComPortPool(InboundComPortPool comPortPool) {
        this.comPortPool.set(comPortPool);
    }

    private void validateComPortType(InboundComPortPool comPortPool) {
        if (comPortPool.getComPortType()!=this.getComPortType()) {
            throw new TranslatableApplicationException("comPortTypeOfComPortXDoesNotMatchWithComPortPoolY", "The ComPortType of ComPort {0} does not match with that of the ComPortPool {1}",
                    new Object[] {this.getComPortType(), comPortPool.getComPortType()});
        }
    }

    protected void validateCreate() {
        super.validateCreate();
        validateNotNull(this.getComPortType(), "type");
        validateNotNull(comPortPool.orNull(), "inboundComPort.comPortPool");
        validateComPortType(comPortPool.get());
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

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        InboundComPort mySource = (InboundComPort) source;
        this.setComPortPool(mySource.getComPortPool());
    }

    static class InboundComPortBuilderImpl<B extends InboundComPortBuilder<B,C>, C extends InboundComPort>
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