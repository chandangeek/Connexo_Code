package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;


/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.OutboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class OutboundComPortImpl extends ComPortImpl implements OutboundComPort {

    private final EngineConfigurationService engineConfigurationService;

    @Range(min = 1, max = MAXIMUM_NUMBER_OF_SIMULTANEOUS_CONNECTIONS, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}")
    private int numberOfSimultaneousConnections;

    @Inject
    protected OutboundComPortImpl(DataModel dataModel, EngineConfigurationService engineConfigurationService, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
        this.engineConfigurationService = engineConfigurationService;
    }

    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    @Override
    @XmlElement
    public int getNumberOfSimultaneousConnections () {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setComPortType(ComPortType type) {
        validatePoolType(type);
        super.setComPortType(type);
    }

    @Override
    public void makeObsolete() {
        validateMakeObsolete();
        removeFromComPortPools();
        super.makeObsolete();
    }

    private void removeFromComPortPools() {
        for (ComPortPool comPortPool : engineConfigurationService.findContainingComPortPoolsForComPort(this)) {
            ((OutboundComPortPool)comPortPool).removeOutboundComPort(this);
        }
    }


    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        this.setNumberOfSimultaneousConnections(source.getNumberOfSimultaneousConnections());
    }

    /**
     * The type can only be changed, when this {@link com.energyict.mdc.engine.config.ComPort} is not a member of any {@link ComPortPool}
     */
    private void validatePoolType(ComPortType newType) {
        if (newType != this.getComPortType()) {
         List<OutboundComPortPool> comPortPools = engineConfigurationService.findContainingComPortPoolsForComPort(this);
         if (!comPortPools.isEmpty()) {
             throw new TranslatableApplicationException(thesaurus, MessageSeeds.OUTBOUND_COMPORT_STILL_IN_POOL);
         }
        }
    }

    static class OutboundComPortBuilderImpl extends ComPortBuilderImpl<OutboundComPortBuilder, OutboundComPort> implements OutboundComPortBuilder {
        protected OutboundComPortBuilderImpl(OutboundComPort outboundComPort, String name) {
            super(OutboundComPortBuilder.class, outboundComPort, name);
        }

        @Override
        public OutboundComPortBuilder comPortType(ComPortType comPortType) {
            comPort.setComPortType(comPortType);
            return self;
        }

    }


}