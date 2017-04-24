/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.api.crypto.MD5Seed;

/**
 * Implements the null value for the {@link MD5Seed} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-06-27 (09:02)
 */
public class NullMD5Seed implements MD5Seed {

    @Override
    public byte[] getBytes () {
        return new byte[0];
    }

}