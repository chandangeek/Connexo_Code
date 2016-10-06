package com.energyict.mdc.channels.ip.socket;

import com.energyict.cpo.Environment;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import com.energyict.mdw.core.DLMSKeyStoreParameters;
import com.energyict.mdw.core.DLMSKeyStoreUserFile;
import com.energyict.mdw.crypto.DLMSKeyStoreUserFileProviderImpl;
import com.energyict.protocol.exceptions.ConnectionException;
import sun.security.util.DerInputStream;
import sun.security.x509.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.cert.*;
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

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    protected ServerLoggableComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", getTLSVersionPropertyValue());

        try {

            final SSLContext sslContext = SSLContext.getInstance(getTLSVersionPropertyValue());
            DLMSKeyStoreUserFile dlmsKeyStoreUserFile = new DLMSKeyStoreUserFileProviderImpl().getKeyStoreUserFile();

            final TrustManager[] trustManagers = getTrustManagers(dlmsKeyStoreUserFile.findOrCreateDLMSTrustStore());
            final KeyManager[] keyManagers = getKeyManagers(dlmsKeyStoreUserFile.findOrCreateDLMSKeyStore());

            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) socketFactory.createSocket();
            socket.setNeedClientAuth(true);
            socket.setWantClientAuth(true);
            handlePreferredCipherSuites(socket);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupTLSConnection", "Failed to setup the TLS connection.");
            throw new ConnectionException(pattern, e);
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

    private TrustManager[] getTrustManagers(KeyStore keyStore) throws ConnectionException{
        TrustManager[] trustManagers = null;
        try {
            trustManagers = new TrustManager[]{new X509TrustManagerImpl(keyStore)};
        } catch (Exception e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupTrustManager", "Failed to setup a Trust Manager, TLS connection will not be setup.");
            throw new ConnectionException(pattern, e);
        }
        return trustManagers;
    }

    private KeyManager[] getKeyManagers(KeyStore keyStore) throws ConnectionException {
        KeyManager[] keyManagers = null;
        try {
            keyManagers = new KeyManager[]{new X509KeyManagerImpl(keyStore)};
        } catch (Exception e) {
            String pattern = Environment.getDefault().getTranslation("failedToSetupKeyManager", "Failed to setup a Key Manager, TLS connection will not be setup.");
            throw new ConnectionException(pattern, e);
        }
        return keyManagers;
    }

    private class X509TrustManagerImpl implements X509TrustManager {

        X509TrustManager x509TrustManager;

        X509TrustManagerImpl(KeyStore keyStore) throws Exception {

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager trustManagers [] = trustManagerFactory.getTrustManagers();

         /*
          * Search for a X509TrustManager and use it as default
          */
            for (int i = 0; i < trustManagers.length; i++) {
                if (trustManagers[i] instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManagers[i];
                    return;
                }
            }

            String pattern = Environment.getDefault().getTranslation("defaultTrustManagerNotFound", "A default trust manager could not be found, TLS Connection will not be established.");
            throw new ConnectionException(pattern);
        }

        /**
         * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root and return if it can be validated and is trusted for client SSL authentication based on the authentication type.
         * @param chain the peer certificate chain
         * @param authType the authentication type based on the client certificate
         * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException, IllegalArgumentException {
            try {
                x509TrustManager.checkClientTrusted(chain, authType);
            } catch (CertificateException e) {
                String pattern = Environment.getDefault().getTranslation("clientNotTrusted", "Based on provided certificate chain and authentication type, the client cannot be trusted.");
                throw new CertificateException(pattern, e);
            }
        }

        /**
         * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root and return if it can be validated and is trusted for server SSL authentication based on the authentication type.
         * @param chain the peer certificate chain
         * @param authType he key exchange algorithm used
         * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
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

    private class X509KeyManagerImpl implements X509KeyManager {

        X509KeyManager x509KeyManager;
        private KeyStore keyStore;

        X509KeyManagerImpl(KeyStore keyStore) throws Exception {
            this.keyStore = keyStore;
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keyStore, DLMSKeyStoreParameters.PARAMETERS);
            KeyManager[] keymanagers =  kmfactory.getKeyManagers();

            /*
          * Search for a X509KeyManager and use it as default
          */
            for (int i = 0; i < keymanagers.length; i++) {
                if (keymanagers[i] instanceof X509KeyManager) {
                    x509KeyManager = (X509KeyManager) keymanagers[i];
                    return;
                }
            }

            String pattern = Environment.getDefault().getTranslation("defaultKeyManagerNotFound", "A default key manager could not be found, TLS Connection will not be established.");
            throw new ConnectionException(pattern);
        }


        /**
         *  Get the matching aliases for authenticating the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         * @param keyType
         * @param issuers
         * @return
         */
        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            System.out.println("getClientAliases = "+x509KeyManager.getClientAliases(keyType, issuers));
            return x509KeyManager.getClientAliases(keyType, issuers);

        }

        /**
         * Choose an alias to authenticate the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         * @param keyType
         * @param issuers
         * @param socket
         * @return
         */
        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            String alias = x509KeyManager.chooseClientAlias(keyType, issuers, socket);
            return alias != null ? alias : getAlias(keyType, issuers);
        }

        /**
         * Get the matching aliases for authenticating the server side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         * @param keyType
         * @param issuers
         * @return
         */
        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return x509KeyManager.getServerAliases(keyType, issuers);
        }

        /**
         * Choose an alias to authenticate the server side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
         * @param keyType
         * @param issuers
         * @param socket
         * @return
         */
        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return x509KeyManager.chooseServerAlias(keyType, issuers, socket);
        }

        /**
         * Returns the certificate chain associated with the given alias.
         * @param alias
         * @return
         */
        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return x509KeyManager.getCertificateChain(alias);
        }

        /**
         * Returns the key associated with the given alias.
         * @param alias
         * @return
         */
        @Override
        public PrivateKey getPrivateKey(String alias) {
            return x509KeyManager.getPrivateKey(alias);
        }

        private String getAlias(String[] keyType, Principal[] issuers){
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

        private boolean isPublicKeyAlgorithmMatching(X509Certificate certificate, String[] keyType){
            String certPublicKeyAlgName = certificate.getPublicKey().getAlgorithm();
            for(String type: keyType){
                if(certPublicKeyAlgName.equalsIgnoreCase(type)){
                    return true;
                }
            }
            return false;
        }

        private boolean isMatchingCAIssuer(Principal[] issuers, List<Principal> caRootIssuersForCertIdentifiedByAlias) {
            for(Principal issuer: issuers){
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
            for(GeneralName generalName: generalNames.names()){
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

    @Override
    public String getVersion() {
        return "$Date: 2016-10-06 14:37:42 +0300 (Thu, 06 Oct 2016)$";
    }

}