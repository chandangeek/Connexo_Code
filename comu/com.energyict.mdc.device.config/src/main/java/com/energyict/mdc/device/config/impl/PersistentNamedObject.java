package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
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
public abstract class PersistentNamedObject<T> extends PersistentIdObject<T> {

    private String name;

    protected PersistentNamedObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
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

    protected void validateName(String newName) {
        if (newName == null) {
            throw nameIsRequiredException(this.getThesaurus());
        }
        if (newName.trim().isEmpty()) {
            throw nameIsRequiredException(this.getThesaurus());
        }
    }

    protected abstract NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus);

    protected void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw this.duplicateNameException(this.getThesaurus(), name);
        }
    }

    protected abstract DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name);

    private T findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

}