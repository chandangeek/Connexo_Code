package com.energyict.mdc.engine.impl;

import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.datavault.PersistentKeyStore;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.KEY;
import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.TRUST;

/**
 * Represents the supported functionality to get and set private keys and certificates to the persisted keystore and truststore.
 * Note that the keystore only contains client information.
 * Note that the truststore only contains subCa and rootCA certificates.
 * The server end-device certificates are stored as {@link com.energyict.mdc.upl.security.CertificateWrapper}s, not in this truststore.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/01/2016 - 15:18
 */
public class DLMSKeyStoreUserFile {

    static final char[] PARAMETERS = new char[]{'i', '#', '?', 'r', 'P', '1', '_', 'L', 'v', '/', 'T', '@', '>', 'k', 'h', '*'};
    private static final String KEY_STORE_EXTENSION = "p12";
    private static final String TRUST_STORE_EXTENSION = "JCEKS";
    private static final String KEY_STORE_NAME = "keystore";
    private static final String TRUST_STORE_NAME = "truststore";
    private static final String COMMON_NAME = "CN";
    private static final String CERTIFICATE_AUTHORITY_SUFFIX = "CA";

    private final KeyStoreService keyStoreService;

    public DLMSKeyStoreUserFile(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    /**
     * Load the existing keyStoreName.keyStoreExtension key store of type keyStoreType from the database if it exists.
     * If it does not exist yet, create a new, empty keystore and persist it as a userfile.
     */
    public KeyStore findOrCreateDLMSKeyStore() {
        return findOrCreateDLMSStore(KEY, KEY_STORE_NAME, KEY_STORE_EXTENSION).forReading(PARAMETERS);
    }

    private PersistentKeyStore findOrCreateDLMSTrustStore() {
        return findOrCreateDLMSStore(TRUST, TRUST_STORE_NAME, TRUST_STORE_EXTENSION);
    }

    private PersistentKeyStore findOrCreateDLMSStore(com.energyict.mdc.upl.crypto.KeyStoreService.StoreType type, String storeName, String storeExtension) {
        return this.keyStoreService
                    .findSystemDefined(toStoreName(storeName, storeExtension))
                    .orElseGet(() -> this.createDLMSStore(type, storeName, storeExtension));
    }

    private PersistentKeyStore createDLMSStore(com.energyict.mdc.upl.crypto.KeyStoreService.StoreType type, String name, String extension) {
        try {
            return this.keyStoreService
                    .newSystemDefinedKeyStore(toStoreName(name, extension), type.getStoreTypeValue())
                    .build(PARAMETERS);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to load the DLMS key store from user file", e);
        }
    }

    private String toStoreName(String storeName, String extension) {
        return storeName + "." + extension;
    }

    public void importCertificate(Certificate certificate, String alias) {
        try {
            if (!(certificate instanceof X509Certificate)) {
                throw new IllegalArgumentException("Invalid certificate format, currently only X.509 v3 is supported.");
            }
            X509Certificate x509Certificate = (X509Certificate) certificate;
            x509Certificate.checkValidity();

            PersistentKeyStore persistentKeyStore = findOrCreateDLMSTrustStore();
            KeyStore trustStore = persistentKeyStore.forReading(PARAMETERS);

            //Find the subject CN of the given certificate
            String subjectCommonName = "";
            LdapName ldapName = new LdapName(x509Certificate.getSubjectX500Principal().getName());
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase(COMMON_NAME)) {
                    subjectCommonName = rdn.getValue().toString();
                    break;
                }
            }

            //Naming scheme for the Sub-CA instance: The subject CN shall finish with “CA” so that the CA function is recognized.
            if (!subjectCommonName.endsWith(CERTIFICATE_AUTHORITY_SUFFIX)) {
                throw new IllegalArgumentException("The subject Common Name (CN) of a certificate authority certificate must end with 'CA'");
            }

            if (trustStore.containsAlias(alias)) {
                Certificate existingCertificate = trustStore.getCertificate(alias);
                if (existingCertificate.equals(certificate)) {
                    //The exact certificate already exists in the key store.
                    return;
                } else {
                    String name = ((X509Certificate) existingCertificate).getSubjectDN().getName();
                    //There's a different certificate currently stored under this alias!
                    throw new IllegalArgumentException("Another certificate already exists (Subject: '" + name + "') in the key store, under alias '" + alias + "'! Remove it first.");
                }
            }

            if (isSubjectEqualToIssuer(x509Certificate)) {
                validateRootCACertificate(x509Certificate);
            } else {
                validateCertificateIsTrusted(x509Certificate, trustStore);
            }

            persistentKeyStore
                    .forUpdating(PARAMETERS)
                    .setCertificateEntry(alias, certificate)
                    .save();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | SignatureException | InvalidKeyException | InvalidNameException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isSubjectEqualToIssuer(X509Certificate certificate) {
        return certificate.getIssuerDN().equals(certificate.getSubjectDN());
    }

    /**
     * A new root CA certificate should be self signed
     */
    private void validateRootCACertificate(X509Certificate certificate) throws KeyStoreException, NoSuchAlgorithmException, CertificateEncodingException, SignatureException, InvalidKeyException {
        Signature signingImpl = Signature.getInstance(certificate.getSigAlgName());
        signingImpl.initVerify(certificate);
        signingImpl.update(certificate.getTBSCertificate());
        if (!signingImpl.verify(certificate.getSignature())) {
            throw new IllegalArgumentException("The provided certificate was not self signed");
        }
    }

    /**
     * A new client certificate should be signed by a known sub or root CA.
     */
    private void validateCertificateIsTrusted(X509Certificate certificate, KeyStore keyStore) throws KeyStoreException, NoSuchAlgorithmException, CertificateEncodingException, SignatureException, InvalidKeyException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String knownAlias = aliases.nextElement();
            Certificate caCertificate = keyStore.getCertificate(knownAlias);
            if (certificate.getIssuerDN().equals(((X509Certificate) caCertificate).getSubjectDN())) {
                Signature signingImpl = Signature.getInstance(certificate.getSigAlgName());
                signingImpl.initVerify(caCertificate);
                signingImpl.update(certificate.getTBSCertificate());
                if (signingImpl.verify(certificate.getSignature())) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Could not find an existing sub CA or root CA certificate to trust the given certificate. Rejected.");
    }

}