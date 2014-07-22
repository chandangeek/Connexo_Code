package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private Date modificationDate;
    private List<RegisterTypeInGroup> registerTypeInGroups = new ArrayList<>();

    private final Clock clock;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= StringColumnLengthConstraints.REGISTER_GROUP_NAME, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
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
        if (!getRegisterTypesInGroup().isEmpty()) {
            List<RegisterType> registerTypes = new ArrayList<>();
            for(RegisterTypeInGroup registerTypeInGroup : getRegisterTypesInGroup()){
                registerTypes.add(registerTypeInGroup.getRegisterType());
            }

            throw CannotDeleteBecauseStillInUseException.registerGroupIsStillInUse(this.getThesaurus(), this, registerTypes);
        }
    }

    private List<RegisterTypeInGroup> getRegisterTypesInGroup() {
        if (registerTypeInGroups == null) {
            registerTypeInGroups = dataModel.mapper(RegisterTypeInGroup.class).find("registerGroupId", getId());
        }
        return registerTypeInGroups;
    }

    @Override
    public List<RegisterType> getRegisterTypes() {
        List<RegisterType> registerTypes = new ArrayList<>();
        for(RegisterTypeInGroup registerTypeInGroup : getRegisterTypesInGroup()){
            registerTypes.add(registerTypeInGroup.getRegisterType());
        }

        return registerTypes;
    }

    @Override
    public void addRegisterType(RegisterType registerType) {
        RegisterTypeInGroup mappingInGroup = RegisterTypeInGroup.from(dataModel, this, registerType);
        mappingInGroup.persist();
        getRegisterTypesInGroup().add(mappingInGroup);
    }

    @Override
    public void removeRegisterType(RegisterType registerType) {
        Iterator<RegisterTypeInGroup> it = getRegisterTypesInGroup().iterator();
        while (it.hasNext()) {
            RegisterTypeInGroup each = it.next();
            if (each.getRegisterType().equals(registerType)) {
                each.delete();
                it.remove();
            }
        }
    }

    @Override
    public void removeRegisterTypes() {
        Iterator<RegisterTypeInGroup> it = getRegisterTypesInGroup().iterator();
        while (it.hasNext()) {
            RegisterTypeInGroup each = it.next();
            each.delete();
        }

        registerTypeInGroups = null;
    }

    @Override
    public boolean updateRegisterTypes(HashMap<Long, RegisterType> registerTypes){
        HashMap<Long, RegisterType> existing = new HashMap<>();
        for(RegisterTypeInGroup mappingInGroup : getRegisterTypesInGroup()){
            existing.put(mappingInGroup.getRegisterType().getId(), mappingInGroup.getRegisterType());
        }

        boolean modified = false;
        modified |= unlinkToRegisterTypes(existing, registerTypes);
        modified |= linkToRegisterTypes(existing, registerTypes);
        return modified;
    }

    private boolean unlinkToRegisterTypes(HashMap<Long, RegisterType> current, HashMap<Long, RegisterType> target){
        HashMap<Long, RegisterType> toRemove = new HashMap<>(current);
        for(Map.Entry entry : target.entrySet()){
            if(toRemove.containsKey(entry.getKey())){
                toRemove.remove(entry.getKey());
            }
        }
        boolean modified = (toRemove.size()>0);
        for (Map.Entry entry : toRemove.entrySet()) {
            modified = true;
            removeRegisterType((RegisterType) entry.getValue());
        }

        return modified;
    }

    private boolean linkToRegisterTypes(HashMap<Long, RegisterType> current, HashMap<Long, RegisterType> target){
        HashMap<Long, RegisterType> toAdd = new HashMap<>(target);
        for(Map.Entry entry : current.entrySet()){
            if(toAdd.containsKey(entry.getKey())){
                toAdd.remove(entry.getKey());
            }
        }
        boolean modified = (toAdd.size()>0);
        for (Map.Entry entry : toAdd.entrySet()) {
            addRegisterType((RegisterType) entry.getValue());
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