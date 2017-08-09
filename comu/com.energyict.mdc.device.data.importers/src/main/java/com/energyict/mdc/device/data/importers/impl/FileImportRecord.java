/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import java.util.ArrayList;
import java.util.List;

public class FileImportRecord extends com.elster.jupiter.fileimport.csvimport.FileImportRecord {
    private String deviceIdentifier;
    private List<String> location = new ArrayList<>();
    private List<String> geoCoordinates = new ArrayList<>();

    public FileImportRecord() {
        super();
    }

    public FileImportRecord(long lineNumber) {
        super(lineNumber);
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
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
