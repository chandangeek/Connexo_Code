package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.base.Optional;

public class ServletInboundComPortInfo extends InboundComPortInfo<ServletBasedInboundComPort, ServletBasedInboundComPort.ServletBasedInboundComPortBuilder> {

    public ServletInboundComPortInfo() {
        this.comPortType = ComPortType.SERVLET;
    }

    public ServletInboundComPortInfo(ServletBasedInboundComPort comPort) {
        super(comPort);
        this.useHttps = comPort.isHttps();
        this.keyStoreFilePath = comPort.getKeyStoreSpecsFilePath();
        this.keyStorePassword = comPort.getKeyStoreSpecsPassword();
        this.trustStoreFilePath = comPort.getTrustStoreSpecsFilePath();
        this.trustStorePassword = comPort.getTrustStoreSpecsPassword();
        this.portNumber = comPort.getPortNumber();
        this.contextPath = comPort.getContextPath();
    }

    protected void writeTo(ServletBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        Optional<Boolean> useHttps = Optional.fromNullable(this.useHttps);
        if(useHttps.isPresent()) {
            source.setHttps(useHttps.get());
        }
        Optional<String> keyStoreFilePath = Optional.fromNullable(this.keyStoreFilePath);
        if(keyStoreFilePath.isPresent()) {
            source.setKeyStoreSpecsFilePath(keyStoreFilePath.get());
        }
        Optional<String> keyStorePassword = Optional.fromNullable(this.keyStorePassword);
        if(keyStorePassword.isPresent()) {
            source.setKeyStoreSpecsPassword(keyStorePassword.get());
        }
        Optional<String> trustStoreFilePath = Optional.fromNullable(this.trustStoreFilePath);
        if(trustStoreFilePath.isPresent()) {
            source.setTrustStoreSpecsFilePath(trustStoreFilePath.get());
        }
        Optional<String> trustStorePassword = Optional.fromNullable(this.trustStorePassword);
        if(trustStorePassword.isPresent()) {
            source.setTrustStoreSpecsPassword(trustStorePassword.get());
        }
        Optional<Integer> portNumber = Optional.fromNullable(this.portNumber);
        if(portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
        Optional<String> contextPath = Optional.fromNullable(this.contextPath);
        if(contextPath.isPresent()) {
            source.setContextPath(contextPath.get());
        }
    }

    @Override
    protected ServletBasedInboundComPort.ServletBasedInboundComPortBuilder build(ServletBasedInboundComPort.ServletBasedInboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(
                builder.
                https(useHttps).
                keyStoreSpecsFilePath(keyStoreFilePath).
                keyStoreSpecsPassword(keyStorePassword).
                trustStoreSpecsFilePath(trustStoreFilePath).
                trustStoreSpecsPassword(trustStorePassword)
                , engineModelService);
    }

    @Override
    protected ServletBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newServletBasedInboundComPort(this.name, this.contextPath, this.numberOfSimultaneousConnections, this.portNumber), engineModelService).add();
    }
}
