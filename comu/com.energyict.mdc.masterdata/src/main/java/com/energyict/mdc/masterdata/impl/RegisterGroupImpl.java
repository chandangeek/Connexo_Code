package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private List<RegisterTypeInGroup> registerTypeInGroups = new ArrayList<>();

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= 256, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    @Inject
    public RegisterGroupImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(RegisterGroup.class, dataModel, eventService, thesaurus);
    }

    RegisterGroupImpl initialize (String name) {
        this.setName(name);
        return this;
    }

    static RegisterGroupImpl from (DataModel dataModel, String name) {
        return dataModel.getInstance(RegisterGroupImpl.class).initialize(name);
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
            registerTypeInGroups = dataModel.mapper(RegisterTypeInGroup.class).find("registerGroup", this);
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
        getRegisterTypesInGroup().forEach(com.energyict.mdc.masterdata.impl.RegisterTypeInGroup::delete);
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

    private boolean unlinkToRegisterTypes(HashMap<Long, RegisterType> current, Map<Long, RegisterType> target){
        Map<Long, RegisterType> toRemove = new HashMap<>(current);
        for (Map.Entry<Long, RegisterType> entry : target.entrySet()) {
            if (toRemove.containsKey(entry.getKey())) {
                toRemove.remove(entry.getKey());
            }
        }
        boolean modified = !toRemove.isEmpty();
        for (Map.Entry entry : toRemove.entrySet()) {
            modified = true;
            removeRegisterType((RegisterType) entry.getValue());
        }

        return modified;
    }

    private boolean linkToRegisterTypes(Map<Long, RegisterType> current, HashMap<Long, RegisterType> target){
        Map<Long, RegisterType> toAdd = new HashMap<>(target);
        for (Map.Entry<Long, RegisterType> entry : current.entrySet()) {
            if (toAdd.containsKey(entry.getKey())) {
                toAdd.remove(entry.getKey());
            }
        }
        boolean modified = !toAdd.isEmpty();
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