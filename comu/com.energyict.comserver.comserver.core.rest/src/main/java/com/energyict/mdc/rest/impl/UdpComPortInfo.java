package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpComPortInfo extends ComPortInfo {

    public UdpComPortInfo() {
    }

    public UdpComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
    }

    @Override
    public ComPortShadow asShadow() {
        UDPBasedInboundComPortShadow shadow = new UDPBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
