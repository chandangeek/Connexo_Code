package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;

import java.util.List;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that are persistable and have a unique ID.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:29
 */
public abstract class PersistentIdObject<D> {

    @SuppressWarnings("unused")
    private long id;

    private Class<D> domainClass;
    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;

    protected PersistentIdObject(Class<D> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.domainClass = domainClass;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected DataMapper<D> getDataMapper() {
        return this.dataModel.mapper(this.domainClass);
    }

    protected QueryExecutor<D> queryJoinedWith(Class<?>... joinClasses) {
        return this.dataModel.query(this.domainClass, joinClasses);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected <T> DataMapper<T> mapper(Class<T> api) {
        return this.dataModel.mapper(api);
    }

    public void save () {
        if (this.id > 0) {
            this.validateAndUpdate();
            this.notifyUpdated();
        }
        else {
            this.validateAndCreate();
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

    protected void postEvent(EventType eventType) {
        this.eventService.postEvent(eventType.topic(), this);
    }

    protected abstract UpdateEventType updateEventType();

    private void notifyDeleted() {
        this.eventService.postEvent(this.deleteEventType().topic(), this);
    }

    protected abstract DeleteEventType deleteEventType();

    /**
     * Validates and saves this object for the first time.
     */
    protected void validateAndCreate() {
        Save.CREATE.save(this.dataModel, this);
    }

    /**
     * Validates and updates the changes made to this object.
     */
    protected void validateAndUpdate() {
        Save.UPDATE.save(this.dataModel, this);
    }

    /**
     * Updates the changes made to this object without running validation.
     */
    protected void update() {
        this.getDataModel().update(this);
    }

    /**
     * Updates the changes made to specified fields on this object without running validation.
     *
     * @param fieldNames The name of the fields that have changed and need updating
     */
    protected void update(String... fieldNames) {
        this.getDataModel().update(this, fieldNames);
    }

    /**
     * Updates the changes made to specified fields on this object without running validation.
     *
     * @param fieldNames The name of the fields that have changed and need updating
     */
    protected void update(List<String> fieldNames) {
        this.update(fieldNames.toArray(new String[fieldNames.size()]));
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
