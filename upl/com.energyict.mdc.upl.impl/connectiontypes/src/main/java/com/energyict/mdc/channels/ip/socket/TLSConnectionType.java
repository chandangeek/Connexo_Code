package com.energyict.mdc.channels.ip.socket;

import com.energyict.cpo.Environment;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import com.energyict.mdw.core.DLMSKeyStoreParameters;
import com.energyict.mdw.core.DLMSKeyStoreUserFile;
import com.energyict.mdw.core.DLMSKeyStoreUserFileProvider;
import com.energyict.mdw.crypto.DLMSKeyStoreUserFileProviderImpl;
import com.energyict.protocol.exceptions.ConnectionException;
import sun.security.util.DerInputStream;
import sun.security.x509.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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
    private static final String TLS_DEFAULT_VERSION = "TLSv1.2";
    private static final String PREFERRED_CIPHER_SUITES_PROPERTY_NAME = "PreferredCipherSuites";
    private static final String SEPARATOR = ",";
    private Logger logger;

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

    /**
     * The alias of the TLS private key.
     */
    private PropertySpec tlsAliasPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(CLIENT_TLS_ALIAS);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = super.getOptionalProperties();
        optionalProperties.add(tlsVersionPropertySpec());
        optionalProperties.add(preferredCipheringSuitesPropertySpec());
        optionalProperties.add(tlsAliasPropertySpec());
        return optionalProperties;
    }

    private String getTLSVersionPropertyValue() {
        return (String) this.getProperty(TLS_VERSION_PROPERTY_NAME, TLS_DEFAULT_VERSION);
    }

    private String getPreferredCipherSuitesPropertyValue() {
        return (String) this.getProperty(PREFERRED_CIPHER_SUITES_PROPERTY_NAME);
    }

    private String getClientTLSAliasPropertyValue() {
        return (String) this.getProperty(CLIENT_TLS_ALIAS);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case TLS_VERSION_PROPERTY_NAME:
                return tlsVersionPropertySpec();
            case PREFERRED_CIPHER_SUITES_PROPERTY_NAME:
                return preferredCipheringSuitesPropertySpec();
            case CLIENT_TLS_ALIAS:
                return tlsAliasPropertySpec();
            default:
                return super.getPropertySpec(name);
        }
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    protected ServerLoggableComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", getTLSVersionPropertyValue());       //Workaround to get TLS fully working on java 1.7

        try {
            final SSLContext sslContext = SSLContext.getInstance(getTLSVersionPropertyValue());
            DLMSKeyStoreUserFile dlmsKeyStoreUserFile = getDlmsKeyStoreUserFile().getKeyStoreUserFile();

            final TrustManager[] trustManagers = getTrustManagers(dlmsKeyStoreUserFile.findOrCreateDLMSTrustStore());
            final KeyManager[] keyManagers = getKeyManagers(dlmsKeyStoreUserFile.findOrCreateDLMSKeyStore());

            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) socketFactory.createSocket();
            handlePreferredCipherSuites(socket);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupTLSConnection", "Failed to setup the TLS connection.");
            throw new ConnectionException(pattern, e);
        }
    }

    protected DLMSKeyStoreUserFileProvider getDlmsKeyStoreUserFile() {
        return new DLMSKeyStoreUserFileProviderImpl();
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

    /**
     * Create a TrustManager based on the persisted trust store of EIServer. This contains sub-CA and root-CA certificates.
     */
    protected TrustManager[] getTrustManagers(KeyStore trustStore) throws ConnectionException {
        try {
            return new TrustManager[]{new X509TrustManagerImpl(trustStore)};
        } catch (Exception e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupTrustManager", "Failed to setup a Trust Manager, TLS connection will not be setup.");
            throw new ConnectionException(pattern, e);
        }
    }

    /**
     * Create a KeyManager based on the persisted key store of EIServer. This contains the private key for TLS and its matching certificate.
     */
    protected KeyManager[] getKeyManagers(KeyStore keyStore) throws ConnectionException {
        try {
            return new KeyManager[]{new X509KeyManagerImpl(keyStore)};
        } catch (Exception e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupKeyManager", "Failed to setup a Key Manager, TLS connection will not be setup.");
            throw new ConnectionException(pattern, e);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-10-27 13:09:53 +0200 (Thu, 27 Oct 2016)$";
    }

    protected class X509TrustManagerImpl implements X509TrustManager {

        X509TrustManager x509TrustManager;

        X509TrustManagerImpl(KeyStore trustStore) throws Exception {

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager trustManagers[] = trustManagerFactory.getTrustManagers();

            /*
             * Search for a X509TrustManager and use it as default
             */
            for (TrustManager trustManager : trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    return;
                }
            }

            String pattern = Environment.getDefault().getTranslation("defaultTrustManagerNotFound", "A default trust manager could not be found, TLS Connection will not be established.");
            throw new ConnectionException(pattern);
        }

        /**
         * Not supported at the client side
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws UnsupportedOperationException {
            String pattern = Environment.getDefault().getTranslation("notSupportedOnClient", "Method not supported on client side");
            throw new UnsupportedOperationException(pattern);
        }

        /**
         * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root and return if it can be validated and is trusted for server SSL authentication based on the authentication type.
         *
         * @param chain    the peer certificate chain
         * @param authType he key exchange algorithm used
         * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                x509TrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                String pattern = Environment.getDefault().getTranslation("serverNotTrusted", "Based on provided certificate chain and authentication type, the server cannot be trusted");
                throw new CertificateException(pattern, e);
            }
        }

        /**
         * Return an array of certificate authority certificates which are trusted for authenticating peers.
         */
        public X509Certificate[] getAcceptedIssuers() {
            return x509TrustManager.getAcceptedIssuers();
        }
    }

    protected class X509KeyManagerImpl implements X509KeyManager {

        X509KeyManager x509KeyManager;
        protected KeyStore keyStore;

        X509KeyManagerImpl(KeyStore keyStore) throws Exception {
            this.keyStore = keyStore;
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keyStore, DLMSKeyStoreParameters.PARAMETERS);
            KeyManager[] keymanagers = kmfactory.getKeyManagers();

            /*
             * Search for a X509KeyManager and use it as default
             */
            for (KeyManager keymanager : keymanagers) {
                if (keymanager instanceof X509KeyManager) {
                    x509KeyManager = (X509KeyManager) keymanager;
                    return;
                }
            }

            String pattern = Environment.getDefault().getTranslation("defaultKeyManagerNotFound", "A default key manager could not be found, TLS Connection will not be established.");
            throw new ConnectionException(pattern);
        }

        /**
         * Get the matching aliases for authenticating the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         */
        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return x509KeyManager.getClientAliases(keyType, issuers);
        }

        /**
         * Choose an alias to authenticate the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         */
        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            String tlsAlias = getClientTLSAliasPropertyValue();
            return tlsAlias != null ? tlsAlias : getAlias(keyType, issuers);
        }

        /**
         * Not supported at the client side
         */
        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            String pattern = Environment.getDefault().getTranslation("notSupportedOnClient", "Method not supported on client side");
            throw new UnsupportedOperationException(pattern);
        }

        /**
         * Not supported at the client side
         */
        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            String pattern = Environment.getDefault().getTranslation("notSupportedOnClient", "Method not supported on client side");
            throw new UnsupportedOperationException(pattern);
        }

        /**
         * Returns the certificate chain associated with the given alias.
         */
        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return x509KeyManager.getCertificateChain(alias);
        }

        /**
         * Returns the key associated with the given alias.
         */
        @Override
        public PrivateKey getPrivateKey(String alias) {
            return x509KeyManager.getPrivateKey(alias);
        }

        private String getAlias(String[] keyType, Principal[] issuers) {
            try {
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (isPublicKeyAlgorithmMatching(certificate, keyType)) {
                        AuthorityKeyIdentifierExtension authorityKeyIdentifierExtension = getAuthorityKeyIdentifierExtension(certificate);
                        List<Principal> caRootIssuersForCertIdentifiedByAlias = getCAPrincipalsFromAKIE(authorityKeyIdentifierExtension);
                        if (isMatchingCAIssuer(issuers, caRootIssuersForCertIdentifiedByAlias)) {
                            return alias;
                        }
                    }
                }
            } catch (KeyStoreException e) {
                getLogger().warning("A matching alias could not be found for the given public key type and CA issuers. " + e);
            }
            return null;
        }

        private boolean isPublicKeyAlgorithmMatching(X509Certificate certificate, String[] keyType) {
            String certPublicKeyAlgName = certificate.getPublicKey().getAlgorithm();
            for (String type : keyType) {
                if (certPublicKeyAlgName.equalsIgnoreCase(type)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isMatchingCAIssuer(Principal[] issuers, List<Principal> caRootIssuersForCertIdentifiedByAlias) {
            for (Principal issuer : issuers) {
                if (caRootIssuersForCertIdentifiedByAlias.contains(issuer)) {
                    getLogger().info("Matching CA issuer found, return alias");
                    return true;
                }
            }
            return false;
        }

        private List<Principal> getCAPrincipalsFromAKIE(AuthorityKeyIdentifierExtension authorityKeyIdentifierExtension) {
            List<Principal> caPrincipals = new ArrayList<>();
            GeneralNames generalNames = null;
            try {
                generalNames = (GeneralNames) authorityKeyIdentifierExtension.get(AuthorityKeyIdentifierExtension.AUTH_NAME);
            } catch (IOException e) {
                getLogger().warning("Unable to get GeneralNames from AuthorityKeyIdentifierExtension object. " + e);
            }
            for (GeneralName generalName : generalNames.names()) {
                X500Name name = (X500Name) generalName.getName();
                caPrincipals.add(name.asX500Principal());
            }

            return caPrincipals;
        }

        private AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension(X509Certificate certificate) {
            String oid = PKIXExtensions.AuthorityKey_Id.toString();
            byte[] extensionValue = certificate.getExtensionValue(oid);
            DerInputStream in = null;
            try {
                in = new DerInputStream(extensionValue);
                return new AuthorityKeyIdentifierExtension(false, in.getOctetString());
            } catch (IOException e) {
                getLogger().warning("Unable to get AuthorityKeyIdentifierExtension. " + e);
            }

            return null;
        }
    }
}