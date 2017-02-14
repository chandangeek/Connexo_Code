package com.elster.jupiter.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

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
public interface KeyAccessorType extends HasId, HasName {

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
     * @return The KeyType associated with this KeyAccessor
     */
    KeyType getKeyType();

    /**
     * KeyEncryptionMethod describes how the key will be stored. KeyEncryptionMethods are registered through whiteboard
     * on the KeyService
     * @return The KeyEncryptionMethod as a name
     */
    String getKeyEncryptionMethod();

    Updater startUpdate();

    interface Builder {
        Builder description(String description);
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
