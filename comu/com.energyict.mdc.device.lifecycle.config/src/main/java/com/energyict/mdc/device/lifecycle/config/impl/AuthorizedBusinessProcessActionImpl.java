package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link AuthorizedBusinessProcessAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (13:04)
 */
public class AuthorizedBusinessProcessActionImpl extends AuthorizedActionImpl implements AuthorizedBusinessProcessAction {

    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private Reference<TransitionBusinessProcess> process = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private Reference<State> state = ValueReference.absent();

    @Inject
    public AuthorizedBusinessProcessActionImpl(DataModel dataModel) {
        super(dataModel);
    }

    AuthorizedBusinessProcessActionImpl initialize(DeviceLifeCycleImpl deviceLifeCycle, State state, String name, TransitionBusinessProcess process) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.name = name;
        this.state.set(state);
        this.process.set(process);
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TransitionBusinessProcess getTransitionBusinessProcess() {
        return this.process.get();
    }

    @Override
    public State getState() {
        return this.state.get();
    }

}