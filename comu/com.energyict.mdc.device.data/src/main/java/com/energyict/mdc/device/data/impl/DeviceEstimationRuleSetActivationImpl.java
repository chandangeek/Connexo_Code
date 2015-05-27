package com.energyict.mdc.device.data.impl;

import java.time.Instant;

import javax.inject.Inject;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

public class DeviceEstimationRuleSetActivationImpl implements DeviceEstimationRuleSetActivation {

    public enum Fields {
        ESTIMATIONRULESET("estimationRuleSet"),
        ESTIMATIONACTIVATION("estimationActivation"),
        ACTIVE("active")
        ;
        
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }
    
    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();
    private Reference<DeviceEstimation> estimationActivation = ValueReference.absent();
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
    
    DeviceEstimationRuleSetActivation init(EstimationRuleSet estimationRuleSet, boolean active, DeviceEstimation estimationActivation) {
        this.estimationRuleSet.set(estimationRuleSet);
        this.active = active;
        this.estimationActivation.set(estimationActivation);
        return this;
    }
    
    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.get();
    }

    @Override
    public DeviceEstimation getDeviceEstimationActivation() {
        return estimationActivation.get();
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
