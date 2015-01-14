package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class OutboundTCPComPortPoolFactory extends NamedFactory<OutboundTCPComPortPoolFactory, OutboundComPortPool> {
    private final Store store;
    private final EngineConfigurationService engineConfigurationService;

    private List<String> comPorts;

    @Inject
    public OutboundTCPComPortPoolFactory(Store store, EngineConfigurationService engineConfigurationService) {
        super(OutboundTCPComPortPoolFactory.class);
        this.store = store;
        this.engineConfigurationService = engineConfigurationService;
    }

    public OutboundTCPComPortPoolFactory withComPorts(String... comPortNames){
        if (comPortNames != null){
           this.comPorts = Arrays.asList(comPortNames);
        }
        return this;
    }

    @Override
    public OutboundComPortPool get() {
        Log.write(this);
        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(getName(), ComPortType.TCP, new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));
        outboundComPortPool.setActive(true);
        if (comPorts != null) {
            List<OutboundComPort> createdComPorts = store.get(OutboundComPort.class);
            for (OutboundComPort comPort : createdComPorts) {
                if (comPorts.contains(comPort.getName())){
                    outboundComPortPool.addOutboundComPort(comPort);
                }
            }
        }
        outboundComPortPool.save();
        store.add(OutboundComPortPool.class, outboundComPortPool);
        return outboundComPortPool;
    }
}
