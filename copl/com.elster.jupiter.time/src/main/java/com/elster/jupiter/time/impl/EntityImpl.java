package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.Entity;
import com.elster.jupiter.time.EventType;

import java.time.Instant;

abstract class EntityImpl implements Entity {

    private final EventService eventService;

    private long id;

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
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

    void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    void setModTime(Instant modTime) {
        this.modTime = modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void save(){
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
        dataModel.remove(this);
    }
}
