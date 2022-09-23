/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.UDPInboundComPort;
import com.energyict.mdc.ports.ComPortType;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

/**
 * Provides an implementation for the {@link CoapBasedInboundComPort} interface.
 */
@NotEmptyFilePathAndPasswords(groups = {Save.Create.class, Save.Update.class})
public class CoapBasedInboundComPortImpl extends UDPBasedInboundComPortImpl implements CoapBasedInboundComPort, UDPInboundComPort, InboundComPort, ComPort {

    private boolean dtls;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String keyStoreSpecsFilePath;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String keyStoreSpecsPassword;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String trustStoreSpecsFilePath;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String trustStoreSpecsPassword;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String contextPath;

    @Inject
    protected CoapBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public boolean isCoapBased() {
        return true;
    }

    @Override
    public boolean isServletBased() {
        return false;
    }

    @Override
    @XmlElement
    public boolean isDtls() {
        return dtls;
    }

    @Override
    public void setDtls(boolean dtls) {
        this.dtls = dtls;
    }

    @Override
    public String getKeyStoreSpecsFilePath() {
        return dtls ? keyStoreSpecsFilePath : null;
    }

    @Override
    @XmlElement
    public void setKeyStoreSpecsFilePath(String keyStoreSpecsFilePath) {
        this.keyStoreSpecsFilePath = keyStoreSpecsFilePath != null ? keyStoreSpecsFilePath.trim() : null;
    }

    @Override
    @XmlElement
    public String getKeyStoreSpecsPassword() {
        return dtls ? keyStoreSpecsPassword : null;
    }

    @Override
    public void setKeyStoreSpecsPassword(String keyStoreSpecsPassword) {
        this.keyStoreSpecsPassword = keyStoreSpecsPassword != null ? keyStoreSpecsPassword.trim() : null;
    }

    @Override
    public String getTrustStoreSpecsFilePath() {
        return dtls ? trustStoreSpecsFilePath : null;
    }

    @Override
    public void setTrustStoreSpecsFilePath(String trustStoreSpecsFilePath) {
        this.trustStoreSpecsFilePath = trustStoreSpecsFilePath != null ? trustStoreSpecsFilePath.trim() : null;
    }

    @Override
    public String getTrustStoreSpecsPassword() {
        return dtls ? trustStoreSpecsPassword : null;
    }

    @Override
    public void setTrustStoreSpecsPassword(String trustStoreSpecsPassword) {
        this.trustStoreSpecsPassword = trustStoreSpecsPassword != null ? trustStoreSpecsPassword.trim() : null;
    }

    @Override
    @XmlElement
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }


    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        CoapBasedInboundComPort mySource = (CoapBasedInboundComPort) source;
        this.setDtls(mySource.isDtls());
        this.setKeyStoreSpecsFilePath(mySource.getKeyStoreSpecsFilePath());
        this.setKeyStoreSpecsPassword(mySource.getKeyStoreSpecsPassword());
        this.setTrustStoreSpecsFilePath(mySource.getTrustStoreSpecsFilePath());
        this.setTrustStoreSpecsPassword(mySource.getTrustStoreSpecsPassword());
        this.setContextPath(contextPath);
    }

    static class CoapBasedInboundComPortBuilderImpl
            extends UDPInboundComPortBuilderImpl<CoapBasedInboundComPortBuilder, CoapBasedInboundComPort>
            implements CoapBasedInboundComPortBuilder {

        protected CoapBasedInboundComPortBuilderImpl(CoapBasedInboundComPort coapBasedInboundComPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(CoapBasedInboundComPortBuilder.class, coapBasedInboundComPort, name, numberOfSimultaneousConnections, portNumber);
            comPort.setComPortType(ComPortType.COAP);
        }

        @Override
        public CoapBasedInboundComPortBuilder dtls(boolean dtls) {
            comPort.setDtls(dtls);
            return this;
        }

        @Override
        public CoapBasedInboundComPortBuilder keyStoreSpecsFilePath(String uri) {
            comPort.setKeyStoreSpecsFilePath(uri);
            return this;
        }

        @Override
        public CoapBasedInboundComPortBuilder keyStoreSpecsPassword(String password) {
            comPort.setKeyStoreSpecsPassword(password);
            return this;
        }

        @Override
        public CoapBasedInboundComPortBuilder trustStoreSpecsFilePath(String uri) {
            comPort.setTrustStoreSpecsFilePath(uri);
            return this;
        }

        @Override
        public CoapBasedInboundComPortBuilder trustStoreSpecsPassword(String password) {
            comPort.setTrustStoreSpecsPassword(password);
            return this;
        }
    }
}