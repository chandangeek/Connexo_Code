/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

/**
 * Models an {@link com.energyict.mdc.engine.config.IPBasedInboundComPort} that is using servlet technology
 * as a means to communicate.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (10:47)
 */
public interface ServletBasedInboundComPort extends IPBasedInboundComPort {

    /**
     * Tests if this ServletBasedInboundComPort is using
     * the HTTPS protocol to ensure that the communication
     * with is secured and encrypted.
     *
     * @return A flag that indicates if the HTTPS protocol should be used
     */
    public boolean isHttps();

    public void setHttps(boolean https);

    /**
     * Gets the specifications of the KeyStore that holds
     * the server side cryptographic keys and certificates.
     * Note that this information is <strong>NOT</strong>
     * when the ComPort does not use the HTTPS protocol.
     *
     * @return The server side KeyStore specifications
     */
    public String getKeyStoreSpecsFilePath();

    public void setKeyStoreSpecsFilePath(String keyStoreSpecsFilePath);

    public String getKeyStoreSpecsPassword();

    public void setKeyStoreSpecsPassword(String keyStoreSpecsPassword);

    /**
     * Gets the specifications of the KeyStore that holds
     * the client cryptographic keys and certificates
     * that will be trusted and therefore accepted by the server.
     * Note that this information is <strong>NOT</strong>
     * when the ComPort does not use the HTTPS protocol.
     *
     * @return The server side KeyStore specifications
     */
    public String getTrustStoreSpecsFilePath();

    public void setTrustStoreSpecsFilePath(String trustStoreSpecsFilePath);

    public String getTrustStoreSpecsPassword();

    public void setTrustStoreSpecsPassword(String trustStoreSpecsPassword);

    /**
     * Gets the context path on which the actual servlet
     * that will handle the communication will be deployed.
     * <p>
     * As an example: assuming that
     * <ul>
     * <li>the hostname of your ComServer is <code>your.comserver.com</code></li>
     * <li>the port number is set to <code>8080</code></li>
     * <li>the context path is set to <code>/eiweb</code></li>
     * </ul>
     * then the complete URL on which remote components will post data
     * to this ServletBasedInboundComPort will be <code>http://your.comserver.com:8080/eiweb</code>
     *
     * @return The context path on which the actual servlet will be deployed
     */
    public String getContextPath ();

    public void setContextPath(String contextPath);

    interface ServletBasedInboundComPortBuilder extends IpBasedInboundComPortBuilder<ServletBasedInboundComPortBuilder, ServletBasedInboundComPort>{
        public ServletBasedInboundComPortBuilder https(boolean https);
        public ServletBasedInboundComPortBuilder keyStoreSpecsFilePath(String uri);
        public ServletBasedInboundComPortBuilder keyStoreSpecsPassword(String password);
        public ServletBasedInboundComPortBuilder trustStoreSpecsFilePath(String uri);
        public ServletBasedInboundComPortBuilder trustStoreSpecsPassword(String password);
    }

}