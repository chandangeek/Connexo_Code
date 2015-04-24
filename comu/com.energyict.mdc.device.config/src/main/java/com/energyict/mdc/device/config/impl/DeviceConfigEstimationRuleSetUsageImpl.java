package com.energyict.mdc.device.config.impl;

import java.time.Instant;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceConfigEstimationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;

public class DeviceConfigEstimationRuleSetUsageImpl implements DeviceConfigEstimationRuleSetUsage {
    
    enum Fields {
        DEVICECONFIGURATION("deviceConfiguration"),
        ESTIMATIONRULESET("estimationRuleSet"),
        POSITION("position")
        ;
        
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();

    @SuppressWarnings("unused")
    private int position;
    // audit columns
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    DeviceConfigEstimationRuleSetUsage init(DeviceConfiguration deviceConfiguration, EstimationRuleSet estimationRuleSet) {
        this.deviceConfiguration.set(deviceConfiguration);
        this.estimationRuleSet.set(estimationRuleSet);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfiguration.orNull();
    }

    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.orNull();
    }
}
