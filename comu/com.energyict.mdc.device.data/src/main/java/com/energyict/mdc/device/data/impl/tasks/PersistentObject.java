/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.UpdateEventType;

public abstract class PersistentObject<T> {

    protected final Class<T> domainClass;
    protected final DataModel dataModel;
    protected final EventService eventService;
    protected final Thesaurus thesaurus;

    private boolean notYetPersisted;

    protected PersistentObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
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

    /**
     * Marks the PersistentObject as not yet persisted to the dataModel<br/>
     * This will ensure when method #save() is called, an insert instead of update will be performed
     */
    public void markAsNotYetPersisted() {
        this.notYetPersisted = true;
    }

    public void update() {
        this.post();
        this.notifyUpdated();
    }

    /**
     * Updates the changes made to specified fields on this object without running validation.
     *
     * @param fieldNames The name of the fields that have changed and need updating
     */
    protected void update(String... fieldNames) {
        this.dataModel.update(this, fieldNames);
        this.notifyUpdated();
    }

    void save() {
        if (notYetPersisted) {
            this.postNew();
            this.notifyCreated();
        } else {
            update();
        }
    }

    public void delete() {
        this.validateDelete();
        this.doDelete();
        this.notifyDeleted();
    }

    protected void notifyCreated() {
        this.eventService.postEvent(this.createEventType().topic(), this);
    }

    protected abstract CreateEventType createEventType();

    protected void notifyUpdated() {
        this.eventService.postEvent(this.updateEventType().topic(), this);
    }

    protected abstract UpdateEventType updateEventType();

    protected void notifyDeleted() {
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
}