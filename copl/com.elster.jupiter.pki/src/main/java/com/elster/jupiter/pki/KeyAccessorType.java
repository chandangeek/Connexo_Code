package com.elster.jupiter.pki;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

/**
 * KeyAccessor links a KeyType to a DeviceType. By configuring KeyTypes on a DeviceType, the user identifies which
 * keys/certificates are required or make sense for devices of this type, the user assigns a function to the key/certificate.
 * Key requirement is based on which keys the underlying protocol actually needs.
 * <p>
 * The KeyAccessorType also has a name. This name will identify a key for a certain purpose. E.g. the Shipment file will
 * map keys to KeyAccessorType by name, that is, the keys from the shipment file will be labelled, the label matches the
 * name of a KeyAccessor.
 */
@ConsumerType
public interface KeyAccessorType extends HasId, HasName  {

    /**
     * The KeyAccessorType system assigned id
     */
    long getId();

    /**
     * This name will identify a key for a certain purpose. E.g. the Shipment file will map keys to KeyAccessorType by
     * name, that is, the keys from the shipment file will be labelled, the label matches the name of a KeyAccessor.
     */
    String getName();

    /**
     * User can add a description clarifying the purpose of this key on a device.
     */
    String getDescription();

    /**
     * Duration is a default validity period of a key. This makes sense mostly for symmetric keys who, unlike certificates,
     * do not contain a validity period by themselves. The validity will determine the frequency for key renewal.
     */
    Optional<TimeDuration> getDuration();


    /**
     * Keytype identifies main parameters for the stored value (certificate or key), such as curve, algorithm,
     * bitlength, ...
     *
     * @return The KeyType associated with this KeyAccessor
     */
    KeyType getKeyType();

    /**
     * KeyEncryptionMethod describes how the key will be stored. KeyEncryptionMethods are registered through whiteboard
     * on the KeyService
     *
     * @return The KeyEncryptionMethod as a name
     */
    String getKeyEncryptionMethod();

    long getVersion();

    /**
     * If a KeyAcccessorType with a KeyType of CryptographicType Certificate (sor subset) is being created, a
     * TrustStore needs to be linked to the KeyAccessorType. This allows the users of the certificate contained in
     * the value of this KeyAccessorType to be validated against a specific TrustStore.
     * @return TrustStore in case the KeyType of this accessorType represents a certificate
     */
    Optional<TrustStore> getTrustStore();

    Updater startUpdate();

    interface Builder {
        /**
         * Provide a user understandable description of this key's function
         */
        Builder description(String description);

        /**
         * KeyEncryptionMethod describes how the Key will be encrypted/stored.
         * Only applies to keys (both symmetric and asymmetric)
         * Valid KeyEncryptionMethods are provided by wrapper factories whom register on the PkiService whiteboard
         * @param keyEncryptionMethod @see PkiService::getKeyEncryptionMethods()
         */
        Builder keyEncryptionMethod(String keyEncryptionMethod);

        /**
         * If a KeyAcccessorType with a KeyType of CryptographicType Certificate (sor subset) is being created, a
         * TrustStore needs to be linked to the KeyAccessorType. This allows the users of the certificate contained in
         * the value of this KeyAccessorType to be validated against a specific TrustStore.
         * @param trustStore The trust by which a chain of trust for certificates for this KeyAccessor will be validated
         */
        Builder trustStore(TrustStore trustStore);

        /**
         * Symmetric keys require a durarion to be provided.
         */
        Builder duration(TimeDuration duration);

        KeyAccessorType add();
    }

    interface Updater {
        Updater name(String name);

        Updater description(String description);

        Updater duration(TimeDuration duration);

        KeyAccessorType complete();
    }
}
