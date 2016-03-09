package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.SpatialGeometryObject;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

public class GeoCoordinatesImpl implements GeoCoordinates{


    private long id;
    private final DataModel dataModel;
    private SpatialGeometryObject coordinates;
  //  private String coordLat;
  //  private String coordLong;
    /*private final String getSDOCoordinatesFunction=
            "create or replace function get_coordinates(longitude in number, \n" +
                    "                                           latitude in number)\n" +
                    "return MDSYS.SDO_GEOMETRY deterministic is\n" +
                    "begin\n" +
                    "     return MDSYS.SDO_GEOMETRY(\n" +
                    "     2001\n" +
                    "    ,8307 -- SRID\n" +
                    "    ,MDSYS.SDO_POINT_TYPE(longitude, latitude, NULL),NULL, NULL);\n" +
                    "end;\n" +
                    "/";

*/
    @Inject
    GeoCoordinatesImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    GeoCoordinatesImpl init(SpatialGeometryObject coordinates) {
        this.coordinates=coordinates;
        return this;
    }

    static GeoCoordinates from(DataModel dataModel, SpatialGeometryObject coordinates) {
        return dataModel.getInstance(GeoCoordinatesImpl.class).init(coordinates);
    }


   /* GeoCoordinatesImpl init(String coordLat, String coordLong) {
        this.coordLat=coordLat;
        this.coordLong = coordLong;
        return this;
    }

    static GeoCoordinates from(DataModel dataModel, String coordLat, String coordLong) {
        return dataModel.getInstance(GeoCoordinatesImpl.class).init(coordLat, coordLong);
    }
*/
    @Override
    public long getId() {
        return id;
    }



    @Override
    public SpatialGeometryObject getCoordinates() { return coordinates; }

/*
    @Override
    public String getLat() {
        return coordLat;
    }

    @Override
    public String getLong() {
        return coordLong;
    }

  */
}
