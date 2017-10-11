package com.elster.jupiter.pki;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * This is the main interface for security management. It provides access for the HSM & datavault keys, trust store, key types,
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
     * Returns a list of all keypair wrappers that exist in the system. There is expected to be a limited set of keypair wrappers.
     * Devices e.g. will only contain certificates, not uncertified keypairs.
     * @return List of existing keypairs, sorted by alias
     */
    List<KeypairWrapper> getAllKeyPairs();

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
     * Creates a KeyType for a password. A password has a few parameters: length and possible characters in it.
     * @param name The type's name
     * @return a password key type builder, allowing you to set properties as length and character sets
     */
    PasswordTypeBuilder newPassphraseType(String name);


    /**
     * List the PropertySpecs that can be expected for the described Wrapper type
     * @param keyAccessorType The key accessor describing the KeyEncryptionMethod and {@link CryptographicType}
     * @return List of to-be-expected property specs
     */
    List<PropertySpec> getPropertySpecs(KeyAccessorType keyAccessorType);

    /**
     * List the PropertySpecs that are required to import a wrapper of the mentioned type.
     * E.g. For a datavault keypair, we will need the keys themselves, on a HSM private key, we will need only the KEK label.
     * @param keyAccessorType The key accessor describing the KeyEncryptionMethod and {@link CryptographicType}
     * @return List of required property specs for importing an SecurityValueWrapper if this type.
     */
//    List<PropertySpec> getImportPropertySpecs(KeyAccessorType keyAccessorType);

    /**
     * List the PropertySpecs that are required to generate a wrapper of the mentioned type.
     * E.g. For a datavault keypair, we will need no additional properties, for a HSM private key stored in the DB, we will need the label of the DB wrapper key.
     * @param keyAccessorType The key accessor describing the KeyEncryptionMethod and {@link CryptographicType}
     * @return List of required property specs for importing an SecurityValueWrapper if this type.
     */
