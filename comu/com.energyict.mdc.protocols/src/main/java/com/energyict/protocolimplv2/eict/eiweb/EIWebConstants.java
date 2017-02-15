/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.eiweb;

/**
 * Contains constants that apply only to the EIWeb context.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (13:11)
 */
public final class EIWebConstants {

    /**
     * The number of seconds that on average span 10 years.
     */
    public static final long SECONDS10YEARS = 315532800;

    /**
     * The String that indicates that URL data post content
     * is of type binary.
     */
    public static final String BINARY_CONTENT_TYPE_INDICATOR = "application/octet-stream";

    /**
     * The String that indicates that URL data post content
     * is of plain text.
     */
    public static final String PLAINTEXT_CONTENT_TYPE_INDICATOR = "application/x-www-form-urlencoded";

    /**
     * The name of the URL parameter that contains the device id.
     */
    public static final String DEVICE_ID_URL_PARAMETER_NAME = "id";

    /**
     * The name of the URL parameter that contains the device id.
     */
    public static final String UTC_URL_PARAMETER_NAME = "utc";

    /**
     * The name of the URL parameter that contains the code.
     */
    public static final String CODE_URL_PARAMETER_NAME = "code";

    /**
     * The name of the URL parameter that contains the state bits.
     */
    public static final String STATE_BITS_URL_PARAMETER_NAME = "statebits";

    /**
     * The name of the URL parameter that contains the mask.
     */
    public static final String MASK_URL_PARAMETER_NAME = "mask";

    /**
     * The name of the URL parameter that contains the meter data.
     */
    public static final String METER_DATA_PARAMETER_NAME = "value";

    /**
     * The name of the URL parameter that contains the number of confirmed messages.
     */
    public static final String MESSAGE_COUNTER_URL_PARAMETER_NAME = "xmlctr";

    // Hide utility class constructor
    private EIWebConstants () {}

}