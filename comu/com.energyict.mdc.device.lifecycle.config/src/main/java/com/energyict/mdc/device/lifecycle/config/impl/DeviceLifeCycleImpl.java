package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.Valid;
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
public class DeviceLifeCycleImpl implements DeviceLifeCycle {

    public enum Fields {
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
    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = { Save.Create.class, Save.Update.class })
    private Reference<FinateStateMachine> stateMachine = ValueReference.absent();
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

    public DeviceLifeCycleImpl initialize(FinateStateMachine stateMachine) {
        this.stateMachine.set(stateMachine);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.getFinateStateMachine().getName();
    }

    @Override
    public FinateStateMachine getFinateStateMachine() {
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

}