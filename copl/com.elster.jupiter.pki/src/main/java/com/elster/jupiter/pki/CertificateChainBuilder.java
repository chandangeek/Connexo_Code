package com.elster.jupiter.pki;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.LinkedList;


public class CertificateChainBuilder {

    /**
     *
     * @param certChain certificate chain to be added into keystore
     * @param keyStore keystore that will be populated
     * @param parameters used to set key entry (for each entry in key store)
     * @throws KeyStoreException
     * @throws InvalidKeyException
     */
    public static void populateKeyStore(LinkedList<ClientCertificateWrapper> certChain, KeyStore keyStore, char[] parameters) throws KeyStoreException, InvalidKeyException {
        Certificate[] certs = new Certificate[certChain.size()];
        int i=0;
        while (certChain.peek() != null) {
            ClientCertificateWrapper cert = certChain.pollFirst();
            certs[i] = cert.getCertificate().get();
            i++;
            keyStore.setKeyEntry(
                    cert.getAlias(),
                    cert.getPrivateKeyWrapper().getPrivateKey().get(),
                    parameters,
                    Arrays.copyOf(certs, i));
        }
    }

    public static LinkedList<CertificateWrapper> getCertificateChain(CertificateWrapper clientCertificateWrapper) {
        LinkedList<CertificateWrapper> certChain = new LinkedList<>();
        do {
            certChain.addFirst(clientCertificateWrapper);
            clientCertificateWrapper = clientCertificateWrapper.getParent();
        } while (clientCertificateWrapper != null);
        return certChain;
    }

}
