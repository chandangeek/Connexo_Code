package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.impl.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.shadow.ports.KeyStoreShadow;
import com.energyict.mdc.shadow.ports.ServletBasedInboundComPortShadow;

public class ServletInboundComPortInfo extends InboundComPortInfo<ServletBasedInboundComPort> {

    public ServletInboundComPortInfo() {
        this.comPortType = ComPortType.SERVLET;
    }

    public ServletInboundComPortInfo(ServletBasedInboundComPort comPort) {
        super(comPort);
        this.useHttps = comPort.useHttps();
        this.keyStoreFilePath = comPort.getKeyStoreSpecsFilePath();
        this.keyStorePassword = comPort.getKeyStoreSpecsPassword();
        this.trustStoreFilePath = comPort.getTrustStoreSpecsFilePath();
        this.trustStorePassword = comPort.getTrustStoreSpecsPassword();
        this.portNumber = comPort.getPortNumber();
        this.contextPath = comPort.getContextPath();
    }

    protected void writeTo(ServletBasedInboundComPort source) {
        super.writeTo(source);
        source.setHttps(this.useHttps);

        source.setKeyStoreSpecsFilePath(this.keyStoreFilePath);
        source.setKeyStoreSpecsPassword(this.keyStorePassword);
        source.setTrustStoreSpecsFilePath(this.trustStoreFilePath);
        source.setTrustStoreSpecsPassword(this.trustStorePassword);
        source.setPortNumber(this.portNumber);
        source.setContextPath(this.contextPath);
    }

}
