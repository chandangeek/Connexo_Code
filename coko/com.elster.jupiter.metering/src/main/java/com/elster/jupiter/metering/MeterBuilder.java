package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.time.Instant;

public interface MeterBuilder {

    Meter create();

    MeterBuilder setMRID(String mRID);

    MeterBuilder setAmrId(String amrId);

    MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine);

    MeterBuilder setSerialNumber(String serialNumber);

    MeterBuilder setLocation(Location location);

    MeterBuilder setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

    MeterBuilder setReceivedDate(Instant receivedDate);

    LocationBuilder newLocationBuilder();

}
