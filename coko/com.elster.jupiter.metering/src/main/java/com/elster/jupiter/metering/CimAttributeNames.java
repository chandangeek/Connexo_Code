package com.elster.jupiter.metering;

public enum CimAttributeNames {
    SERIAL_ID("SerialID"),
    CIM_DEVICE_NAME("CimDeviceName"),
    CIM_DEVICE_MR_ID("CimDeviceMrID"),
    CIM_DEVICE_SERIAL_NUMBER("CimDeviceSerialNumber");

    private final String attributeName;

    CimAttributeNames(String nameType) {
        this.attributeName = nameType;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
