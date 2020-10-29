package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channel.ip.socket.SocketComChannel;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 27/07/2016 - 15:00
 */
public class TLSConnectionType extends OutboundTcpIpConnectionType {

    public static final String TLS_VERSION_PROPERTY_NAME = "TLSVersion";
    public static final String CLIENT_TLS_PRIVATE_KEY = "ClientTLSPrivateKey";
    public static final String SERVER_TLS_CERTIFICATE = "ServerTLSCertificate";
    public static final String PREFERRED_CIPHER_SUITES_PROPERTY_NAME = "PreferredCipherSuites";
    public static final String TLS_DEFAULT_VERSION = "TLSv1.2";
    private static final String SEPARATOR = ",";

    private final CertificateWrapperExtractor certificateWrapperExtractor;
    private Logger logger;

    /**
     * Maximum length of certificate chain
     */
    private static final int MAX_CERT_CHAIN_LENGTH = 8;

    public TLSConnectionType(PropertySpecService propertySpecService, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(propertySpecService);
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    /**
     * The version of the TLS protocol to be used.
     * Defaults to TLSv1.2
     */
    private PropertySpec tlsVersionPropertySpec() {
        return this.stringWithDefault(TLS_VERSION_PROPERTY_NAME, PropertyTranslationKeys.TLS_VERSION, TLS_DEFAULT_VERSION);
    }

    /**
     * A comma-separated list of cipher suites that are preferred by the client (ComServer)
     */
    private PropertySpec preferredCipheringSuitesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PREFERRED_CIPHER_SUITES_PROPERTY_NAME, false, PropertyTranslationKeys.TLS_PREFERRED_CIPHER_SUITES, getPropertySpecService()::stringSpec).finish();
    }

    /**
     * The client TLS private key. Should be a ClientCertificateWrapper containing the client Certificate and the client PrivateKey.
     */
    private PropertySpec tlsClientPrivateKeyPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CLIENT_TLS_PRIVATE_KEY, false, PropertyTranslationKeys.CLIENT_TLS_PRIVATE_KEY, () -> getPropertySpecService().referenceSpec(KeyAccessorType.class.getName())).finish();
    }

    protected CertificateWrapper getTlsClientPrivateKey() {
        return (CertificateWrapper) this.getProperty(CLIENT_TLS_PRIVATE_KEY);
    }

    /**
     * The server TLS certificate. Should be a CertificateWrapper that is linked to a TrustStore.
     */
    private PropertySpec tlsServerCertificatePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(SERVER_TLS_CERTIFICATE, true, PropertyTranslationKeys.SERVER_TLS_CERTIFICATE, () -> getPropertySpecService().referenceSpec(KeyAccessorType.class.getName())).finish();
    }

    private CertificateWrapper getTlsServerCertificate() {
        return (CertificateWrapper) this.getProperty(SERVER_TLS_CERTIFICATE);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(tlsVersionPropertySpec());
        propertySpecs.add(preferredCipheringSuitesPropertySpec());
        propertySpecs.add(tlsClientPrivateKeyPropertySpec());
        propertySpecs.add(tlsServerCertificatePropertySpec());
        return propertySpecs;
    }

    private String getTLSVersionPropertyValue() {
        return (String) this.getProperty(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    private String getPreferredCipherSuitesPropertyValue() {
        return (String) this.getProperty(PREFERRED_CIPHER_SUITES_PROPERTY_NAME);
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    public ComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", getTLSVersionPropertyValue());

        try {
            final SSLContext sslContext = SSLContext.getInstance(getTLSVersionPropertyValue());
            //TODO: As now the truststore and keystore comes from core and not created here, we need to see who will do the specific work we had on EIServer to validate certificates, CRL and so on
            X509TrustManager trustManager = certificateWrapperExtractor.getTrustManager(getTlsServerCertificate()).get();           //This contains sub-CA and root-CA certificates.
            X509KeyManager keyManager = getKeyManager();                  //This contains the private key for TLS and its matching certificate.

            sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) socketFactory.createSocket();
            handlePreferredCipherSuites(socket);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException | CertificateException e) {
            getLogger().severe("Security exception:" + e.getMessage());
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.FailedToSetupTLSConnection, e);
        }
    }

    protected X509KeyManager getKeyManager() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException, UnrecoverableKeyException, ConnectionException {
        return certificateWrapperExtractor.getKeyManager(getTlsClientPrivateKey()).get();
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
                    throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.PreferredCipherSuiteIsNotSupportedByJavaVersion, preferredCipherSuite);
                }
            }

            socket.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-10-27 14:22:27 +0200 (Thu, 27 Oct 2016)$";
    }

    private PropertySpec stringWithDefault(String name, TranslationKey translationKey, String defaultValue) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }
}