package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.cpo.Environment;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocol.exceptions.ConnectionException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String PREFERRED_CIPHER_SUITES_PROPERTY_NAME = "PreferredCipherSuites";
    private static final String SEPARATOR = ",";

    /**
     * The version of the TLS protocol to be used.
     * Defaults to TLSv1.2
     */
    private PropertySpec tlsVersionPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    /**
     * A comma-separated list of cipher suites that are preferred by the client (ComServer)
     */
    private PropertySpec preferredCipheringSuitesPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(PREFERRED_CIPHER_SUITES_PROPERTY_NAME);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = super.getOptionalProperties();
        optionalProperties.add(tlsVersionPropertySpec());
        optionalProperties.add(preferredCipheringSuitesPropertySpec());
        return optionalProperties;
    }

    private String getTLSVersionPropertyValue() {
        return (String) this.getProperty(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    private String getPreferredCipherSuitesPropertyValue() {
        return (String) this.getProperty(PREFERRED_CIPHER_SUITES_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case TLS_VERSION_PROPERTY_NAME:
                return tlsVersionPropertySpec();
            case PREFERRED_CIPHER_SUITES_PROPERTY_NAME:
                return preferredCipheringSuitesPropertySpec();
            default:
                return super.getPropertySpec(name);
        }
    }

    @Override
    protected ComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", getTLSVersionPropertyValue());

        try {
            final SSLContext sslContext = SSLContext.getInstance(getTLSVersionPropertyValue());

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
            handlePreferredCipherSuites(socket);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            throw new ConnectionException(e);
        }
    }

    private void handlePreferredCipherSuites(SSLSocket socket) throws ConnectionException {
        String preferredCipherSuitesPropertyValue = getPreferredCipherSuitesPropertyValue();
        if (preferredCipherSuitesPropertyValue != null) {

            List<String> enabledCipherSuites = new ArrayList<>(Arrays.asList(socket.getEnabledCipherSuites()));

            //Put the configured preferredCipherSuites in front of the 'enabled' list, if they are supported by this java version.
            String[] preferredCipherSuites = preferredCipherSuitesPropertyValue.split(SEPARATOR);
            for (int index = 0; index < preferredCipherSuites.length; index++) {
                String preferredCipherSuite = preferredCipherSuites[index];
                if (enabledCipherSuites.contains(preferredCipherSuite)) {
                    enabledCipherSuites.remove(preferredCipherSuite);
                    enabledCipherSuites.add(index, preferredCipherSuite);
                } else {
                    String pattern = Environment.getDefault().getTranslation("preferredCipherSuiteIsNotSupportedByJavaVersion", "The preferred cipher suite '{0}' is not supported by your current java version.");
                    throw new ConnectionException(pattern, preferredCipherSuite);
                }
            }

            socket.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
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
        return "$Date: 2016-09-02 14:48:00 +0200 (Mon, 11 Jul 2016)$";
    }
}