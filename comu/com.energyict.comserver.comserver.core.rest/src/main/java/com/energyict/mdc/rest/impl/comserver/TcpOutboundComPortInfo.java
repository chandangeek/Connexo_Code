package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.ports.ComPortType;

public class TcpOutboundComPortInfo extends OutboundComPortInfo {

    public TcpOutboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.TCP);
    }

    public TcpOutboundComPortInfo(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        super(comPort, engineConfigurationService);
    }
}
