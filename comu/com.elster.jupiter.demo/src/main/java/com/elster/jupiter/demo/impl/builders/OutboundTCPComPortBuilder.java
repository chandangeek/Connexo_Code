/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;
import java.util.Optional;

public class OutboundTCPComPortBuilder extends NamedBuilder<OutboundComPort, OutboundTCPComPortBuilder> {
    private ComServer comServer;

    @Inject
    public OutboundTCPComPortBuilder() {
        super(OutboundTCPComPortBuilder.class);
    }

    public OutboundTCPComPortBuilder withComServer(ComServer comServer){
        this.comServer = comServer;
        return this;
    }

    @Override
    public OutboundComPort get() {
        if (this.comServer == null){
            throw new UnableToCreate("Com server can't be null");
        }
        return find().orElseGet(() -> create());
    }

    @Override
    public Optional<OutboundComPort> find() {
        Optional<ComPort> comPort = comServer.getComPorts().stream().filter(port -> port instanceof OutboundComPort && port.getName().equals(getName())).findFirst();
        return Optional.ofNullable((OutboundComPort) comPort.orElse(null));
    }

    @Override
    public OutboundComPort create(){
        Log.write(this);
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(getName(), 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        return comPort;
    }
}