//    List<PropertySpec> getGenerationPropertySpecs(KeyAccessorType keyAccessorType);

    /**
     * Get an existing KeyType by name.
     * Returns Optional.empty() if not found
     * @param name The KpiType's name. The name is a unique identifier.
     * @return The KpiType if present, empty otherwise.
     */
    Optional<KeyType> getKeyType(String name);

    /**
     * Get an existing KeyType by id.
     * Returns Optional.empty() if not found
     * @param id The KpiType's id.
     * @return The KpiType if present, empty otherwise.
     */
    Optional<KeyType> getKeyType(long id);

    /**
     * Get a list of all known key types
     */
    List<KeyType> getKeyTypes();

    /**
     * Creates a new PrivateKeyWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param keyType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @param keyEncryptionMethod Desired method of key storage
     * @return a new private key wrapper of the required type and encryption method, without value.
     */
    PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType, String keyEncryptionMethod);

    /**
     * Creates a new SymmetricKeyWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param keyAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new symmetric key wrapper of the required type and encryption method, without value.
     */
    SymmetricKeyWrapper newSymmetricKeyWrapper(KeyAccessorType keyAccessorType);

    /**
     * Returns a DeviceSecretImporter, capable of importing a secret (Passphrase, Key or whatever) for the appropriate {@link KeyAccessorType}
     * @param keyAccessorType
     * @return
     */
    DeviceSecretImporter getDeviceSecretImporter(KeyAccessorType keyAccessorType);

    /**
     * Creates a new PassphraseWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param keyAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new passphrase wrapper of the required type and encryption method, without value.
     */
    PassphraseWrapper newPassphraseWrapper(KeyAccessorType keyAccessorType);

    CertificateWrapper newCertificateWrapper(String alias);

    /**
     * Creates a new Client certificate wrapper.
     *
     * @param clientCertificateKeyType The Key AccessorType describing the certificate and the private key
     * @param keyEncryptionMethod Desired method of key storage
     * @return Persisted, empty ClientCertificateWrapper
     */
    ClientCertificateWrapperBuilder newClientCertificateWrapper(KeyType clientCertificateKeyType, String keyEncryptionMethod);

    /**
     * Create a new Keypair wrapper. A keypair wrapper contains a public and/or private key, without certificate.
     * @param alias The keypair will be known by an alias. The alias will need to be unique in the keypair store.
     * @param keyType The KeyType describes the key (RSA, DSA, EC) and the specific parameters.
     * @param keyEncryptionMethod The KeyEncryptionMethod describes which wrapper will be used to store the private key
     * (DataVault being the Connexo default)
     * @return a newly created KeypairWrapper, containing an empty
     */
    KeypairWrapper newKeypairWrapper(String alias, KeyType keyType, String keyEncryptionMethod);

    KeypairWrapper newPublicKeyWrapper(String alias, KeyType keyType);

    /**
     * Returns the client certificate known by the provided alias
     * @param alias
     * @return The {@link ClientCertificateWrapper} known by the alias, empty if not found
     */
    Optional<ClientCertificateWrapper> findClientCertificateWrapper(String alias);

    /**
     * Returns the client certificate known by the id
     * @param id
     * @return The {@link ClientCertificateWrapper}, empty if not found
     */
    Optional<ClientCertificateWrapper> findClientCertificateWrapper(long id);

    /**
     * Returns the CertificateWrapper known by the provided alias, no tust store is searched by this method
     * @param alias The certificate's alias
     * @return The {@link CertificateWrapper} known by the alias, empty if not found.
     */
    Optional<CertificateWrapper> findCertificateWrapper(String alias);

    /**
     * Returns the CertificateWrapper identified by the provided id
     * @param id The certificate's id
     * @return The {@link CertificateWrapper}, empty if not found.
     */
    Optional<CertificateWrapper> findCertificateWrapper(long id);

    /**
     * Returns the KeypairWrapper identified by the provided id
     * @param id The Keypair's id
     * @return The {@link KeypairWrapper}, empty if not found.
     */
    Optional<KeypairWrapper> findKeypairWrapper(long id);

    /**
     * Returns the KeypairWrapper identified by alias
     * @param alias The Keypair's alias
     * @return The {@link KeypairWrapper}, empty if not found.
     */
    Optional<KeypairWrapper> findKeypairWrapper(String alias);

    /**
     * Returns the CertificateWrapper identified by the provided id if the {@link CertificateWrapper} has the correct version
     * @param id The certificate's id
     * @param version The object's required version
     * @return The {@link CertificateWrapper}, empty if not found.
     */
    Optional<CertificateWrapper> findAndLockCertificateWrapper(long id, long version);

    /**
     * Returns all non-trusted certificates, that means, all certificates for the 'certificate store'
     * @return All Certificates and ClientCertificates, TrustedCertificates will not be part of the list.
     */
    Finder<CertificateWrapper> findAllCertificates();


    /**
     * Used by the secure shipment importer, this method allows the importer to determine which algorithm is to be used
     * for which encryption method.
     * Connexo has only a single algorithm by default:
     *    http://www.w3.org/2001/04/xmlenc#aes256-cbc (identifier) -> algorithm AES/CBC/PKCS5PADDING with key length 32.
     * Custom mappings can be added using the method registerSymmetricAlgorithm()
     * @param identifier The identifier as found in the XMLSEC file.
     * @return The {@link SymmetricAlgorithm} associated with this identifier, or Optional.empty() if there is none.
     */
    Optional<SymmetricAlgorithm> getSymmetricAlgorithm(String identifier);

    /**
     * Shipment importer used the SymmetricAlgorithm to determine which java algortihm is to be used for which XML algorithm identifier.
     * This method can be used to add additional algorithms. The identifier of the SymmetricAlgorithm is used as key,
     * therefore, existing mappings can be overridden as well.
     * @param symmetricAlgorithm The SymmetricAlgorithm to register. The identifier will be used as id.
     */
    void registerSymmetricAlgorithm(SymmetricAlgorithm symmetricAlgorithm);

    /**
     * Returns a finder for all Keypairs in the Keypair-store.
     * @return
     */
    Finder<KeypairWrapper> findAllKeypairs();

    /**
     * Returns the KeypairWrapper identified by the provided id if the {@link KeypairWrapper} has the correct version
     * @param id The Keypair's id
     * @param version The object's required version
     * @return The {@link KeypairWrapper}, empty if not found.
     */
    Optional<KeypairWrapper> findAndLockKeypairWrapper(long id, long version);

    /**
     * List all known aliases from the certificate store that match the search filter.
     * @see {https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=JDG&title=Filter}
     * @see {https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=JDG&title=Forms+and+form+elements}
     * @param searchFilter Search filter for alias and truststore, possibly containing wildcards for alias
     * @return Finder for matching aliases. If more results are available
     * than requested, limit+1 results will be returned.
     */
    Finder<CertificateWrapper> getAliasesByFilter(AliasSearchFilter searchFilter);

    class AliasSearchFilter {
        public String alias;
        public TrustStore trustStore;
    }

    public interface PasswordTypeBuilder {
        PasswordTypeBuilder description(String description);
        PasswordTypeBuilder length(int length);
        PasswordTypeBuilder withLowerCaseCharacters();
        PasswordTypeBuilder withUpperCaseCharacters();
        PasswordTypeBuilder withNumbers();
        PasswordTypeBuilder withSpecialCharacters();
        KeyType add();
    }

    public interface ClientCertificateWrapperBuilder {
        ClientCertificateWrapperBuilder alias(String alias);
        ClientCertificateWrapper add();
    }

    public interface KeypairWrapperBuilder {
        KeypairWrapperBuilder alias(String alias);
        KeypairWrapper add();
    }

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
