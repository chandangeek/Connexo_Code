package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.Range;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class OutboundComPortImpl extends ComPortImpl implements OutboundComPort {

    private final EngineModelService engineModelService;
    private int numberOfSimultaneousConnections;

    @Inject
    protected OutboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> provider, EngineModelService engineModelService) {
        super(dataModel, provider);
        this.engineModelService = engineModelService;
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
        this.validateInRange(Range.<Integer>closed(1, MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS),
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
         List<ComPortPool> comPortPools = engineModelService.findContainingComPortPoolsForComPort(this);
         if (!comPortPools.isEmpty()) {
             throw new TranslatableApplicationException("outboundComPortXStillMemberOfPool", "Outbound comport {0} is still a member of comport pool {1}. The comport type cannot be changed.", this.getName(), comPortPools.get(0).getName());
         }
        }
    }

    static class OutboundComPortBuilderImpl extends ComPortBuilderImpl<OutboundComPortBuilder, OutboundComPort> implements OutboundComPortBuilder {
        protected OutboundComPortBuilderImpl(Provider<OutboundComPortImpl> outboundComPortProvider) {
            super(OutboundComPortBuilder.class, outboundComPortProvider.get());
        }

        public OutboundComPortBuilder numberOfSimultaneousConnections(int number) {
            comPort.setNumberOfSimultaneousConnections(number);
            return this;
        }
    }


}