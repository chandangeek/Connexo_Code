/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

interface PropertySetter {

    /**
     * @param key - A string containing a lexical representation of xsd:hexBinary
     */
    void setHexBinaryKey(String key);

    /**
     *
     * @return - A string containing a lexical representation of xsd:hexBinary
     */
    String getHexBinaryKey();

    /**
     *
     * @return
     */
    byte[] getKey();

}
