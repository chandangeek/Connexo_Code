package com.elster.jupiter.pki;

import com.elster.jupiter.pki.impl.Renewable;

import javax.crypto.SecretKey;

/**
 * This class wraps an actual Key with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
public interface SymmetricKeyWrapper extends HasDynamicPropertiesWithUpdatableValues, Renewable {

    String getKeyEncryptionMethod();

    SecretKey getKey();

}
