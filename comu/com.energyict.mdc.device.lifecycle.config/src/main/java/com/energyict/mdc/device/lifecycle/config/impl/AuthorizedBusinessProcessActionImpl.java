package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import org.hibernate.validator.constraints.NotEmpty;

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
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private Reference<State> state = ValueReference.absent();

    AuthorizedBusinessProcessActionImpl initialize(DeviceLifeCycle deviceLifeCycle, State state, String name, String deploymentId, String processId) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.state.set(state);
        this.name = name;
        this.deploymentId = deploymentId;
        this.processId = processId;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDeploymentId() {
        return this.deploymentId;
    }

    @Override
    public String getProcessId() {
        return this.processId;
    }

    @Override
    public State getState() {
        return this.state.get();
    }

}