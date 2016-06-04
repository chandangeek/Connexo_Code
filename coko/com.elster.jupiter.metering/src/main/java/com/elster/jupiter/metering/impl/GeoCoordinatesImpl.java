package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Save.Update;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;
import java.time.Instant;


@ValidCoordinates(groups = {Save.Create.class, Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_LOCATION_ENTRY + "}")
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
            Save.UPDATE.save(dataModel, this);
            return;
        }
        Save.CREATE.save(dataModel, this);
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
