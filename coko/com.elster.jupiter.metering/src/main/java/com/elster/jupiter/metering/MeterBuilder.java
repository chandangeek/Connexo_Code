package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.util.geo.SpatialCoordinates;

public interface MeterBuilder {

    Meter create();

    MeterBuilder setMRID(String mRID);

    MeterBuilder setAmrId(String amrId);

    MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine);

    MeterBuilder setName(String name);

    MeterBuilder setSerialNumber(String serialNumber);

    MeterBuilder setLocation(Location location);

    MeterBuilder setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

    LocationBuilder newLocationBuilder();

}
