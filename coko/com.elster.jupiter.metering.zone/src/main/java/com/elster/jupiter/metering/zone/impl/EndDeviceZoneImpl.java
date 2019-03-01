/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.ZONE_TYPE_NOT_UNIQUE + "}")
class EndDeviceZoneImpl implements EndDeviceZone {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    private final Reference<EndDevice> endDevice = Reference.empty();
    private final Reference<Zone> zone = Reference.empty();
    private long zoneId;
    private long endDeviceId;
    private final DataModel dataModel;
    private final MeteringZoneService meteringZoneService;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    EndDeviceZoneImpl(DataModel dataModel, MeteringZoneService meteringZoneService) {
        this.dataModel = dataModel;
        this.meteringZoneService = meteringZoneService;
    }

    EndDeviceZoneImpl init(Zone zone, EndDevice endDevice) {
        this.zone.set(zone);
        this.endDevice.set(endDevice);
        return this;
    }

    static EndDeviceZoneImpl from(DataModel dataModel, Zone zone, EndDevice endDevice) {
        return dataModel.getInstance(EndDeviceZoneImpl.class).init(zone, endDevice);
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void save() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public void delete() {
        dataModel.mapper(EndDeviceZone.class).remove(this);
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndDeviceZoneImpl)) {
            return false;
        }
        EndDeviceZoneImpl that = (EndDeviceZoneImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Zone getZone() {
        return zone.get();
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice.get();
    }

    @Override
    public void setZone(Zone zone) {
        this.zone.set(zone);
        this.zoneId = zone.getId();
    }

    @Override
    public void setEndDevice(EndDevice endDevice) {
        this.endDevice.set(endDevice);
        this.endDeviceId = endDevice.getId();
    }

}