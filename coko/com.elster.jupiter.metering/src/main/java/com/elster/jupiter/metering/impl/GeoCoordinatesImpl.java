package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;
import java.time.Instant;


public class GeoCoordinatesImpl implements GeoCoordinates {


    private long id;
    private final DataModel dataModel;
    private SpatialCoordinates coordinates;
    private Instant createTime;
    private Instant modTime;

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

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
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
