package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateChangeBusinessProcessStartEvent;
import com.elster.jupiter.orm.DataModel;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link StateChangeBusinessProcess}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-29 (13:54)
 */
public class StateChangeBusinessProcessImpl implements StateChangeBusinessProcess {

    public enum Fields {
        DEPLOYMENT_ID("deploymentId"),
        PROCESS_ID("processId");

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
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;

    @Inject
    public StateChangeBusinessProcessImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    StateChangeBusinessProcessImpl initialize(String deploymentId, String processId) {
        this.deploymentId = deploymentId;
        this.processId = processId;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
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
    public void executeOnEntry(String sourceId, State state) {
        this.executeWithChangeType(sourceId, state, StateChangeBusinessProcessStartEvent.Type.ENTRY);
    }

    @Override
    public void executeOnExit(String sourceId, State state) {
        this.executeWithChangeType(sourceId, state, StateChangeBusinessProcessStartEvent.Type.EXIT);
    }

    private void executeWithChangeType(String sourceId, State state, StateChangeBusinessProcessStartEvent.Type changeType) {
        this.dataModel
                .getInstance(StateChangeBusinessProcessStartEventImpl.class)
                .initialize(this, sourceId, state, changeType)
                .publish();
    }

}