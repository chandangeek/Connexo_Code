/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;

public abstract class PersistentIdObject<T> {

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    protected final Class<T> domainClass;
    protected final DataModel dataModel;
    protected final EventService eventService;
    protected final Thesaurus thesaurus;

    protected PersistentIdObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.domainClass = domainClass;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected DataMapper<T> getDataMapper() {
        return this.dataModel.mapper(this.domainClass);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected <U> DataMapper<U> mapper(Class<U> api) {
        return this.dataModel.mapper(api);
    }

    protected EventService getEventService() {
        return eventService;
    }

    public void save () {
        if (this.getId() > 0) {
            this.post();
            this.notifyUpdated();
        }
        else {
            this.postNew();
            this.notifyCreated();
        }
    }

    public void delete() {
        this.validateDelete();
        this.doDelete();
        this.notifyDeleted();
    }

    public long getVersion() {
        return version;
    }

    private void notifyCreated() {
        this.eventService.postEvent(this.createEventType().topic(), this);
    }

    protected abstract CreateEventType createEventType();

    private void notifyUpdated() {
        this.eventService.postEvent(this.updateEventType().topic(), this);
    }

    protected abstract UpdateEventType updateEventType();

    private void notifyDeleted() {
        this.eventService.postEvent(this.deleteEventType().topic(), this);
    }

    protected abstract DeleteEventType deleteEventType();

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        Save.CREATE.save(this.dataModel, this, Save.Create.class);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        Save.UPDATE.save(this.dataModel, this, Save.Update.class);
    }


    /**
     * Deletes this object using the mapper.
     */
    protected abstract void doDelete();

    /**
     * Validates that this object can safely be deleted
     * and throws a {@link com.elster.jupiter.nls.LocalizedException} if that is not the case.
     */
    protected abstract void validateDelete();

    public long getId() {
        return id;
    }

    public Instant getModTime() {
        return modTime;
    }

}
