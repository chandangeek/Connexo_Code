package com.energyict.mdc.device.data.importers.impl;

import java.util.ArrayList;
import java.util.List;

public class FileImportRecord {

    private long lineNumber;
    private String deviceMRID;
    private List<String> location = new ArrayList<>();

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

    public List<String> getLocation() {
        return location;
    }

    public void addLocation(String location) {
        this.location.add(location);
    }
}
