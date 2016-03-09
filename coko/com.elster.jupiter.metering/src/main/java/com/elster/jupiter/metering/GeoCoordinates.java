package com.elster.jupiter.metering;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.geo.SpatialGeometryObject;

import java.util.Map;

@ProviderType
public interface GeoCoordinates {

    long getId();
    SpatialGeometryObject getCoordinates();

    //String getLat();
    //String getLong();
}
