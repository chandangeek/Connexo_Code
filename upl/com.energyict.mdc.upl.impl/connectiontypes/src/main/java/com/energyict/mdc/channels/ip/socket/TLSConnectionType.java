package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import sun.security.util.DerInputStream;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.KEY;
import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.TRUST;

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
    private static final String CLIENT_TLS_ALIAS = "ClientTLSAlias";
    private static final String SEPARATOR = ",";

    private final NlsService nlsService;
    private final X509Service x509Service;
    private final KeyStoreService keyStoreService;
    private Logger logger;

    public TLSConnectionType(PropertySpecService propertySpecService, NlsService nlsService, X509Service x509Service, KeyStoreService keyStoreService) {
        super(propertySpecService);
        this.nlsService = nlsService;
        this.x509Service = x509Service;
        this.keyStoreService = keyStoreService;
    }

    /**
     * The version of the TLS protocol to be used.
     * Defaults to TLSv1.2
     */
    private PropertySpec tlsVersionPropertySpec() {
        return this.stringWithDefault(TLS_VERSION_PROPERTY_NAME, PropertyTranslationKeys.TLS_VERSION ,TLS_DEFAULT_VERSION);
    }

    /**
     * A comma-separated list of cipher suites that are preferred by the client (ComServer)
     */
    private PropertySpec preferredCipheringSuitesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PREFERRED_CIPHER_SUITES_PROPERTY_NAME, false, PropertyTranslationKeys.TLS_PREFERRED_CIPHER_SUITES, getPropertySpecService()::stringSpec).finish();
    }

    /**
     * The alias of the TLS private key.
     */
    private PropertySpec tlsAliasPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CLIENT_TLS_ALIAS, false, PropertyTranslationKeys.TLS_CLIENT_TLS_ALIAS, getPropertySpecService()::stringSpec).finish();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(tlsVersionPropertySpec());
        propertySpecs.add(preferredCipheringSuitesPropertySpec());
        propertySpecs.add(tlsAliasPropertySpec());
        return propertySpecs;
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

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    protected ComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        System.setProperty("jdk.tls.client.protocols", getTLSVersionPropertyValue());

        try {
            final SSLContext sslContext = SSLContext.getInstance(getTLSVersionPropertyValue());

            final TrustManager[] trustManagers = getTrustManagers(this.keyStoreService.findOrCreate(TRUST, "DLMS"));
            final KeyManager[] keyManagers = getKeyManagers(this.keyStoreService.findOrCreate(KEY, "DMLS"));

            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            SSLSocket socket = (SSLSocket) socketFactory.createSocket();
            handlePreferredCipherSuites(socket);
            socket.connect(new InetSocketAddress(host, port), timeOut);

            return new SocketComChannel(socket);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.FailedToSetupTLSConnection, e);
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
                    throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.PreferredCipherSuiteIsNotSupportedByJavaVersion, preferredCipherSuite);
                }
            }

            socket.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
        }
    }

    /**
     * Create a TrustManager based on the persisted trust store of EIServer. This contains sub-CA and root-CA certificates.
     */
    private TrustManager[] getTrustManagers(KeyStore trustStore) throws ConnectionException {
        try {
            return new TrustManager[]{new X509TrustManagerImpl(trustStore)};
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.FailedToSetupTrustManager, e);
        }
    }

    /**
     * Create a KeyManager based on the persisted key store of EIServer. This contains the private key for TLS and its matching certificate.
     */
    private KeyManager[] getKeyManagers(KeyStore keyStore) throws ConnectionException {
        try {
            return new KeyManager[]{new X509KeyManagerImpl(keyStore)};
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.FailedToSetupKeyManager, e);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-10-24 09:40:46 +0200 (Mon, 24 Oct 2016)$";
    }

    private class X509TrustManagerImpl implements X509TrustManager {

        X509TrustManager x509TrustManager;

        X509TrustManagerImpl(KeyStore trustStore) throws ConnectionException, KeyStoreException, NoSuchAlgorithmException {
            x509Service.getTrustManager(trustStore).orElseThrow(() -> new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.DefaultTrustManagerNotFound));
        }

        /**
         * Not supported at the client side
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws UnsupportedOperationException {
            throw unsupportedOperationException();
        }

        /**
         * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root and return if it can be validated and is trusted for server SSL authentication based on the authentication type.
         *
         * @param chain the peer certificate chain
         * @param authType he key exchange algorithm used
         * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                x509TrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                throw new CertificateException(nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(MessageSeeds.ServerNotTrusted).format(), e);
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

        X509KeyManagerImpl(KeyStore keyStore) throws NoSuchAlgorithmException, java.security.UnrecoverableKeyException, KeyStoreException, ConnectionException {
            this.keyStore = keyStore;
            this.x509KeyManager = x509Service.getKeyManager(keyStore).orElseThrow(() -> new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.DefaultKeyManagerNotFound));
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
            if (tlsAlias != null) {
                return tlsAlias;
            } else {
                return getAlias(keyType, issuers);
            }
        }

        /**
         * Not supported at the client side
         */
        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            throw unsupportedOperationException();
        }

        /**
         * Not supported at the client side
         */
        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            throw unsupportedOperationException();
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
            try {
                GeneralNames generalNames = (GeneralNames) authorityKeyIdentifierExtension.get(AuthorityKeyIdentifierExtension.AUTH_NAME);
                for (GeneralName generalName : generalNames.names()) {
                    X500Name name = (X500Name) generalName.getName();
                    caPrincipals.add(name.asX500Principal());
                }
            } catch (IOException e) {
                getLogger().warning("Unable to get GeneralNames from AuthorityKeyIdentifierExtension object. " + e);
            }

            return caPrincipals;
        }

        private AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension(X509Certificate certificate) {
            String oid = PKIXExtensions.AuthorityKey_Id.toString();
            byte[] extensionValue = certificate.getExtensionValue(oid);
            DerInputStream in;
            try {
                in = new DerInputStream(extensionValue);
                return new AuthorityKeyIdentifierExtension(false, in.getOctetString());
            } catch (IOException e) {
                getLogger().warning("Unable to get AuthorityKeyIdentifierExtension. " + e);
            }

            return null;
        }
    }

    protected UnsupportedOperationException unsupportedOperationException() {
        return new UnsupportedOperationException(MessageSeeds.NotSupportedOnClient.getDefaultFormat());
    }

    private PropertySpec stringWithDefault(String name, TranslationKey translationKey, String defaultValue) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

}