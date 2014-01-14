package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.impl.ServletBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (11:32)
 */
public class ServletBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerServletBasedInboundComPort {

    private boolean https;
    private String keyStoreSpecsFilePath;
    private String keyStoreSpecsPassword;
    private String trustStoreSpecsFilePath;
    private String trustStoreSpecsPassword;
    private String contextPath;

    public void init(ComServer owner) {
        this.setComServer(owner);
    }

    @Inject
    protected ServletBasedInboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public boolean isServletBased() {
        return true;
    }

    protected void validate(){
        super.validate();
        if (this.useHttps()) {
            this.validatePaths(this.getKeyStoreSpecsFilePath(), this.getKeyStoreSpecsPassword(), "keystore");
            this.validatePaths(this.getTrustStoreSpecsFilePath(), this.getTrustStoreSpecsPassword(), "truststore");
        }
        this.validateNotNull(this.getContextPath(), "servletbasedinboundcomport.contextpath");
    }

    private void validatePaths(Object filePath, Object password, String keyStoreType) {
        this.validateNotNull(filePath, "servletbasedinboundcomport." + keyStoreType + ".filepath");
        this.validateNotNull(password, "servletbasedinboundcomport." + keyStoreType + ".password");
    }

    @Override
    public boolean useHttps () {
        return https;
    }

    @Override
    public void setHttps(boolean https) {
        this.https = https;
    }

    @Override
    public String getKeyStoreSpecsFilePath() {
        return keyStoreSpecsFilePath;
    }

    @Override
    public void setKeyStoreSpecsFilePath(String keyStoreSpecsFilePath) {
        this.keyStoreSpecsFilePath = keyStoreSpecsFilePath;
    }

    @Override
    public String getKeyStoreSpecsPassword() {
        return keyStoreSpecsPassword;
    }

    @Override
    public void setKeyStoreSpecsPassword(String keyStoreSpecsPassword) {
        this.keyStoreSpecsPassword = keyStoreSpecsPassword;
    }

    @Override
    public String getTrustStoreSpecsFilePath() {
        return trustStoreSpecsFilePath;
    }

    @Override
    public void setTrustStoreSpecsFilePath(String trustStoreSpecsFilePath) {
        this.trustStoreSpecsFilePath = trustStoreSpecsFilePath;
    }

    @Override
    public String getTrustStoreSpecsPassword() {
        return trustStoreSpecsPassword;
    }

    @Override
    public void setTrustStoreSpecsPassword(String trustStoreSpecsPassword) {
        this.trustStoreSpecsPassword = trustStoreSpecsPassword;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}