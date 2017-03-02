/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterBuilder;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Provider;
import java.time.Instant;
import java.util.UUID;

class MeterBuilderImpl implements MeterBuilder {

    private final AmrSystem amrSystem;
    private final Provider<MeterImpl> meterFactory;
    private String amrId;
    private UUID mRID;
    private String name;
    private FiniteStateMachine finiteStateMachine;
    private String serialNumber;
    private Location location;
    private SpatialCoordinates spatialCoordinates;
    private Instant receivedDate;
    private String manufacturer;
    private String modelNbr;
    private String modelVersion;

    MeterBuilderImpl(AmrSystem amrSystem, Provider<MeterImpl> meterFactory, String amrId, String name) {
        this.amrSystem = amrSystem;
        this.meterFactory = meterFactory;
        this.amrId = amrId;
        this.name = name;
    }

    @Override
    public Meter create() {
        MeterImpl meter = meterFactory.get().init(amrSystem, amrId, name, mRID);
        if (finiteStateMachine != null) {
            meter.setFiniteStateMachine(finiteStateMachine);
        }
        meter.setSerialNumber(serialNumber);
        meter.setLocation(location);
        meter.setSpatialCoordinates(spatialCoordinates);
        meter.getLifecycleDates().setReceivedDate(receivedDate);
        meter.setManufacturer(manufacturer);
        meter.setModelNumber(modelNbr);
        meter.setModelVersion(modelVersion);
        meter.doSave();
        return meter;
    }

    @Override
    public MeterBuilder setMRID(String mRID) {
        this.mRID = UUID.fromString(mRID);
        return this;
    }

    @Override
    public MeterBuilder setAmrId(String amrId) {
        this.amrId = amrId;
        return this;
    }

    @Override
    public MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine) {
        this.finiteStateMachine = finiteStateMachine;
        return this;
    }

    @Override
    public MeterBuilder setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    @Override
    public MeterBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    @Override
    public MeterBuilder setSpatialCoordinates(SpatialCoordinates geoCoordinates) {
        this.spatialCoordinates = geoCoordinates;
        return this;
    }

    @Override
    public MeterBuilder setReceivedDate(Instant receivedDate) {
        this.receivedDate = receivedDate;
        return this;
    }

    @Override
    public MeterBuilder setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    @Override
    public MeterBuilder setModelNumber(String modelNbr) {
        this.modelNbr = modelNbr;
        return this;
    }

    @Override
    public MeterBuilder setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    @Override
    public LocationBuilder newLocationBuilder() {
        return new LocationBuilderImpl(meterFactory.get().getDataModel());
    }
}
