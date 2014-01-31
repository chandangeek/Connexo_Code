package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that have a name that is unique across all entities of the same type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (13:38)
 */
public abstract class PersistentNamedObject<T> {

    private long id;
    private String name;

    private Class<T> domainClass;
    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;

    protected PersistentNamedObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super();
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
     * and throws a {@link LocalizedException} if that is not the case.
     */
    protected abstract void validateDelete();

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    private void validateName(String newName) {
        if (newName == null) {
            throw nameIsRequiredException(this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw nameIsRequiredException(this.thesaurus);
        }
    }

    protected abstract NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus);

    private void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw DuplicateNameException.registerMappingAlreadyExists(this.thesaurus, name);
        }
    }

    private T findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

}