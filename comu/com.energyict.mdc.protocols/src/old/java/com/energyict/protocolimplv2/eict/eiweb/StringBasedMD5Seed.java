/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.api.crypto.MD5Seed;

/**
 * Provides an implementation for the {@link MD5Seed} interface
 * that is backed by a plain and simple String.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:44)
 */
public class StringBasedMD5Seed implements MD5Seed {

    private String value;

    public StringBasedMD5Seed (String value) {
        super();
        this.value = value;
    }

    @Override
    public byte[] getBytes () {
        return this.value.getBytes();
    }

}