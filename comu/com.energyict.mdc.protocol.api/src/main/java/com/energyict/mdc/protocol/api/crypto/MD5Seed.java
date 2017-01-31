/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.crypto;

/**
 * Represents a seed for the MD5 cryptographic algorithm.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:32)
 */
public interface MD5Seed {

    /**
     * Gets the raw bytes that constitute this seed.
     *
     * @return The raw bytes
     */
    public byte[] getBytes ();

}