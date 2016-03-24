package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;


public class GeoCoordinatesImpl implements GeoCoordinates {


    private long id;
    private final DataModel dataModel;
    private SpatialCoordinates coordinates;

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

    GeoCoordinatesImpl init(SpatialCoordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    static GeoCoordinatesImpl from(DataModel dataModel, SpatialCoordinates coordinates) {
        return dataModel.getInstance(GeoCoordinatesImpl.class).init(coordinates);
    }

    @Override
    public long getId() {
        return id;
    }


    @Override
    public SpatialCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public void setCoordinates(SpatialCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    void doSave() {
        if (hasId()) {
            dataModel.mapper(GeoCoordinates.class).update(this);
            return;
        }
        dataModel.mapper(GeoCoordinates.class).persist(this);
    }

    private boolean hasId() {
        return id != 0L;
    }

    public void remove() {
        if (hasId()) {
            dataModel.mapper(GeoCoordinates.class).remove(this);
        }
    }

}
