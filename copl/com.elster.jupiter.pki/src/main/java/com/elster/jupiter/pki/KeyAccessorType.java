package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

import java.time.Duration;
import java.util.Optional;

/**
 * KeyAccessor links a KeyType to a DeviceType. By configuring KeyTypes on a DeviceType, the user identifies which
 * keys/certificates are required or make sense for devices of this type, the user assigns a function to the key/certificate.
 * Key requirement is based on which keys the underlying protocol actually needs.
 *
 * The KeyAccessorType also has a name. This name will identify a key for a certain purpose. E.g. the Shipment file will
 * map keys to KeyAccessorType by name, that is, the keys from the shipment file will be labelled, the label matches the
 * name of a KeyAccessor.
 */
@ConsumerType
public interface KeyAccessorType {
    /**
     * This name will identify a key for a certain purpose. E.g. the Shipment file will map keys to KeyAccessorType by
     * name, that is, the keys from the shipment file will be labelled, the label matches the name of a KeyAccessor.
     */
    void setName(String name);
    String getName();

    /**
     * User can add a description clarifying the purpose of this key on a device.
     */
    void setDescription(String description);
    String getDescription();

    /**
     * Duration is a default validity period of a key. This makes sense mostly for symmetric keys who, unlike certificates,
     * do not contain a validity period by themselves. The validity will determine the frequency for key renewal.
     * @param duration The period for which a (symmetric key) crypto entity will be valid, after which renewal is required.
     */
    void setDuration(Duration duration);
    Optional<Duration> getDuration();


    /**
     * Keytype identifies main parameters for the stored value (certificate or key), such as curve, algorithm,
     * bitlength, ...
     * @return The KeyType associated with this KeyAccessor
     */
    KeyType getKeyType();

    interface Builder {
        Builder description(String description);
        Builder duration(Duration duration);
        KeyAccessorType add();
    }
}
