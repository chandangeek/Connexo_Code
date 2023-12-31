/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

public class UdpOutboundComPortInfo extends OutboundComPortInfo {

    public UdpOutboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.UDP);
    }

    public UdpOutboundComPortInfo(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        super(comPort, engineConfigurationService);
    }
}
