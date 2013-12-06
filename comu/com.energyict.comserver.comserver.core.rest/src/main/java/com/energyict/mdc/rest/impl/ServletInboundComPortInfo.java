package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.shadow.ports.KeyStoreShadow;
import com.energyict.mdc.shadow.ports.ServletBasedInboundComPortShadow;

public class ServletInboundComPortInfo extends InboundComPortInfo<ServletBasedInboundComPortShadow> {

    public ServletInboundComPortInfo() {
        this.comPortType = ComPortType.SERVLET;
    }

    public ServletInboundComPortInfo(ServletBasedInboundComPort comPort) {
        super(comPort);
        this.useHttps = comPort.useHttps();
        KeyStoreShadow keyStoreSpecifications = comPort.getKeyStoreSpecifications();
        if (keyStoreSpecifications !=null) {
            this.keyStoreFilePath = keyStoreSpecifications.getFilePath();
            this.keyStorePassword = keyStoreSpecifications.getPassword();
        }
        KeyStoreShadow trustedKeyStoreSpecifications = comPort.getTrustedKeyStoreSpecifications();
        if (trustedKeyStoreSpecifications!=null) {
            this.trustStoreFilePath = trustedKeyStoreSpecifications.getFilePath();
            this.trustStorePassword = trustedKeyStoreSpecifications.getPassword();
        }
    }

    protected void writeToShadow(ServletBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setUseHttps(this.useHttps);

        shadow.setKeyStoreSpecs(new KeyStoreShadow(this.keyStoreFilePath, this.keyStorePassword));
        shadow.setTrustStoreSpecs(new KeyStoreShadow(this.trustStoreFilePath, this.trustStorePassword));
    }

    public ServletBasedInboundComPortShadow asShadow() {
        ServletBasedInboundComPortShadow shadow = new ServletBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }

}
