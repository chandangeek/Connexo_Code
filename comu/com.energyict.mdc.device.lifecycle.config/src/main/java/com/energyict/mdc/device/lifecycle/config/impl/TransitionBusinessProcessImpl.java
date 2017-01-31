/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link TransitionBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (09:58)
 */
public class TransitionBusinessProcessImpl implements TransitionBusinessProcess {

    public enum Fields {
        NAME("name"),
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
    private String name;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String deploymentId;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= 256, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String processId;

    @Inject
    public TransitionBusinessProcessImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    TransitionBusinessProcessImpl initialize(String name, String deploymentId, String processId) {
        this.name = name;
        this.deploymentId = deploymentId;
        this.processId = processId;
        return this;
    }

    @Override
    public String getName() {
        return name;
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
        this.dataModel
                .getInstance(TransitionBusinessProcessStartEventImpl.class)
                .initialize(this, deviceId, currentState)
                .publish();
    }

}