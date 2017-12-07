package com.elster.jupiter.pki;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Comparison;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * This is the main interface towards the security bundle. It provides access for the main PKI & HSM procedures.
 */
@ProviderType
public interface SecurityManagementService {


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
     * Creates a KeyType for a password. A password has a few parameters: length and possible characters in it.
     * @param name The type's name
     * @return a password key type builder, allowing you to set properties as length and character sets
     */
    PasswordTypeBuilder newPassphraseType(String name);


    /**
     * List the PropertySpecs that can be expected for the described Wrapper type
     * @param securityAccessorType The security accessor describing the KeyEncryptionMethod and {@link CryptographicType}
     * @return List of to-be-expected property specs
     */
    List<PropertySpec> getPropertySpecs(SecurityAccessorType securityAccessorType);

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
     * @param securityAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new symmetric key wrapper of the required type and encryption method, without value.
     */
    SymmetricKeyWrapper newSymmetricKeyWrapper(SecurityAccessorType securityAccessorType);

    /**
     * Creates a new PassphraseWrapper. The PkiService will delegate the actual creation and storage to the appropriate
     * factory given the provided key encryption method.
     * @param securityAccessorType Contains all information required by the pkiService and factories to figure out what has
     * to be done.
     * @return a new passphrase wrapper of the required type and encryption method, without value.
     */
    PassphraseWrapper newPassphraseWrapper(SecurityAccessorType securityAccessorType);

    CertificateWrapper newCertificateWrapper(String alias);

    List<SecurityValueWrapper> getExpired(Expiration expiration, Instant when);

    Optional<Comparison> getExpirationCondition(Expiration expiration, Instant when, String securityValueWrapperTableName);
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

    Finder<CertificateWrapper> getAliasesByFilter(AliasParameterFilter aliasParameterFilter);

    /**
     * List all known subjects from the certificate store that match the search filter.
     * @param searchFilter Search filter for subject, possibly containing wildcards for subject
     * @return Finder for matching subjects. If more results are available
     * than requested, limit+1 results will be returned.
     */
    Finder<CertificateWrapper> getSubjectsByFilter(SubjectParameterFilter searchFilter);

    /**
     * List all known issuers from the certificate store that match the search filter.
     * @param searchFilter Search filter for issuer, possibly containing wildcards for issuer
     * @return Finder for matching issuers. If more results are available
     * than requested, limit+1 results will be returned.
     */
    Finder<CertificateWrapper> getIssuersByFilter(IssuerParameterFilter searchFilter);

    /**
     * List all known keyUsages from the certificate store that match the search filter.
     * @param searchFilter Search filter for extendedKeyUsages, possibly containing wildcards for keyUsages
     * @return Finder for matching keyUsages. If more results are available
     * than requested, limit+1 results will be returned.
     */
    Finder<CertificateWrapper> getKeyUsagesByFilter(KeyUsagesParameterFilter searchFilter);

    /**
     * List all known certificates from the certificate store that match the search filter.
     *
     * @param dataSearchFilter Search filter for alias, subject, issuer and expiration dates,
     *                         possibly containing wildcards for alias, subject and issuer
     * @return Finder for matching certificates. If more results are available
     * than requested, limit+1 results will be returned.
     */
    Finder<CertificateWrapper> findCertificatesByFilter(DataSearchFilter dataSearchFilter);

    /**
     * List all known trusted certificates from the trust store that match the search filter.
     * @param dataSearchFilter Search filter for alias, subject, issuer and expiration dates,
     *                         possibly containing wildcards for alias, subject and issuer
     * @return Finder for matching certificates. If more results are available
     * than requested, limit+1 results will be returned.
     */
    List<CertificateWrapper> findTrustedCertificatesByFilter(DataSearchFilter dataSearchFilter);

    class DataSearchFilter {
        public Optional<TrustStore> trustStore;
        public Optional<List<String>> alias;
        public Optional<List<String>> subject;
        public Optional<List<String>> issuer;
        public Optional<List<String>> keyUsages;
        public Optional<Instant> intervalFrom;
        public Optional<Instant> intervalTo;
    }

    QueryService getQueryService() ;

    Query<CertificateWrapper> getCertificateWrapperQuery();

    Condition getSearchCondition(DataSearchFilter dataSearchFilter);

     interface PasswordTypeBuilder {

        PasswordTypeBuilder description(String description);
        PasswordTypeBuilder length(int length);
        PasswordTypeBuilder withLowerCaseCharacters();
        PasswordTypeBuilder withUpperCaseCharacters();
        PasswordTypeBuilder withNumbers();
        PasswordTypeBuilder withSpecialCharacters();
        KeyType add();
    }

    interface ClientCertificateWrapperBuilder {
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

    interface ClientCertificateTypeBuilder {
        ClientCertificateTypeBuilder description(String description);
        ClientCertificateTypeBuilder setKeyUsages(EnumSet<KeyUsage> keyUsages);
        ClientCertificateTypeBuilder setExtendedKeyUsages(EnumSet<ExtendedKeyUsage> keyUsages);
        AsyncKeySizeBuilder RSA();
        AsyncKeySizeBuilder DSA();
        AsyncCurveBuilder ECDSA();
    }

    interface AsyncKeySizeBuilder {
        AsyncKeySizeBuilder keySize(int keySize);
        KeyType add();
    }

    interface AsyncCurveBuilder {
        AsyncCurveBuilder curve(String curveName);
        KeyType add();
    }

    interface TrustStoreBuilder {
        /**
         * Clarify the purpose of this TrustStore by adding a human understandable description.
         * This field is optional.
         */
        TrustStoreBuilder description(String description);

        TrustStore add();
    }

    interface KeyTypeBuilder {
        KeyTypeBuilder description(String description);
        KeyType add();
    }

}
