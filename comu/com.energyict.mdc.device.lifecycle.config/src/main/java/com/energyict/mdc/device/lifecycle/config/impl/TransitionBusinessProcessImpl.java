package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * Provides an implementation for the {@link TransitionBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (09:58)
 */
public class TransitionBusinessProcessImpl implements TransitionBusinessProcess {

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
    public TransitionBusinessProcessImpl(BpmService bpmService) {
        super();
        this.bpmService = bpmService;
    }

    TransitionBusinessProcessImpl initialize(String deploymentId, String processId) {
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
    public void executeOn(long deviceId, State currentState) {
        Map<String, Object> parameters = ImmutableMap.of(
                TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME, deviceId,
                TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME, currentState.getId());
        this.bpmService.startProcess(this.deploymentId, this.processId, parameters);
    }

}