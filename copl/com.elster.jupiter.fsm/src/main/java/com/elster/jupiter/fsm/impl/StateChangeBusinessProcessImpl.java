package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Map;

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

    private final BpmService bpmService;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;

    @Inject
    public StateChangeBusinessProcessImpl(BpmService bpmService) {
        super();
        this.bpmService = bpmService;
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
        this.executeWithChangeType(sourceId, state, "entry");
    }

    @Override
    public void executeOnExit(String sourceId, State state) {
        this.executeWithChangeType(sourceId, state, "exit");
    }

    private void executeWithChangeType(String sourceId, State state, String changeType) {
        Map<String, Object> parameters = ImmutableMap.of(
                SOURCE_ID_BPM_PARAMETER_NAME, sourceId,
                STATE_ID_BPM_PARAMETER_NAME, state.getId(),
                CHANGE_TYPE_BPM_PARAMETER_NAME, changeType);
        this.bpmService.startProcess(this.deploymentId, this.processId, parameters);
    }

}