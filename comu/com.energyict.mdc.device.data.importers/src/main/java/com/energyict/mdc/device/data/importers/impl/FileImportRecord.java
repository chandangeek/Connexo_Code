package com.energyict.mdc.device.data.importers.impl;

public class FileImportRecord {

    private long lineNumber;
    private String deviceMRID;

    public FileImportRecord() {
    }

    public FileImportRecord(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setDeviceMRID(String deviceMRID) {
        this.deviceMRID = deviceMRID;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getDeviceMRID() {
        return deviceMRID;
    }
}
