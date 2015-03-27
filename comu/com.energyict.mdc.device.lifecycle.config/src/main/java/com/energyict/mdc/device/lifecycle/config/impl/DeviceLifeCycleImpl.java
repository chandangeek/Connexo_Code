package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.impl.constraints.Unique;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceLifeCycle} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:30)
 */
@Unique(message = "{" + MessageSeeds.Keys.UNIQUE_DEVICE_LIFE_CYCLE_NAME + "}", groups = { Save.Create.class, Save.Update.class })
public class DeviceLifeCycleImpl implements DeviceLifeCycle {

    public enum Fields {
        NAME("name"),
        STATE_MACHINE("stateMachine"),
        ACTIONS("actions");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = { Save.Create.class, Save.Update.class })
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    @Valid
    private List<AuthorizedAction> actions = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public DeviceLifeCycleImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DeviceLifeCycleImpl initialize(String name, FiniteStateMachine stateMachine) {
        this.name = name;
        this.stateMachine.set(stateMachine);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public FiniteStateMachine getFiniteStateMachine() {
        return this.stateMachine.get();
    }

    @Override
    public List<AuthorizedAction> getAuthorizedActions() {
        return Collections.unmodifiableList(this.actions);
    }

    @Override
    public List<AuthorizedAction> getAuthorizedActions(State state) {
        return this.actions
                .stream()
                .filter(a -> state.getId() == a.getState().getId())
                .collect(Collectors.toList());
    }

    void add(AuthorizedAction action) {
        this.actions.add(action);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreationTimestamp() {
        return createTime;
    }

    @Override
    public Instant getModifiedTimestamp() {
        return modTime;
    }

    @Override
    public void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }

    @Override
    public DeviceLifeCycleUpdater startUpdate() {
        return null;
    }
}