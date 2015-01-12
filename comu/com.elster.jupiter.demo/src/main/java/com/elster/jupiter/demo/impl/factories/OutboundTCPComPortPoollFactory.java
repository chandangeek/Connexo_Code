package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class OutboundTCPComPortPoollFactory extends NamedFactory<OutboundTCPComPortPoollFactory, OutboundComPortPool> {
    private final Store store;
    private final EngineConfigurationService engineConfigurationService;

    private List<String> comPorts;

    @Inject
    public OutboundTCPComPortPoollFactory(Store store, EngineConfigurationService engineConfigurationService) {
        super(OutboundTCPComPortPoollFactory.class);
        this.store = store;
        this.engineConfigurationService = engineConfigurationService;
    }

    public OutboundTCPComPortPoollFactory withComPorts(String... comPortNames){
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
            List<OutboundComPort> ourComPorts = store.get(OutboundComPort.class);
            for (String comPort : comPorts) {
                OutboundComPort outboundComPort = ourComPorts.stream().filter(p -> p.getName().equals(comPort)).findFirst().orElseThrow(() -> new UnableToCreate("Unknown comPort"));
                outboundComPortPool.addOutboundComPort(outboundComPort);
            }
        }
        outboundComPortPool.save();
        store.add(OutboundComPortPool.class, outboundComPortPool);
        return outboundComPortPool;
    }
}
