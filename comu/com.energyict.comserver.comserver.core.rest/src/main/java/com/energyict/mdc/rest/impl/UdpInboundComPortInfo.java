package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpInboundComPortInfo extends ComPortInfo {

    public UdpInboundComPortInfo() {
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
    }

    @Override
    public ComPortShadow asShadow() {
        UDPBasedInboundComPortShadow shadow = new UDPBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
