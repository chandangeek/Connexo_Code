/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;


import java.util.Optional;

public interface HsmKey extends SymmetricKeyWrapper{

    /**
     * Base64 encodes the key and stores it together with the label in the database
     * @param key HSM encrypted key
     * @param label HSM label to decrypt key
     */
    void setKey(byte[] key, String label);

    /**
     * Decodes the Base64 encoded key from the database if key is not reversible (resulting in the key encrypted by HSM). if key is reversible then we will return decrypted value
     * @return HSM encrypted key
     */
    byte[] getKey();

    /**
     *
     * @return smart meter key used in renew process, if exists. otherwise null is returned (if this key does not exist)
     */
    byte[] getSmartMeterKey();

    /**
     *
     * @return true in case of the key is a service key, otherwise false
     */
    boolean isServiceKey();

    /**
     *
     * @return wrapped value for service key used in SKP/SKI process, if exists. otherwise null is returned (if this key does not exist)
     */
    byte[] getWrappedKey();

    /**
     *
     * @return HSM label
     */
    String getLabel();

    /**
     *
     * @param value to indicate service key
     */
    void setServiceKey(boolean value);

    /**
     *
     * @param value Base64 encoded key value
     */
    void setWrappedKey(String value);

    /**
     *
     * @param value Base64 encoded key value
     */
    void setSmartMeterKey(String value);

    /**
     * HSM requires previous key values, in order to generate a new key
     * @param masterKey the optional wrap key (the reversible keys don't need a wrapper)
     */
    void generateValue(SecurityAccessorType securityAccessorType, Optional<HsmKey> masterKey);
}