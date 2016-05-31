package com.elster.jupiter.metering;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.time.Instant;

@ProviderType
public interface GeoCoordinates {

    long getId();

    SpatialCoordinates getCoordinates();

    void setCoordinates(SpatialCoordinates coordinates);

    Instant getCreateTime();

    Instant getModTime();
}
