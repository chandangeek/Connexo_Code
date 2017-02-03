/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption.asymetric;

/**
 * The different algorithms supported in DLMS security suite 1 and 2.
 *
 * @author alex
 */
public enum ECCCurve {

    /**
     * P-256 curve.
     */
    P256_SHA256("secp256r1", "SHA256withECDSA", 32),

    /**
     * P-384 curve.
     */
    P384_SHA384("secp384r1", "SHA384withECDSA", 48);

    /**
     * Size of the components generated.
     */
    private final int componentSize;

    /**
     * Name of the algo in the JSSE provider.
     */
    private final String signAlgo;

    /**
     * The name of the curve.
     */
    private final String curveName;

    /**
     * Create a new instance.
     *
     * @param signAlgo      Internal name of the signature algo.
     * @param componentSize Size of the components (r and s).
     * @param curveName     The name of the curve.
     */
    ECCCurve(final String curveName, final String signAlgo, final int componentSize) {
        this.curveName = curveName;
        this.componentSize = componentSize;
        this.signAlgo = signAlgo;
    }

    /**
     * @return the componentSize
     */
    public final int getSignatureComponentSize() {
        return this.componentSize;
    }

    /**
     * @return the internalName
     */
    public final String getSignatureAlgoName() {
        return this.signAlgo;
    }

    /**
     * Returns the name of the curve.
     *
     * @return The name of the curve.
     */
    public final String getCurveName() {
        return this.curveName;
    }
}