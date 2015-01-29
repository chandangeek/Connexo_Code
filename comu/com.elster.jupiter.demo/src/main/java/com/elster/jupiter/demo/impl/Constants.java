package com.elster.jupiter.demo.impl;

//TODO delete
public final class Constants {
    private Constants() {}
    public static final class DeviceGroup{
        public static final String SOUTH_REGION = "South region";
        public static final String NORTH_REGION = "North region";
        public static final String ALL_ELECTRICITY_DEVICES = "All electricity devices";

        private DeviceGroup() {}
    }

    public static final class DataExportTask{
        public static final String DEFAULT_PREFIX = "Consumption data exporter - ";

        private DataExportTask() {}
    }


    public static final class Device{
        public static final String STANDARD_PREFIX = "SPE";

        public static final String MOCKED_VALIDATION_DEVICE = "VPB";
        public static final String MOCKED_VALIDATION_SERIAL_NUMBER = "085600010352";

        public static final String MOCKED_REALISTIC_DEVICE = "DME";
        public static final String MOCKED_REALISTIC_SERIAL_NUMBER = "093000020359";

        public static final String A3_DEVICE = "DTech-A3-";
        public static final String A3_SERIAL_NUMBER = "18358577";

        private Device() {}
    }
}
