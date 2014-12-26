package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;

public class OutboundTCPComPortFactory extends NamedFactory<OutboundTCPComPortFactory, OutboundComPort> {
    private final Store store;

    private ComServer comServer;

    @Inject
    public OutboundTCPComPortFactory(Store store) {
        super(OutboundTCPComPortFactory.class);
        this.store = store;
    }

    public OutboundTCPComPortFactory withComServer(ComServer comServer){
        this.comServer = comServer;
        return this;
    }

    public OutboundComPort get(){
        Log.write(this);
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(getName(), 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        comPort.save();
        store.add(OutboundComPort.class, comPort);
        return comPort;
    }
}
