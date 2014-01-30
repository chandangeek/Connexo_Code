package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.pluggable.impl.EventType;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class RegisterGroupImpl implements RegisterGroup {

    private long id;
    @NotNull
    private String name;
    private Date modificationDate;

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private Clock clock;

    @Inject
    public RegisterGroupImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    RegisterGroupImpl initialize (String name) {
        this.setName(name);
        return this;
    }

    static RegisterGroupImpl from (DataModel dataModel, String name) {
        return dataModel.getInstance(RegisterGroupImpl.class).initialize(name);
    }

    private void validateName(String newName) {
        if (newName == null) {
            throw NameIsRequiredException.registerGroupNameIsRequired(this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw NameIsRequiredException.registerGroupNameIsRequired(this.thesaurus);
        }
    }

    private void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw DuplicateNameException.registerGroupAlreadyExists(this.thesaurus, name);
        }
    }

    private RegisterGroup findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

    private DataMapper<RegisterGroup> getDataMapper() {
        return this.dataModel.mapper(RegisterGroup.class);
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        if (this.id > 0) {
            this.post();
        }
        else {
            this.postNew();
        }
    }

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.getDataMapper().update(this);
    }


    public void delete() {
        this.validateDelete();
        this.notifyDependents();
        this.getDataMapper().remove(this);
    }

    private void notifyDependents() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    @Override
    public String toString () {
        return this.getName();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    private void validateDelete() {
        List<RegisterMapping> registerMappings = this.dataModel.mapper(RegisterMapping.class).find("registerGroup", this.getId());
        if (!registerMappings.isEmpty()) {
            CannotDeleteBecauseStillInUseException.registerGroupIsStillInUse(this.thesaurus, this, registerMappings);
        }
    }

}