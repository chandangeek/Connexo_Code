/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class OutboundTCPComPortPoolBuilder extends NamedBuilder<OutboundComPortPool, OutboundTCPComPortPoolBuilder> {
    private final EngineConfigurationService engineConfigurationService;

    private List<String> comPortNames;

    @Inject
    public OutboundTCPComPortPoolBuilder(EngineConfigurationService engineConfigurationService) {
        super(OutboundTCPComPortPoolBuilder.class);
        this.engineConfigurationService = engineConfigurationService;
    }

    public OutboundTCPComPortPoolBuilder withComPortNames(List<String> comPortNames) {
        this.comPortNames = comPortNames;
        return this;
    }

    @Override
    public Optional<OutboundComPortPool> find() {
        return engineConfigurationService.findOutboundComPortPoolByName(getName());
    }

    @Override
    public OutboundComPortPool create() {
        Log.write(this);
        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(getName(), ComPortType.TCP, new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));
        outboundComPortPool.setActive(true);
        if (comPortNames != null) {
            engineConfigurationService.findAllOutboundComPorts().stream().filter(port -> comPortNames.contains(port.getName()))
                    .forEach(outboundComPortPool::addOutboundComPort);
        }
        outboundComPortPool.update();
        return outboundComPortPool;
    }
}
