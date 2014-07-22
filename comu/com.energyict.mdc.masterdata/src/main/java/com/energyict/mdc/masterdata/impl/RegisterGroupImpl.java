package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private Date modificationDate;
    private List<RegisterMappingInGroup> mappingsInGroup = new ArrayList<>();

    private final Clock clock;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= 256, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

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
        if (!getRegisterMappingInGroup().isEmpty()) {
            List<RegisterMapping> registerMappings = new ArrayList<>();
            for(RegisterMappingInGroup mappingInGroup : getRegisterMappingInGroup()){
                registerMappings.add(mappingInGroup.getRegisterMapping());
            }

            throw CannotDeleteBecauseStillInUseException.registerGroupIsStillInUse(this.getThesaurus(), this, registerMappings);
        }
    }

    private List<RegisterMappingInGroup> getRegisterMappingInGroup() {
        if (mappingsInGroup == null) {
            mappingsInGroup = dataModel.mapper(RegisterMappingInGroup.class).find("registerGroup", this);
        }
        return mappingsInGroup;
    }

    @Override
    public List<RegisterMapping> getRegisterMappings() {
        List<RegisterMapping> registerMappings = new ArrayList<>();
        for(RegisterMappingInGroup mappingInGroup : getRegisterMappingInGroup()){
            registerMappings.add(mappingInGroup.getRegisterMapping());
        }

        return registerMappings;
    }

    @Override
    public void addRegisterMapping(RegisterMapping registerMapping) {
        RegisterMappingInGroup mappingInGroup = RegisterMappingInGroup.from(dataModel, this, registerMapping);
        mappingInGroup.persist();
        getRegisterMappingInGroup().add(mappingInGroup);
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        Iterator<RegisterMappingInGroup> it = getRegisterMappingInGroup().iterator();
        while (it.hasNext()) {
            RegisterMappingInGroup each = it.next();
            if (each.getRegisterMapping().equals(registerMapping)) {
                each.delete();
                it.remove();
            }
        }
    }

    @Override
    public void removeRegisterMappings() {
        Iterator<RegisterMappingInGroup> it = getRegisterMappingInGroup().iterator();
        while (it.hasNext()) {
            RegisterMappingInGroup each = it.next();
            each.delete();
        }

        mappingsInGroup = null;
    }

    @Override
    public boolean updateRegisterMappings(HashMap<Long, RegisterMapping> registerMappings){
        HashMap<Long, RegisterMapping> existing = new HashMap<>();
        for(RegisterMappingInGroup mappingInGroup : getRegisterMappingInGroup()){
            existing.put(mappingInGroup.getRegisterMapping().getId(), mappingInGroup.getRegisterMapping());
        }

        boolean modified = false;
        modified |= unlinkToRegisterMappings(existing, registerMappings);
        modified |= linkToRegisterMappings(existing, registerMappings);
        return modified;
    }

    private boolean unlinkToRegisterMappings(HashMap<Long, RegisterMapping> current, HashMap<Long, RegisterMapping> target){
        HashMap<Long, RegisterMapping> toRemove = new HashMap<>(current);
        for(Map.Entry entry : target.entrySet()){
            if(toRemove.containsKey(entry.getKey())){
                toRemove.remove(entry.getKey());
            }
        }
        boolean modified = (toRemove.size()>0);
        for (Map.Entry entry : toRemove.entrySet()) {
            modified = true;
            removeRegisterMapping((RegisterMapping) entry.getValue());
        }

        return modified;
    }

    private boolean linkToRegisterMappings(HashMap<Long, RegisterMapping> current, HashMap<Long, RegisterMapping> target){
        HashMap<Long, RegisterMapping> toAdd = new HashMap<>(target);
        for(Map.Entry entry : current.entrySet()){
            if(toAdd.containsKey(entry.getKey())){
                toAdd.remove(entry.getKey());
            }
        }
        boolean modified = (toAdd.size()>0);
        for (Map.Entry entry : toAdd.entrySet()) {
            addRegisterMapping((RegisterMapping) entry.getValue());
        }

        return modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

}