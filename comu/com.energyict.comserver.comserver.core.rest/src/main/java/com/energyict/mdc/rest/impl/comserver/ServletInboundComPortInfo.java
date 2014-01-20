package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

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

    protected void writeTo(ServletBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        source.setHttps(this.useHttps);

        source.setKeyStoreSpecsFilePath(this.keyStoreFilePath);
        source.setKeyStoreSpecsPassword(this.keyStorePassword);
        source.setTrustStoreSpecsFilePath(this.trustStoreFilePath);
        source.setTrustStoreSpecsPassword(this.trustStorePassword);
        source.setPortNumber(this.portNumber);
        source.setContextPath(this.contextPath);
    }

    @Override
    protected ServletBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return engineModelService.newServletBasedInbound(comServer);
    }
}
