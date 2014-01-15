package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.Range;

import com.google.inject.Provider;
import java.util.List;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class OutboundComPortImpl extends ComPortImpl implements ServerOutboundComPort {

    private int numberOfSimultaneousConnections;

    @Inject
    protected OutboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMemberImpl> provider) {
        super(dataModel, provider);
    }

    @Override
    public void init(ComServer owner) {
        this.setComServer(owner);
    }

    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    @Override
    public int getNumberOfSimultaneousConnections () {
        return numberOfSimultaneousConnections;
    }

    protected void validate (){
        super.validate();
        this.validateInRange(Range.<Integer>open(1, MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS),
                this.getNumberOfSimultaneousConnections(),
                "comport.numberofsimultaneousconnections");
    }

    @Override
    public void setComPortType(ComPortType type) {
        validatePoolType(type);
        super.setComPortType(type);
    }

    /**
     * The type can only be changed, when this {@link com.energyict.mdc.engine.model.ComPort} is not a member of any {@link ComPortPool}
     */
    private void validatePoolType(ComPortType newType) {
        if (newType != this.getComPortType()) {
         List<ComPortPoolMember> comPortPools = Bus.getServiceLocator().getOrmClient().getComPortPoolMemberFactory().find("comPort", this);
         if (!comPortPools.isEmpty()) {
             throw new TranslatableApplicationException("outboundComPortXStillMemberOfPool", "Outbound comport {0} is still a member of comport pool {1}. The comport type cannot be changed.", this.getName(), comPortPools.get(0).getComPortPool().getName());
         }
        }
    }

    static class OutboundComPortBuilderImpl extends ComPortBuilderImpl<OutboundComPortBuilder, ServerOutboundComPort> implements OutboundComPortBuilder {
        protected OutboundComPortBuilderImpl(Provider<OutboundComPortImpl> outboundComPortProvider) {
            super(outboundComPortProvider.get(), OutboundComPortBuilder.class);
        }

        public OutboundComPortBuilder numberOfSimultaneousConnections(int number) {
            comPort.setNumberOfSimultaneousConnections(number);
            return this;
        }
    }


}