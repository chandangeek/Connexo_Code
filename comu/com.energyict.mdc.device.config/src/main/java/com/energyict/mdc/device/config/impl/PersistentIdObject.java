package com.energyict.mdc.device.config.impl;

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
public abstract class PersistentIdObject<T> {

    private long id;

    protected Class<T> domainClass;
    protected DataModel dataModel;
    protected EventService eventService;
    protected Thesaurus thesaurus;

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
        }
    }

    public void delete() {
        this.validateDelete();
        this.doDelete();
        this.notifyDeleted();
    }

    private void notifyUpdated() {
        this.eventService.postEvent(EventType.UPDATED.topic(), this);
    }

    private void notifyDeleted() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    /**
     * Saves this object for the first time.
     */
    protected abstract void postNew();

    /**
     * Updates the changes made to this object.
     */
    protected abstract void post();

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

    public void setId(long id){
        this.id = id;
    }
}
