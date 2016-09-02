package com.energyict.mdc.channels.ip.socket;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import com.energyict.protocol.exceptions.ConnectionException;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 27/07/2016 - 15:00
 */
public class TLSConnectionType extends OutboundTcpIpConnectionType {

    private static final String TLS_VERSION_PROPERTY_NAME = "TLSVersion";
    private static final String TLS_DEFAULT_VERSION = "TLSv1.2";

    private PropertySpec tlsVersionPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = super.getOptionalProperties();
        optionalProperties.add(tlsVersionPropertySpec());
        return optionalProperties;
    }

    protected String tlsVersionPropertyValue() {
        return (String) this.getProperty(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (name.equals(TLS_VERSION_PROPERTY_NAME)) {
            return tlsVersionPropertySpec();
        } else {
            return super.getPropertySpec(name);
        }
    }

    @Override
    protected ServerLoggableComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", tlsVersionPropertyValue());

        try {
            final SSLContext sslContext = SSLContext.getInstance(tlsVersionPropertyValue());

            /* Accept everything */
            final TrustManager[] trustManagers = new TrustManager[]{getAcceptEverythingTrustManager()};

            /* No client authentication keys */
            final KeyManager[] keyManagers;
            keyManagers = null;

            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) socketFactory.createSocket();
            socket.setNeedClientAuth(false);
            socket.setWantClientAuth(false);
            socket.connect(new InetSocketAddress(host, port));

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            throw new ConnectionException(e);
        }
    }

    private X509TrustManager getAcceptEverythingTrustManager() {
        return new X509TrustManager() {

            @Override
            public final X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
        };
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}