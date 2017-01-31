/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import java.util.Optional;

public class ServletInboundComPortInfo extends InboundComPortInfo<ServletBasedInboundComPort, ServletBasedInboundComPort.ServletBasedInboundComPortBuilder> {

    public ServletInboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.SERVLET);
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

    protected void writeTo(ServletBasedInboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        Optional<Boolean> useHttps = Optional.ofNullable(this.useHttps);
        if(useHttps.isPresent()) {
            source.setHttps(useHttps.get());
        }
        Optional<String> keyStoreFilePath = Optional.ofNullable(this.keyStoreFilePath);
        if(keyStoreFilePath.isPresent()) {
            source.setKeyStoreSpecsFilePath(keyStoreFilePath.get());
        }
        Optional<String> keyStorePassword = Optional.ofNullable(this.keyStorePassword);
        if(keyStorePassword.isPresent()) {
            source.setKeyStoreSpecsPassword(keyStorePassword.get());
        }
        Optional<String> trustStoreFilePath = Optional.ofNullable(this.trustStoreFilePath);
        if(trustStoreFilePath.isPresent()) {
            source.setTrustStoreSpecsFilePath(trustStoreFilePath.get());
        }
        Optional<String> trustStorePassword = Optional.ofNullable(this.trustStorePassword);
        if(trustStorePassword.isPresent()) {
            source.setTrustStoreSpecsPassword(trustStorePassword.get());
        }
        Optional<Integer> portNumber = Optional.ofNullable(this.portNumber);
        if(portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
        Optional<String> contextPath = Optional.ofNullable(this.contextPath);
        if(contextPath.isPresent()) {
            source.setContextPath(contextPath.get());
        }
    }

    @Override
    protected ServletBasedInboundComPort.ServletBasedInboundComPortBuilder build(ServletBasedInboundComPort.ServletBasedInboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(
                builder.
                https(useHttps).
                keyStoreSpecsFilePath(keyStoreFilePath).
                keyStoreSpecsPassword(keyStorePassword).
                trustStoreSpecsFilePath(trustStoreFilePath).
                trustStoreSpecsPassword(trustStorePassword)
                , engineConfigurationService);
    }

    @Override
    protected ServletBasedInboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        return build(comServer.newServletBasedInboundComPort(this.name, this.contextPath, this.numberOfSimultaneousConnections, this.portNumber), engineConfigurationService).add();
    }
}
