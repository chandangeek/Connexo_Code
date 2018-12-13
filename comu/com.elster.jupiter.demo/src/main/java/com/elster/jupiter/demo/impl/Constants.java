/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

//TODO delete
public final class Constants {
    private Constants() {}

    public static final class Device{
        public static final String STANDARD_PREFIX = "SPE";
        public static final String GAS_PREFIX = "SPG";
        public static final String WATER_PREFIX = "SPW";
        public static final String GAS_WATER_SERIAL_PREFIX = "SIM";
        public static final String GAS_SERIAL_SUFFIX = "06301";
        public static final String WATER_SERIAL_SUFFIX = "06302";

        public static final String MOCKED_VALIDATION_DEVICE = "VPB";

        public static final String MOCKED_REALISTIC_DEVICE = "DME";

        public static final String A3_DEVICE = "DTech-A3-";
        public static final String A3_SERIAL_NUMBER = "18358577";

        public static final String BEACON_PREFIX = "SPB";
        public static final String BEACON_SLAVE_PREFIX = "SPBS";
        private Device() {}
    }
}
