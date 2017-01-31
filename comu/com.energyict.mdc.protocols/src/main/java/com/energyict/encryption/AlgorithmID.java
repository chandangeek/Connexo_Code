/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption;

/**
 * The algorithm IDs.
 *
 * @author alex
 */
public enum AlgorithmID {

    /**
     * AES-GCM-128 (suite 0 and 1)
     */
    AES_GCM_128(0),

    /**
     * AES-GCM-256 (suite 2).
     */
    AES_GCM_256(1),

    /**
     * AES-Wrap 128 (suite 0 and 1).
     */
    AES_WRAP_128(2),

    /**
     * AES-Wrap 256 (suite 2).
     */
    AES_WRAP_256(3);

    // FIXME : This should be 2.16.756.5.8.3.x, but the encoded value in the green book has 06 instead of 05, so we'll use that for the time being.
    /**
     * The OID pattern.
     */
    private static final String OID_PATTERN = "2.16.756.6.8.3.%d";

    /**
     * The algo ID.
     */
    private final int id;

    /**
     * The OID.
     */
    private final OID oid;

    /**
     * Cache the encoded value, we'll need it often.
     */
    private final byte[] encoded;

    /**
     * The algorithm ID.
     *
     * @param id ID of the algo.
     */
    AlgorithmID(final int id) {
        this.id = id;
        this.oid = OID.fromString(String.format(OID_PATTERN, id));
        this.encoded = this.oid.encodeASN1();
    }

    /**
     * Returns the ID.
     *
     * @return The algo ID.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Returns the encoded version.
     *
     * @return The encoded version.
     */
    public final byte[] getEncoded() {
        return this.encoded;
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        final StringBuilder builder = new StringBuilder("Algorithm ").append(this.name()).append(" : ");

        builder.append("OID [").append(this.oid.toString()).append("]");

        return builder.toString();
    }
}
