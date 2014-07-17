package com.energyict.mdc.masterdata.impl;

/**
 * Defines constants for the maximum length of String properties
 * of entities of the master data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-04 (11:13)
 */
public final class StringColumnLengthConstraints {

    public static final int DEFAULT_NAME_LENGTH = 80;
    public static final int DEFAULT_DESCRIPTION_LENGTH = 255;
    public static final int DEFAULT_OBISCODE_LENGTH = 25;

    public static final int LOAD_PROFILE_TYPE_NAME = DEFAULT_NAME_LENGTH;
    public static final int LOAD_PROFILE_TYPE_DESCRIPTION = DEFAULT_DESCRIPTION_LENGTH;
    public static final int LOAD_PROFILE_TYPE_OBIS_CODE = DEFAULT_OBISCODE_LENGTH;

    public static final int LOG_BOOK_TYPE_NAME = DEFAULT_NAME_LENGTH;
    public static final int LOG_BOOK_TYPE_DESCRIPTION = DEFAULT_DESCRIPTION_LENGTH;
    public static final int LOG_BOOK_TYPE_OBIS_CODE = DEFAULT_OBISCODE_LENGTH;

    public static final int REGISTER_GROUP_NAME = 256;
    public static final int MEASUREMENT_TYPE_NAME = 126;
    public static final int MEASUREMENT_TYPE_OBIS_CODE = DEFAULT_OBISCODE_LENGTH;
    public static final int MEASUREMENT_TYPE_READING_TYPE = 100;
    public static final int MEASUREMENT_TYPE_DESCRIPTION = DEFAULT_DESCRIPTION_LENGTH;

    public static final int PHENOMENON_NAME = DEFAULT_NAME_LENGTH;
    public static final int PHENOMENON_UNIT = 7;
    public static final int PHENOMENON_MEASUREMENT_CODE = 80;

    // Hide constructor for class that only contains constants
    private StringColumnLengthConstraints() {super();}

}