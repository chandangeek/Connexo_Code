/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import java.util.Optional;

public class UdpInboundComPortInfo extends InboundComPortInfo<UDPBasedInboundComPort, UDPBasedInboundComPort.UDPBasedInboundComPortBuilder> {

    public UdpInboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.UDP);
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
    }

    @Override
    protected void writeTo(UDPBasedInboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        Optional<Integer> portNumber = Optional.ofNullable(this.portNumber);
        if(portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
        Optional<Integer> bufferSize = Optional.ofNullable(this.bufferSize);
        if(bufferSize.isPresent()) {
            source.setBufferSize(bufferSize.get());
        }
    }

    @Override
    protected UDPBasedInboundComPort.UDPBasedInboundComPortBuilder build(UDPBasedInboundComPort.UDPBasedInboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(builder.
                bufferSize(bufferSize)
                , engineConfigurationService);
    }

    @Override
    protected UDPBasedInboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        return build(comServer.newUDPBasedInboundComPort(this.name, this.numberOfSimultaneousConnections, this.portNumber), engineConfigurationService).add();
    }
}
