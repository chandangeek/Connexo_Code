/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.InboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}")
public final class InboundComPortPoolImpl extends ComPortPoolImpl implements InboundComPortPool {

    public static final String FIELD_DISCOVEYPROTOCOL = "discoveryProtocolPluggableClassId";

    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService pluggableService;
    @Min(value =1, groups = {Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private long discoveryProtocolPluggableClassId;

    @Inject
    protected InboundComPortPoolImpl(DataModel dataModel, EventService eventService, EngineConfigurationService engineConfigurationService, Thesaurus thesaurus, ProtocolPluggableService pluggableService) {
        super(dataModel, thesaurus, eventService);
        this.engineConfigurationService = engineConfigurationService;
        this.pluggableService = pluggableService;
    }

    InboundComPortPoolImpl initialize(String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol) {
        this.setName(name);
        this.setComPortType(comPortType);
        this.setDiscoveryProtocolPluggableClass(discoveryProtocol);
        return this;
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public List<InboundComPort> getComPorts() {
        return engineConfigurationService.findInboundInPool(this);
    }

    @Override
    public InboundDeviceProtocolPluggableClass getDiscoveryProtocolPluggableClass() {
        return pluggableService.findInboundDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId).get();
    }

    @Override
    public void setDiscoveryProtocolPluggableClass(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass) {
        if (inboundDeviceProtocolPluggableClass !=null) {
            this.discoveryProtocolPluggableClassId = inboundDeviceProtocolPluggableClass.getId();
        } else {
            this.discoveryProtocolPluggableClassId = 0;
        }
    }

    protected void validate() {
        super.validate();
        this.validateDiscoveryProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
    }

    private void validateDiscoveryProtocolPluggableClass(long discoveryProtocolPluggableClassId) {
        if (discoveryProtocolPluggableClassId == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.MUST_HAVE_DISCOVERY_PROTOCOL);
        } else {
            pluggableService
                    .findDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId)
                    .orElseThrow(() -> new TranslatableApplicationException(thesaurus, MessageSeeds.NO_SUCH_PLUGGABLE_CLASS));
        }
    }

    protected void validateDelete() {
        super.validateDelete();
        this.validateNotUsedByComPorts();
    }

    @Override
    protected void validateMakeObsolete() {
        super.validateMakeObsolete();
        this.validateNotUsedByComPorts();
    }

    private void validateNotUsedByComPorts() {
        List<InboundComPort> comPorts = this.getComPorts();
        if (!comPorts.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.COMPORTPOOL_STILL_REFERENCED);
        }
    }

    @Override
    protected void makeMembersObsolete() {
        /* Can only be made obsolete if there are no members
         * so nothing to do here. */
    }

}