/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import java.util.ArrayList;
import java.util.List;

public class FileImportRecord {

    private long lineNumber;
    private String deviceIdentifier;
    private List<String> location = new ArrayList<>();
    private List<String> geoCoordinates = new ArrayList<>();

    public FileImportRecord() {
    }

    public FileImportRecord(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public List<String> getLocation() {
        return location;
    }

    public void addLocation(String location) {
        this.location.add(location);
    }

    public List<String> getGeoCoordinates() {
        return geoCoordinates;
    }

    public void setGeoCoordinates(String geoCoordinates){
        this.geoCoordinates.add(geoCoordinates);
    }
}
