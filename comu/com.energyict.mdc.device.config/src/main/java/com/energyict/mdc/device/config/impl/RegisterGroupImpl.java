package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private Date modificationDate;

    private Clock clock;

    @Inject
    public RegisterGroupImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(RegisterGroup.class, dataModel, eventService, thesaurus);
        this.clock = clock;
    }

    RegisterGroupImpl initialize (String name) {
        this.setName(name);
        return this;
    }

    static RegisterGroupImpl from (DataModel dataModel, String name) {
        return dataModel.getInstance(RegisterGroupImpl.class).initialize(name);
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        throw NameIsRequiredException.registerGroupNameIsRequired(thesaurus);
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.registerGroupAlreadyExists(thesaurus, name);
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        super.save();
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

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERGROUP;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERGROUP;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERGROUP;
    }

    @Override
    public String toString () {
        return this.getName();
    }

    protected void validateDelete() {
        List<RegisterMapping> registerMappings = this.mapper(RegisterMapping.class).find("registerGroup", this.getId());
        if (!registerMappings.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerGroupIsStillInUse(this.getThesaurus(), this, registerMappings);
        }
    }

}