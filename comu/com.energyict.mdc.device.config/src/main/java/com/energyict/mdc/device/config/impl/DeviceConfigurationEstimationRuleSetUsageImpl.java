/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceConfigurationEstimationRuleSetUsage;

import java.time.Instant;

class DeviceConfigurationEstimationRuleSetUsageImpl implements DeviceConfigurationEstimationRuleSetUsage {

    enum Fields {
        DEVICECONFIGURATION("deviceConfiguration"),
        ESTIMATIONRULESET("estimationRuleSet"),
        POSITION("position"),
        IS_RULE_SET_ACTIVE("isRuleSetActive")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @IsPresent
    private Reference<EstimationRuleSet> estimationRuleSet = ValueReference.absent();

    private boolean isRuleSetActive;

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

    DeviceConfigurationEstimationRuleSetUsage init(DeviceConfiguration deviceConfiguration, EstimationRuleSet estimationRuleSet, boolean isRuleSetActive) {
        this.isRuleSetActive = isRuleSetActive;
        this.deviceConfiguration.set(deviceConfiguration);
        this.estimationRuleSet.set(estimationRuleSet);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfiguration.get();
    }

    @Override
    public EstimationRuleSet getEstimationRuleSet() {
        return estimationRuleSet.get();
    }

    @Override
    public boolean isRuleSetActive() {
        return isRuleSetActive;
    }

    @Override
    public void setRuleSetStatus(boolean active) {
        this.isRuleSetActive = active;
    }
}
