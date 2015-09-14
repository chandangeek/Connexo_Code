package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.pubsub.Publisher;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesRequiredException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RegisterGroupImpl extends PersistentNamedObject<RegisterGroup> implements RegisterGroup {

    private final Publisher publisher;
    @Size(min= 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.AT_LEAST_ONE_REGISTER_TYPE_REQUIRED + "}")
    private List<RegisterTypeInGroup> registerTypeInGroups = new ArrayList<>();
    private ChangeNotifier changeNotifier = new NotNotifiedYet();

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= 256, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    @Inject
    public RegisterGroupImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Publisher publisher) {
        super(RegisterGroup.class, dataModel, eventService, thesaurus);
        this.publisher = publisher;
    }

    static RegisterGroupImpl from (DataModel dataModel, String name) {
        return dataModel.getInstance(RegisterGroupImpl.class).initialize(name);
    }

    RegisterGroupImpl initialize (String name) {
        this.setName(name);
        return this;
    }

    @Override
    protected void validateDelete() {
        // Send notification to dependent modules that may have reference to this RegisterGroup
        this.getEventService().postEvent(EventType.REGISTERGROUP_VALIDATEDELETE.topic(), this);
    }

    @Override
    protected void doDelete() {
        this.removeRegisterTypes();
        if (this.getId() > 0) {
            this.getDataMapper().remove(this);
            this.changeNotifier.deleted();
        }
    }

    private void removeRegisterTypes() {
        this.registerTypeInGroups.clear();
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
        this.changeNotifier.added(registerType);
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
        this.changeNotifier.removed(registerType);
    }

    @Override
    public void updateRegisterTypes(List<RegisterType> registerTypes) {
        this.removeObsoleteRegisterTypes(registerTypes);
        this.addNewRegisterTypes(registerTypes);
        this.changeNotifier.updated();
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

    @Override
    protected void postNew() {
        super.postNew();
        this.resetChangeNotifier();
    }

    @Override
    protected void post() {
        super.post();
        this.resetChangeNotifier();
    }

    private void cleanRegisterTypeCache() {
        this.publisher.publish(new InvalidateCacheRequest(MasterDataService.COMPONENTNAME, TableSpecs.MDS_MEASUREMENTTYPE.name()));
    }

    private void touchIfPersistent() {
        if (this.getId() > 0) {
            this.dataModel.touch(this);
        }
    }

    private void resetChangeNotifier() {
        this.changeNotifier = new NotNotifiedYet();
    }

    private void alreadyNotified() {
        this.changeNotifier = new AlreadyNotified();
    }

    /**
     * Notifies others of changes that were applied
     * to this RegisterGroup, including the OrmService
     * that is notified that the version needs to be bumped.
     */
    private interface ChangeNotifier {

        void added(RegisterType registerType);

        void removed(RegisterType registerType);

        void updated();

        void deleted();

    }

    /**
     * Provides an implementation for the ChangeNotifier interface
     * that will notify the interested parties and then switch over
     * to another component that will no longer notify interested
     * parties as one notification is enough.
     */
    private class NotNotifiedYet implements ChangeNotifier {
        @Override
        public void added(RegisterType registerType) {
            this.updated();
        }

        @Override
        public void removed(RegisterType registerType) {
            this.updated();
        }

        @Override
        public void updated() {
            cleanRegisterTypeCache();
            touchIfPersistent();
            alreadyNotified();
        }

        @Override
        public void deleted() {
            cleanRegisterTypeCache();
        }
    }

    /**
     * Provides an implementation for the ChangeNotifier interface
     * that is used when the other components have already been
     * notified. Sending out one notification is enough
     * to avoid that we are sued for stalking ;-)
     */
    private class AlreadyNotified implements ChangeNotifier {
        @Override
        public void added(RegisterType registerType) {
        }

        @Override
        public void removed(RegisterType registerType) {
        }

        @Override
        public void updated() {
        }

        @Override
        public void deleted() {
        }
    }
}