package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.jca.JSSJCAProvider;
import com.atos.worldline.jss.api.jca.spec.KeyLabelKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class HsmKeyManagerImpl implements X509KeyManager {

    private final String clientTlsPrivateKeyAlias;
    private X509Certificate[] certificateChain;
    private X509KeyManager x509KeyManager;

    public HsmKeyManagerImpl(KeyStore keyStore, char[] password, String clientTlsPrivateKeyAlias, X509Certificate[] certificateChain) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        this.clientTlsPrivateKeyAlias = clientTlsPrivateKeyAlias;
        this.certificateChain = certificateChain;

        setHSMProvider();

        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keyStore, password);
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

    private void setHSMProvider() {
        //the bellow code is needed as a workaround for using JSSJCAProvider with priority over SunEC
        Security.insertProviderAt(new JSSJCAProvider(), 1);
    }

    /**
     * Get the matching aliases for authenticating the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
     */
    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        throw new UnsupportedOperationException("Method not supported on client side");
    }

    /**
     * Choose an alias to authenticate the client side of a secure socket given the public key type and the list of certificate issuer authorities recognized by the peer (if any).
     */
    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return clientTlsPrivateKeyAlias != null ? clientTlsPrivateKeyAlias : x509KeyManager.chooseClientAlias(keyType, issuers, socket);
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
        return certificateChain;
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
            throw new RuntimeException("A matching private key stored in HSM for provided alias: " + alias + ", could not be found. " + e);
        }
        return privateKey;
    }
}
