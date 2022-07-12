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
import java.io.PrintWriter;
import java.io.StringWriter;
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
    public static final String CLIENT_TLS_ALIAS = "ClientTLSAlias";
    public static final String CLIENT_TLS_PRIVATE_KEY = "ClientTLSPrivateKey";
    public static final String SERVER_TLS_CERTIFICATE = "ServerTLSCertificate";
    public static final String PREFERRED_CIPHER_SUITES_PROPERTY_NAME = "PreferredCipherSuites";
    public static final String TLS_DEFAULT_VERSION = "TLSv1.2";
    private static final String SEPARATOR = ",";
    public static final String DEFAULT_SECURE_RANDOM_ALG_SHA_1_PRNG = "SHA1PRNG";
    public static final String DEFAULT_SECURE_RANDOM_PROVIDER_SUN = "SUN";
    public static final String COM_ATOS_WORLDLINE_JSS_API_FUNCTION_TIMED_OUT_EXCEPTION = "com.atos.worldline.jss.api.FunctionTimedOutException";

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


    /**
     * The alias of the TLS private key.
     */
    private PropertySpec tlsAliasPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CLIENT_TLS_ALIAS, true, PropertyTranslationKeys.CLIENT_TLS_ALIAS, () -> getPropertySpecService().referenceSpec(KeyAccessorType.class.getName())).finish();
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
        propertySpecs.add(tlsAliasPropertySpec());
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
        String logPrefix = String.format("[TLS:%s:%d] ", host ,port);
        getLogger().info(logPrefix+"Setting up a new TLS connection to "+host+":"+port+" (timeout="+timeOut+")");
        String tlsVersion = getTLSVersionPropertyValue();
        System.setProperty("jdk.tls.client.protocols", tlsVersion);

        try {
            getLogger().info(logPrefix+"Getting SSLContext for: "+tlsVersion);
            final SSLContext sslContext = SSLContext.getInstance(tlsVersion);  // Oracle uses only TLS as example

            getLogger().info(logPrefix+"Creating trustManager");
            X509TrustManager trustManager = getTrustManager();             //This contains sub-CA and root-CA certificates.

            getLogger().info(logPrefix+"Creating keyManager");

            X509KeyManager keyManager = getKeyManager();                  //This contains the private key for TLS and its matching certificate.

            getLogger().info(logPrefix+"Initializing SSL context");
            sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager}, getSecureRandom(logPrefix));

            getLogger().info(logPrefix+"Creating socket factory");
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            getLogger().info(logPrefix+"Creating socket");
            SSLSocket socket = (SSLSocket) socketFactory.createSocket();

            getLogger().info(logPrefix+"Setting TLS enabled protocols: "+tlsVersion);
            socket.setEnabledProtocols(new String[]{tlsVersion});
            setPreferredCipherSuites(socket);

            getLogger().info(logPrefix+"Socket created, connecting TCP "+tlsVersion);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            getLogger().info(logPrefix+"TCP Socket connected, starting TLS handshake "+tlsVersion);
            performTLSHandshakeWithRetries(socket, logPrefix);

            getLogger().info(logPrefix+"TLS Socket is ready for communication "+tlsVersion);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException | CertificateException e) {
            getLogger().severe("Security exception:" + e.getMessage());
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.FailedToSetupTLSConnection, e);
        }
    }

    /**
     * Catch any HSM timeouts and retry.
     * This can happen because JSS runtime is the first security provider, and all crypto functions are relayed to HSM
     */
    protected void performTLSHandshakeWithRetries(SSLSocket socket, String logPrefix) throws IOException, SSLException {
        int retry = 5; // usually the first retry is enough
        while (retry>0) {
            try {
                socket.startHandshake();
            } catch (SSLException e) {
                // catch generic SSL Exceptions and check if there is caused by HSM Timeout
                if (isHSMFunctionTimedOutException(e) && (retry > 1)) {
                    retry--;
                    getLogger().warning(logPrefix + "HSM Timeout detected " + e.getLocalizedMessage() + "; will retry " + retry + " more times");
                } else {
                    throw e;
                }
            } catch (IOException e) {
                // any other exception will be handled in upper layers
                throw e;
            }
        }
    }

    /**
     * Helper function to detect if the exception is caused by an HSM timeout, without adding a dependency to HSM or JSS
     */
    private boolean isHSMFunctionTimedOutException(SSLException e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);

        return e.toString().contains(COM_ATOS_WORLDLINE_JSS_API_FUNCTION_TIMED_OUT_EXCEPTION);
    }

    /**
     * Because we use the JSS runtime is used as default to be able to handle the private key
     * For random generator we have to use the native SUN provider,
     *      else it will use the true-random from JSS, which is very resource intensive!
     *
     * @param logPrefix just for clear output
     * @return either the default random from sun, or the fallback from JSS
     */
    private SecureRandom getSecureRandom(String logPrefix) {

        try {
            SecureRandom secureRandom = SecureRandom.getInstance(DEFAULT_SECURE_RANDOM_ALG_SHA_1_PRNG, DEFAULT_SECURE_RANDOM_PROVIDER_SUN);
            getLogger().info(logPrefix+"Using default SUN random generator for TLS handshake.");
            return secureRandom;
        } catch (Exception e) {
            getLogger().warning(logPrefix+"Falling back to default random generator (JSS), cannot use the native random generator: "+e.getMessage());
        }

        return new SecureRandom();
    }

    /**
     * Use this to set the algorithm
     *          ssl.TrustManagerFactory.algorithm=SunX509
     */
    private X509TrustManager getTrustManager() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        CertificateWrapper tlsServerCertificate = getTlsServerCertificate();
        return certificateWrapperExtractor.getTrustManager(tlsServerCertificate).get();
    }

    protected X509KeyManager getKeyManager() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException, UnrecoverableKeyException, ConnectionException {
        return certificateWrapperExtractor.getKeyManager(getTlsClientPrivateKey()).get();
    }

    private void setPreferredCipherSuites(SSLSocket socket) throws ConnectionException {
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
        return "Date: 2022-07-12";
    }

    private PropertySpec stringWithDefault(String name, TranslationKey translationKey, String defaultValue) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }
}
