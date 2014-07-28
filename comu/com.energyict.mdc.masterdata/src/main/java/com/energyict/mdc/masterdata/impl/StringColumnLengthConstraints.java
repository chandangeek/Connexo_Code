package com.energyict.mdc.masterdata.impl;

/**
 * Defines constants for the maximum length of String properties
 * of entities of the master data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-04 (11:13)
 */
public final class StringColumnLengthConstraints {

    public static final int DEFAULT_OBISCODE_LENGTH = 25;

    public static final int PHENOMENON_UNIT = 7;
    public static final int PHENOMENON_MEASUREMENT_CODE = 80;

    // Hide constructor for class that only contains constants
    private StringColumnLengthConstraints() {super();}

}