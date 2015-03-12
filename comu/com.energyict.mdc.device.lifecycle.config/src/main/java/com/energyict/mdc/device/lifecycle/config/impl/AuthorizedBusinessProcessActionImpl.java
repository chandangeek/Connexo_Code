package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
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
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private Reference<State> state = ValueReference.absent();

    @Inject
    public AuthorizedBusinessProcessActionImpl(DataModel dataModel) {
        super(dataModel);
    }

    public boolean matches(String deploymentId, String processId) {
        return Checks.is(this.deploymentId).equalTo(deploymentId)
                && Checks.is(this.processId).equalTo(processId);
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