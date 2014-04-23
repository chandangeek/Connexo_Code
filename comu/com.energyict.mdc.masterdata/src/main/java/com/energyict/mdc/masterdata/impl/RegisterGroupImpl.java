package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.RegisterMappingInGroup;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private Date modificationDate;
    private List<RegisterMappingInGroup> mappingsInGroup = new ArrayList<>();

    private final Clock clock;

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
    public void save () {
        this.modificationDate = this.clock.now();
        super.save();
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
        if (!mappingsInGroup.isEmpty()) {
            List<RegisterMapping> registerMappings = new ArrayList<>();
            for(RegisterMappingInGroup mappingInGroup : mappingsInGroup){
                registerMappings.add(mappingInGroup.getRegisterMapping());
            }

            throw CannotDeleteBecauseStillInUseException.registerGroupIsStillInUse(this.getThesaurus(), this, registerMappings);
        }
    }

    @Override
    public List<RegisterMapping> getRegisterMappings() {
        List<RegisterMapping> registerMappings = new ArrayList<>();
        for(RegisterMappingInGroup mappingInGroup : mappingsInGroup){
            registerMappings.add(mappingInGroup.getRegisterMapping());
        }

        return registerMappings;
    }

    @Override
    public void addRegisterMapping(RegisterMapping registerMapping) {
        RegisterMappingInGroupImpl mappingInGroup = new RegisterMappingInGroupImpl(dataModel).init(this, registerMapping);
        if(!mappingsInGroup.contains(mappingInGroup)){
            mappingsInGroup.add(mappingInGroup);
        }
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        RegisterMappingInGroup mappingInGroup = new RegisterMappingInGroupImpl(dataModel).init(this, registerMapping);
        if(mappingsInGroup.contains(mappingInGroup)){
            mappingsInGroup.remove(mappingInGroup);
        }
    }

    @Override
    public void removeRegisterMappings(){
        mappingsInGroup.clear();
    }
}