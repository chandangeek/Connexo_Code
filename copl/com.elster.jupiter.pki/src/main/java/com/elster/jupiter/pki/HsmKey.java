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
     * Decodes the Base64 encoded key from the database
     * @return HSM encrypted key
     */
    byte[] getKey();

    /**
     *
     * @return HSM label
     */
    String getLabel();

    /**
     * HSM requires previous key values, in order to generate a new key
     * @param oldKey previous key
     */
    void generateValue(HsmKey oldKey);
}
