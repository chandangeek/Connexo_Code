package com.elster.jupiter.pki;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * This is the main interface towards the PKI bundle. It provides access for the main PKI procedures.
 */
@ProviderType
public interface PkiService {

    enum AsymmetricKeyAlgorithms {
        RSA, DSA, ECDSA
    }

    String COMPONENTNAME = "PKI";

    /**
     * Creates a new, empty trust store in Connexo.
     * @param name unique identifier for trust store
     * @return Empty TrustStore
     */
    TrustStoreBuilder newTrustStore(String name);

    /**
     * Find trust store identified by name
     * @param name The trust store's unique name
     * @return TrustStore if found, if not: Optional.empty()
     */
    Optional<TrustStore> findTrustStore(String name);


    Optional<TrustStore> findAndLockTrustStoreByIdAndVersion(long id, long version);

    /**
     * Find trust store identified by id
     * @param id The trust store's unique id
     * @return TrustStore if found, if not: Optional.empty()
     */
    Optional<TrustStore> findTrustStore(long id);

    /**
     * Returns a lost of all trust stores
     * @return List of trust stores
     */
    List<TrustStore> getAllTrustStores();

    /**
     * Get a list of names of all KeyEncryptionMethods that registered through whiteboard.
     * @return List of key encryption method names
     */
    List<String> getKeyEncryptionMethods(CryptographicType cryptographicType);

    /**
     * Returns a list (through paged finder) for all existing keyTypes in Connexo
     * @return
     */
    Finder<KeyType> findAllKeyTypes();

    /**
     * Creates a new KeyType describing a symmetric key.
     * Required parameters are algorithm and depending on the algorithm: key size
     * @param name The name given to this key type. The name will be a unique identifier
     * @param keyAlgorithmName The algorithm for this key, e.g. AES, Blowfish, ...
     * @param keySize The size in bytes, e.g. 1024
     * @return The newly created, persisted KeyType
     */
    KeyTypeBuilder newSymmetricKeyType(String name, String keyAlgorithmName, int keySize);

    /**
     * Creates a KeyType for a certificate that will be used to proof identity towards other party. Allows specifying key
     * usages and extended key usages, so that CSR can be generated for certificate renewal.
     * Also creates describtion off an asymmetric key.
     * Required parameters are algorithm (currently only RSA, DSA and EC are supported) and depending on the algorithm: curve or key size
     * @param name The name given to this key type. The name will be a unique identifier.
     * @return A builder guiding you through the creation of an asymmetric key definition
     */
    ClientCertificateTypeBuilder newClientCertificateType(String name, String signingAlgorithm);

    /**
     * Creates a KeyType for a certificate.
     * @param name The type's name
     * @return a certificate type builder, allowing you to set description
     */
    CertificateTypeBuilder newCertificateType(String name);

    /**
     * Creates a KeyType for a trusted certificate. A trusted certificate is a certificate that will belong to a trust store.
     * @param name The type's name
     * @return a certificate type builder, allowing you to set the description.
     */
    CertificateTypeBuilder newTrustedCertificateType(String name);

    /**
     * Get an existing KeyType by name.
     * Returns Optional.empty() if not found
     * @param name The KpiType's name. The name is a unique identifier.
     * @return The KpiType if present, empty otherwise.
     */
    Optional<KeyType> getKeyType(String name);

    /**
     * Get a list of all known key types
     */
    List<KeyType> getKeyTypes();

    /**
     * Creates a new PrivateKeyWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param keyAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new private key wrapper of the required type and encryption method, without value.
     */
    PrivateKeyWrapper newPrivateKeyWrapper(KeyAccessorType keyAccessorType);

    /**
     * Creates a new SymmetricKeyWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param keyAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new symmetric key wrapper of the required type and encryption method, without value.
     */
    SymmetricKeyWrapper newSymmetricKeyWrapper(KeyAccessorType keyAccessorType);

    CertificateWrapper newCertificateWrapper(String alias);

    /**
     * Creates a new Client certificate wrapper.
     *
     * @param clientCertificateAccessorType The Key AccessorType describing the certificate and the private key
     * @return Persisted, empty ClientCertificateWrapper
     */
    ClientCertificateWrapper newClientCertificateWrapper(KeyAccessorType clientCertificateAccessorType);

    Optional<ClientCertificateWrapper> findClientCertificateWrapper(String alias);

    /**
     * Returns all non-trusted certificates, that means, all certificates for the 'certificate store'
     * @return All Certificates and ClientCertificates, TrustedCertificates will not be part of the list.
     */
    Finder<CertificateWrapper> findAllCertificates();

    public interface CertificateTypeBuilder {
        CertificateTypeBuilder description(String description);
        KeyType add();
    }

    public interface ClientCertificateTypeBuilder {
        ClientCertificateTypeBuilder description(String description);
        ClientCertificateTypeBuilder setKeyUsages(EnumSet<KeyUsage> keyUsages);
        ClientCertificateTypeBuilder setExtendedKeyUsages(EnumSet<ExtendedKeyUsage> keyUsages);
        AsyncKeySizeBuilder RSA();
        AsyncKeySizeBuilder DSA();
        AsyncCurveBuilder ECDSA();
    }

    public interface AsyncKeySizeBuilder {
        AsyncKeySizeBuilder keySize(int keySize);
        KeyType add();
    }

    public interface AsyncCurveBuilder {
        AsyncCurveBuilder curve(String curveName);
        KeyType add();
    }

    public interface TrustStoreBuilder {
        /**
         * Clarify the purpose of this TrustStore by adding a human understandable description.
         * This field is optional.
         */
        TrustStoreBuilder description(String description);

        TrustStore add();
    }

    public interface KeyTypeBuilder {
        KeyTypeBuilder description(String description);
        KeyType add();
    }

}
