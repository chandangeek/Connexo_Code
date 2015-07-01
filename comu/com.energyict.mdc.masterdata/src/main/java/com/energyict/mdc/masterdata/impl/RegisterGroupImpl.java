package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesRequiredException;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    protected void validateDelete() {
        // Nothing to validate
    }

    @Override
    protected void doDelete() {
        this.removeRegisterTypes();
        this.getDataMapper().remove(this);
    }

    private void removeRegisterTypes() {
        this.registerTypeInGroups.clear();;
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

    @Override
    public List<RegisterType> getRegisterTypes() {
        return this.registerTypeInGroups
                .stream()
                .map(RegisterTypeInGroup::getRegisterType)
                .collect(Collectors.toList());
    }

    @Override
    public void addRegisterType(RegisterType registerType) {
        this.registerTypeInGroups.add(RegisterTypeInGroup.from(this.dataModel, this, registerType));
        if (this.getId() > 0) {
            this.dataModel.touch(this);
        }
    }

    @Override
    public void removeRegisterType(RegisterType registerType) {
        Iterator<RegisterTypeInGroup> it = this.registerTypeInGroups.iterator();
        while (it.hasNext()) {
            RegisterTypeInGroup each = it.next();
            if (each.getRegisterType().getId() == registerType.getId()) {
                it.remove();
            }
        }
        this.checkAtLeastOneRegisterType();
        this.dataModel.touch(this);
    }

    @Override
    public void updateRegisterTypes(List<RegisterType> registerTypes) {
        this.removeObsoleteRegisterTypes(registerTypes);
        this.addNewRegisterTypes(registerTypes);
        this.checkAtLeastOneRegisterType();
        this.dataModel.touch(this);
    }

    private void checkAtLeastOneRegisterType() {
        if (this.registerTypeInGroups.isEmpty()) {
            throw new RegisterTypesRequiredException();
        }
    }

    private void removeObsoleteRegisterTypes(List<RegisterType> registerTypes) {
        List<Long> registerTypeIds = registerTypes.stream().map(RegisterType::getId).collect(Collectors.toList());
        List<RegisterTypeInGroup> toBeRemoved =
                this.registerTypeInGroups
                        .stream()
                        .filter(each -> !registerTypeIds.contains(each.getRegisterType().getId()))
                        .collect(Collectors.toList());
        this.registerTypeInGroups.removeAll(toBeRemoved);
    }

    private void addNewRegisterTypes(List<RegisterType> registerTypes) {
        Set<Long> knownRegisterTypeIds =
                this.registerTypeInGroups
                    .stream()
                        .map(RegisterTypeInGroup::getRegisterType)
                        .map(RegisterType::getId)
                        .collect(Collectors.toSet());
        List<RegisterTypeInGroup> toBeAdded = registerTypes
                .stream()
                .filter(each -> !knownRegisterTypeIds.contains(each.getId()))
                .map(each -> RegisterTypeInGroup.from(this.dataModel, this, each))
                .collect(Collectors.toList());
        this.registerTypeInGroups.addAll(toBeAdded);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

}