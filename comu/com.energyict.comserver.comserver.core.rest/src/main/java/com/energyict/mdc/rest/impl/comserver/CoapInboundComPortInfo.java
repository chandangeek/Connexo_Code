/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

import java.util.Optional;

public class CoapInboundComPortInfo extends InboundComPortInfo<CoapBasedInboundComPort, CoapBasedInboundComPort.CoapBasedInboundComPortBuilder> {

    public CoapInboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.COAP);
    }

    public CoapInboundComPortInfo(CoapBasedInboundComPort comPort) {
        super(comPort);
        this.useDtls = comPort.isDtls();
        this.useSharedKeys = comPort.isUsingSharedKeys();
        this.keyStoreFilePath = comPort.getKeyStoreSpecsFilePath();
        this.keyStorePassword = comPort.getKeyStoreSpecsPassword();
        this.trustStoreFilePath = comPort.getTrustStoreSpecsFilePath();
        this.trustStorePassword = comPort.getTrustStoreSpecsPassword();
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
        this.contextPath = comPort.getContextPath();
    }

    protected void writeTo(CoapBasedInboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        Optional<Boolean> useDtls = Optional.ofNullable(this.useDtls);
        if (useDtls.isPresent()) {
            source.setDtls(useDtls.get());
        }
        Optional<Boolean> useSharedKeys = Optional.ofNullable(this.useSharedKeys);
        if (useSharedKeys.isPresent()) {
            source.setUsingSharedKeys(useSharedKeys.get());
        }
        Optional<String> keyStoreFilePath = Optional.ofNullable(this.keyStoreFilePath);
        if (keyStoreFilePath.isPresent()) {
            source.setKeyStoreSpecsFilePath(keyStoreFilePath.get());
        }
        Optional<String> keyStorePassword = Optional.ofNullable(this.keyStorePassword);
        if (keyStorePassword.isPresent()) {
            source.setKeyStoreSpecsPassword(keyStorePassword.get());
        }
        Optional<String> trustStoreFilePath = Optional.ofNullable(this.trustStoreFilePath);
        if (trustStoreFilePath.isPresent()) {
            source.setTrustStoreSpecsFilePath(trustStoreFilePath.get());
        }
        Optional<String> trustStorePassword = Optional.ofNullable(this.trustStorePassword);
        if (trustStorePassword.isPresent()) {
            source.setTrustStoreSpecsPassword(trustStorePassword.get());
        }
        Optional<Integer> portNumber = Optional.ofNullable(this.portNumber);
        if (portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
        Optional<Integer> bufferSize = Optional.ofNullable(this.bufferSize);
        if (bufferSize.isPresent()) {
            source.setBufferSize(bufferSize.get());
        }
        Optional<String> contextPath = Optional.ofNullable(this.contextPath);
        if (contextPath.isPresent()) {
            source.setContextPath(contextPath.get());
        }
    }

    @Override
    protected CoapBasedInboundComPort.CoapBasedInboundComPortBuilder build(CoapBasedInboundComPort.CoapBasedInboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(
                builder.
                        dtls(useDtls).
                        sharedKeys(useSharedKeys).
                        bufferSize(bufferSize).
                        keyStoreSpecsFilePath(keyStoreFilePath).
                        keyStoreSpecsPassword(keyStorePassword).
                        trustStoreSpecsFilePath(trustStoreFilePath).
                        trustStoreSpecsPassword(trustStorePassword)
                , engineConfigurationService);
    }

    @Override
    protected CoapBasedInboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        return build(comServer.newCoapBasedInboundComPort(this.name, this.contextPath, this.numberOfSimultaneousConnections, this.portNumber), engineConfigurationService).add();
    }
}
