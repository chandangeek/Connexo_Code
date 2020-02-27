/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;


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
     * Sets smart key value
     * @param key key value
     */
    void setSmartMeterKey(byte[] key);

    /**
     *
     * @return smart meter key used in renew process, if exists. otherwise null is returned (if this key does not exist)
     */
    byte[] getSmartMeterKey();

    /**
     *
     * @return HSM label
     */
    String getLabel();

    /**
     * HSM requires previous key values, in order to generate a new key
     * @param masterKey previous key
     */
    void generateValue(SecurityAccessorType securityAccessorType, HsmKey masterKey);
}
