package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that are persistable and have a unique ID
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:29
 */
public abstract class PersistentIdObject<D> {

    private long id;

    protected Class<D> domainClass;
    protected DataModel dataModel;
    protected EventService eventService;
    protected Thesaurus thesaurus;

    protected PersistentIdObject(Class<D> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.domainClass = domainClass;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected DataMapper<D> getDataMapper() {
        return this.dataModel.mapper(this.domainClass);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected <T> DataMapper<T> mapper(Class<T> api) {
        return this.dataModel.mapper(api);
    }

    public void save () {
        if (this.id > 0) {
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

}
