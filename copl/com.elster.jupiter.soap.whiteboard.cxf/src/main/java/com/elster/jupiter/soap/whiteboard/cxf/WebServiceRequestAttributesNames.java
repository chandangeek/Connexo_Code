package com.elster.jupiter.soap.whiteboard.cxf;

public enum WebServiceRequestAttributesNames {


    CIM_DEVICE_NAME("CimDeviceName"),
    CIM_DEVICE_MR_ID("CimDeviceMrID"),
    CIM_DEVICE_SERIAL_NUMBER("CimDeviceSerialNumber"),
    CIM_USAGE_POINT_NAME("CimUsagePointName"),
    CIM_USAGE_POINT_MR_ID("CimUsagePointMrID"),

    SAP_UTILITIES_DEVICE_ID("SapUtilitiesDeviceID"),
    SAP_UTILITIES_MEASUREMENT_TASK_ID("SapUtilitiesDeviceID"),
    SAP_SERIAL_ID("SapSerialID"),
    SAP_UTILITIES_TIME_SERIES_ID("UtilitiesTimeSeriesID");

    private final String attributeName;

    WebServiceRequestAttributesNames(String nameType) {
        this.attributeName = nameType;
    }

    public String getAttributeName() {
        return attributeName;
    }
}