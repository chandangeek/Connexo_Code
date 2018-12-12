/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

import javax.inject.Inject;
import java.time.Instant;

public class DeviceEstimationRuleSetActivationImpl implements DeviceEstimationRuleSetActivation {

    public enum Fields {
        ESTIMATIONRULESET("estimationRuleSet"),
        DEVICE("device"),
        ACTIVE("active");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Device> device = ValueReference.absent();
    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();
    private boolean active;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private DataModel dataModel;

    @Inject
    public DeviceEstimationRuleSetActivationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceEstimationRuleSetActivation init(Device device, EstimationRuleSet estimationRuleSet, boolean active) {
        this.estimationRuleSet.set(estimationRuleSet);
        this.active = active;
        this.device.set(device);
        return this;
    }

    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.get();
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            this.dataModel.update(this);
        }
    }
}
