package com.elster.jupiter.pki;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.security.PublicKey;
import java.util.Optional;

/**
 * This object represents a Keypair (public and/or private key) stored in Connexo. Configuration of the private key can
 * be done by setting a PrivateKeyWrapper or handing properties to the PrivateKeyWrapper.
 */
@ProviderType
public interface KeyPairWrapper extends HasDynamicPropertiesWithUpdatableValues, HasId, SecurityValueWrapper {
    /**
     * A certificate alias is the name given to a certificate located in the certificate store.
     * Each entry in the certificate store has an alias to help identify it.
     * @return This certificate's alias
     */
    String getAlias();

    /**
     * Set the alias for this certificate.
     * A certificate alias is the name given to a certificate located in the certificate store.
     * A Certificate alias is unique in scope of a trust store, if the certificate is a trusted certificate.
     * For non-trusted certificates, the alias is unique system-wide (among other non-trusted certificates)
     * @param alias
     */
    void setAlias(String alias);

    /**
     * If a PublicKey is available in this wrapper, it will be returned, if not, Optional.empty() will be returned.
     * @return PublicKey, if present.
     */
    Optional<PublicKey> getPublicKey();

    /**
     * Sets a value for the public key. Any existing value will be overridden.
     */
    void setPublicKey(PublicKey publicKey);

    /**
     * If a PrivateKey wrapper is available in this keypair, it will be returned, if not, Optional.empty() will be returned.
     * @return PrivateKeyWrapper, if present.
     */
    Optional<PrivateKeyWrapper> getPrivateKeyWrapper();

    /**
     * Sets a value for the private key wrapper. Any existing value will be overridden.
     */
    void setPrivateKeyWrapper(PrivateKeyWrapper privateKeyWrapper);

    /**
     * The current version of this business object. Version property is used for concurrency purposes.
     */
    long getVersion();

    /**
     * Deletes this wrapper and the contained certificate and private key, if applicable.
     */
    void delete();

    /**
     * Persist changes to this object
     */
    void save();

}
