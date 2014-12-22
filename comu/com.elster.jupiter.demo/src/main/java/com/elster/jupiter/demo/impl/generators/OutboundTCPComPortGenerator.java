package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;

public class OutboundTCPComPortGenerator extends NamedGenerator<OutboundTCPComPortGenerator> {

    private final Store store;

    private ComServer comServer;

    @Inject
    public OutboundTCPComPortGenerator(Store store) {
        super(OutboundTCPComPortGenerator.class);
        this.store = store;
    }

    public OutboundTCPComPortGenerator withComServer(ComServer comServer){
        this.comServer = comServer;
        return this;
    }

    public void create(){
        System.out.println("==> Creating Outbound TCP Port '" + getName() + "'...");
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(getName(), 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        comPort.save();
        store.add(OutboundComPort.class, comPort);
    }
}
