package com.energyict.mdc.sap.soap.webservices;

public enum SapAttributeNames {
    SAP_UTILITIES_DEVICE_ID("SapUtilitiesDeviceID"),
    SAP_UTILITIES_MEASUREMENT_TASK_ID("SapUtilitiesMeasurementTaskID"),
    SAP_UTILITIES_TIME_SERIES_ID("SapUtilitiesTimeSeriesID"),
    SAP_METER_READING_DOCUMENT_ID("SapMeterReadingDocumentID");


    private final String attributeName;

    SapAttributeNames(String nameType) {
        this.attributeName = nameType;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
