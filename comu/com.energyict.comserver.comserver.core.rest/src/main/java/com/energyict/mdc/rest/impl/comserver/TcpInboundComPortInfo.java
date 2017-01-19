package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.ports.ComPortType;

import java.util.Optional;

public class TcpInboundComPortInfo extends InboundComPortInfo<TCPBasedInboundComPort, TCPBasedInboundComPort.TCPBasedInboundComPortBuilder> {

    public TcpInboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.TCP);
    }

    public TcpInboundComPortInfo(TCPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
    }

    protected void writeTo(TCPBasedInboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        Optional<Integer> portNumber = Optional.ofNullable(this.portNumber);
        if(portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
    }

    @Override
    protected TCPBasedInboundComPort.TCPBasedInboundComPortBuilder build(TCPBasedInboundComPort.TCPBasedInboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(builder, engineConfigurationService);
    }

    @Override
    protected TCPBasedInboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        return build(comServer.newTCPBasedInboundComPort(this.name, this.numberOfSimultaneousConnections, this.portNumber), engineConfigurationService).add();
    }
}
