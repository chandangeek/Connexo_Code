package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.scheduling.events.CreateEventType;
import com.energyict.mdc.scheduling.events.DeleteEventType;
import com.energyict.mdc.scheduling.events.UpdateEventType;

import java.util.Objects;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that are persistable and have a unique ID
 * <p>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:29
 */
public abstract class PersistentIdObject<T> {

    private long id;

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

    public void update() {
        this.post();
        this.notifyUpdated();
    }

    void save() {
        if (id > 0) {
            update();
        } else {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersistentIdObject that = (PersistentIdObject) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
