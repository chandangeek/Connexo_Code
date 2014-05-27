package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;

/**
 * Serves as the root for all {@link com.energyict.mdc.engine.model.InboundComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (17:00)
 */
@ComPortPoolTypeMatchesComPortType(groups = { Save.Create.class, Save.Update.class })
public abstract class InboundComPortImpl extends ComPortImpl implements ComPort, InboundComPort {

    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private final Reference<InboundComPortPool> comPortPool = ValueReference.absent();

    protected InboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    public InboundComPortPool getComPortPool () {
        return this.comPortPool.orNull();
    }

    public void setComPortPool(InboundComPortPool comPortPool) {
        this.comPortPool.set(comPortPool);
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

        protected InboundComPortBuilderImpl(Class<B> clazz, C comPort, String name) {
            super(clazz, comPort, name);
        }

        @Override
        public B comPortPool(InboundComPortPool comPortPool) {
            comPort.setComPortPool(comPortPool);
            return self;
        }

    }

}