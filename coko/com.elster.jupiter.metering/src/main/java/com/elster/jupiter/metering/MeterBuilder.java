package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.time.Instant;

public interface MeterBuilder {

    Meter create();

    /**
     * Sets custom MRID for this builder.
     * @param mRID A Java-formatted {@link String} representation of {@link java.util.UUID UUID},
     * i.e. one that can be obtained as {@code UUID.randomUUID().toString()}.
     * @return The self.
     */
    MeterBuilder setMRID(String mRID);

    MeterBuilder setAmrId(String amrId);

    MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine);

    MeterBuilder setSerialNumber(String serialNumber);

    MeterBuilder setLocation(Location location);

    MeterBuilder setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

    MeterBuilder setReceivedDate(Instant receivedDate);

    MeterBuilder setManufacturer(String manufacturer);

    MeterBuilder setModelNumber(String modelNumber);

    MeterBuilder setModelVersion(String modelVersion);

    LocationBuilder newLocationBuilder();

}
