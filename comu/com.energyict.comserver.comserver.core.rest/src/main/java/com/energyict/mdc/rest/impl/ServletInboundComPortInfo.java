package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.ports.ServletBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ServletBasedInboundComPortShadow;

public class ServletInboundComPortInfo extends InboundComPortInfo<ServletBasedInboundComPortShadow> {

    public ServletInboundComPortInfo() {
        this.comPortType = ComPortType.SERVLET;
    }

    public ServletInboundComPortInfo(ServletBasedInboundComPort comPort) {
        super(comPort);
    }

    protected void writeToShadow(ServletBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
    }

    public ServletBasedInboundComPortShadow asShadow() {
        ServletBasedInboundComPortShadow shadow = new ServletBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }


}
