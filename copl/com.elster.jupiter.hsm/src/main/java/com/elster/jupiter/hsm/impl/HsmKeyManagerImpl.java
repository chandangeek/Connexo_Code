package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.jca.JSSJCAProvider;
import com.atos.worldline.jss.api.jca.spec.KeyLabelKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class HsmKeyManagerImpl implements X509KeyManager {

    private X509KeyManager x509KeyManager;

    public HsmKeyManagerImpl(KeyStore keyStore, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        final char[] PARAMETERS = new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        Optional<KeyStore> mockKeyStore = Optional.of(KeyStore.getInstance("JKS"));
        try {
            mockKeyStore.get().load(new FileInputStream("C:\\protocols to implement\\Salzburg\\certificates\\keystore.jks"), PARAMETERS);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        //TODO: use the received keyStore and pass instead of the mocked one
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(mockKeyStore.get(), PARAMETERS);
        KeyManager[] keyManagers = kmfactory.getKeyManagers();

        /*
         * Search for a X509KeyManager and use it as default
         */
        for (KeyManager keymanager : keyManagers) {
            if (keymanager instanceof X509KeyManager) {
                x509KeyManager = (X509KeyManager) keymanager;
                return;
            }
        }

        throw new KeyStoreException("A default key manager could not be found.");
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
//        String tlsAlias = getClientTLSAliasPropertyValue();
//        return tlsAlias != null ? tlsAlias :
        //TODO: check if we still need to take into account the property
        String clientAlias = x509KeyManager.chooseClientAlias(keyType, issuers, socket);
        return clientAlias;
    }

    /**
     * Not supported at the client side
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        throw new UnsupportedOperationException("Method not supported on client side");
    }

    /**
     * Not supported at the client side
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        throw new UnsupportedOperationException("Method not supported on client side");
    }

    /**
     * Returns the certificate chain associated with the given alias.
     */
    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return x509KeyManager.getCertificateChain(alias);
    }

    /**
     * Returns EC private key from a HSM associated with a given alias. Note that corresponding X.509 certificate
     * is stored in a keystore instead of HSM.
     *
     * @param alias the alias which refers EC private key in a HSM
     * @return the private key instance, otherwise, null
     */
    @Override
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey privateKey;
        try {
            KeyLabelKeySpec privateKeySpec = new KeyLabelKeySpec(alias);
            KeyFactory keyFactory = KeyFactory.getInstance(JSSJCAProvider.ALGORITHM_NAME_EC, JSSJCAProvider.NAME);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new RuntimeException("A matching alias for private key stored in HSM could not be found. " + e);
        }
        return privateKey;
    }
}
