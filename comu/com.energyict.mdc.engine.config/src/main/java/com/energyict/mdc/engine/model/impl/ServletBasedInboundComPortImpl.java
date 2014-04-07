package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.ServletBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (11:32)
 */
@NotEmptyFilePathAndPasswords(groups = {Save.Create.class, Save.Update.class})
public class ServletBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServletBasedInboundComPort, IPBasedInboundComPort, ComPort, InboundComPort {

    private boolean https;
    private String keyStoreSpecsFilePath;
    private String keyStoreSpecsPassword;
    private String trustStoreSpecsFilePath;
    private String trustStoreSpecsPassword;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private String contextPath;

    @Inject
    protected ServletBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public boolean isServletBased() {
        return true;
    }

    @Override
    public boolean isHttps() {
        return https;
    }

    @Override
    public void setHttps(boolean https) {
        this.https = https;
    }

    @Override
    public String getKeyStoreSpecsFilePath() {
        return https?keyStoreSpecsFilePath:null;
    }

    @Override
    public void setKeyStoreSpecsFilePath(String keyStoreSpecsFilePath) {
        this.keyStoreSpecsFilePath = keyStoreSpecsFilePath!=null?keyStoreSpecsFilePath.trim():null;
    }

    @Override
    public String getKeyStoreSpecsPassword() {
        return https?keyStoreSpecsPassword:null;
    }

    @Override
    public void setKeyStoreSpecsPassword(String keyStoreSpecsPassword) {
        this.keyStoreSpecsPassword = keyStoreSpecsPassword!=null?keyStoreSpecsPassword.trim():null;
    }

    @Override
    public String getTrustStoreSpecsFilePath() {
        return https?trustStoreSpecsFilePath:null;
    }

    @Override
    public void setTrustStoreSpecsFilePath(String trustStoreSpecsFilePath) {
        this.trustStoreSpecsFilePath = trustStoreSpecsFilePath!=null?trustStoreSpecsFilePath.trim():null;
    }

    @Override
    public String getTrustStoreSpecsPassword() {
        return https?trustStoreSpecsPassword:null;
    }

    @Override
    public void setTrustStoreSpecsPassword(String trustStoreSpecsPassword) {
        this.trustStoreSpecsPassword = trustStoreSpecsPassword!=null?trustStoreSpecsPassword.trim():null;
    }

    @Override
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
        ServletBasedInboundComPort mySource = (ServletBasedInboundComPort) source;
        this.setHttps(mySource.isHttps());
        this.setKeyStoreSpecsFilePath(mySource.getKeyStoreSpecsFilePath());
        this.setKeyStoreSpecsPassword(mySource.getKeyStoreSpecsPassword());
        this.setTrustStoreSpecsFilePath(mySource.getTrustStoreSpecsFilePath());
        this.setTrustStoreSpecsPassword(mySource.getTrustStoreSpecsPassword());
        this.setContextPath(contextPath);
    }

    static class ServletBasedInboundComPortBuilderImpl extends IpBasedInboundComPortBuilderImpl<ServletBasedInboundComPortBuilder, ServletBasedInboundComPort>
            implements ServletBasedInboundComPortBuilder {

        protected ServletBasedInboundComPortBuilderImpl(ServletBasedInboundComPort servletBasedInboundComPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(ServletBasedInboundComPortBuilder.class, servletBasedInboundComPort, name, numberOfSimultaneousConnections, portNumber);
            comPort.setComPortType(ComPortType.SERVLET);
        }

        @Override
        public ServletBasedInboundComPortBuilder https(boolean https) {
            comPort.setHttps(https);
            return this;
        }

        @Override
        public ServletBasedInboundComPortBuilder keyStoreSpecsFilePath(String uri) {
            comPort.setKeyStoreSpecsFilePath(uri);
            return this;
        }

        @Override
        public ServletBasedInboundComPortBuilder keyStoreSpecsPassword(String password) {
            comPort.setKeyStoreSpecsPassword(password);
            return this;
        }

        @Override
        public ServletBasedInboundComPortBuilder trustStoreSpecsFilePath(String uri) {
            comPort.setTrustStoreSpecsFilePath(uri);
            return this;
        }

        @Override
        public ServletBasedInboundComPortBuilder trustStoreSpecsPassword(String password) {
            comPort.setTrustStoreSpecsPassword(password);
            return this;
        }

    }

}