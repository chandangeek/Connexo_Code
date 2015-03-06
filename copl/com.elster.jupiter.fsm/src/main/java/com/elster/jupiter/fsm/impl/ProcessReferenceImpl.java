package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link ProcessReference} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:02)
 */
public class ProcessReferenceImpl implements ProcessReference {

    public enum Fields {
        STATE("state"),
        DEPLOYMENT_ID("deploymentId"),
        PROCESS_ID("processId"),
        PURPOSE("purpose"),
        POSITION("position");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private int position;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;
    @IsPresent
    private Reference<State> state = Reference.empty();
    @NotNull
    private Purpose purpose;

    private enum Purpose {
        OnEntry, OnExit;
    }

    ProcessReferenceImpl onEntry(State state, String deploymentId, String processId) {
        return this.initialize(state, Purpose.OnEntry, deploymentId, processId);
    }

    ProcessReferenceImpl onExit(State state, String deploymentId, String processId) {
        return this.initialize(state, Purpose.OnExit, deploymentId, processId);
    }

    private ProcessReferenceImpl initialize(State state, Purpose purpose, String deploymentId, String processId) {
        this.state.set(state);
        this.purpose = purpose;
        this.deploymentId = deploymentId;
        this.processId = processId;
        return this;
    }

    public boolean isOnEntry() {
        return Purpose.OnEntry.equals(this.purpose);
    }

    public boolean isOnExit() {
        return Purpose.OnExit.equals(this.purpose);
    }

    @Override
    public String getDeploymentId() {
        return this.deploymentId;
    }

    @Override
    public String getProcessId() {
        return this.processId;
    }

}