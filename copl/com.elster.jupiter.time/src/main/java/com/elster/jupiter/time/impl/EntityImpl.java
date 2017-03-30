/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.Entity;
import com.elster.jupiter.time.EventType;

import java.time.Instant;
import java.util.Objects;

abstract class EntityImpl implements Entity {

    private final EventService eventService;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    // Audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private DataModel dataModel;

    EntityImpl(DataModel dataModel, EventService eventService){
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    final DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    void save(){
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
            eventService.postEvent(created().topic(), this);
        } else {
            Save.UPDATE.save(dataModel, this);
            eventService.postEvent(updated().topic(), this);
        }
    }

    abstract EventType created();
    abstract EventType updated();
    abstract EventType deleted();

    final EventService getEventService() {
        return eventService;
    }

    @Override
    public void delete(){
        eventService.postEvent(deleted().topic(), this);
        this.doDelete();
    }

    protected void doDelete() {
        dataModel.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityImpl that = (EntityImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
