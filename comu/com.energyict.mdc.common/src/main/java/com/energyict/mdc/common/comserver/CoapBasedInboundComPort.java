/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models an {@link UDPBasedInboundComPort} that is using Coap standard
 * as a means to communicate.
 */
public interface CoapBasedInboundComPort extends UDPInboundComPort {

    /**
     * Tests if this CoapBasedInboundComPort is using
     * the DTLS security to ensure that the communication
     * with is secured and encrypted.
     *
     * @return A flag that indicates if the HTTPS protocol should be used
     */
    public boolean isDtls();

    public void setDtls(boolean dtls);

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

    interface CoapBasedInboundComPortBuilder extends UDPInboundComPortBuilder<CoapBasedInboundComPort.CoapBasedInboundComPortBuilder, CoapBasedInboundComPort> {
        public CoapBasedInboundComPortBuilder dtls(boolean dtls);

        public CoapBasedInboundComPortBuilder keyStoreSpecsFilePath(String uri);

        public CoapBasedInboundComPortBuilder keyStoreSpecsPassword(String password);

        public CoapBasedInboundComPortBuilder trustStoreSpecsFilePath(String uri);

        public CoapBasedInboundComPortBuilder trustStoreSpecsPassword(String password);
    }

}