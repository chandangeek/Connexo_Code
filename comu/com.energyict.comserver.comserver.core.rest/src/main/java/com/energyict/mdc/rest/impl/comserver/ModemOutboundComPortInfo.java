package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.ports.ComPortType;

public class ModemOutboundComPortInfo extends OutboundComPortInfo {

    public ModemOutboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.SERIAL);
    }

    public ModemOutboundComPortInfo(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        super(comPort, engineConfigurationService);
    }
}
