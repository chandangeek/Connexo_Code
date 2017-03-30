/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

class DeviceConfValidationRuleSetUsageImpl implements DeviceConfValidationRuleSetUsage {

    private long validationRuleSetId;
    private long deviceConfigurationId;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private transient ValidationRuleSet validationRuleSet;
    private transient DeviceConfiguration deviceConfiguration;

    private final DataModel dataModel;
    private final ValidationService validationService;

    @Inject
    DeviceConfValidationRuleSetUsageImpl(DataModel dataModel, ValidationService validationService) {
        this.dataModel = dataModel;
        this.validationService = validationService;
    }

    DeviceConfValidationRuleSetUsageImpl init(ValidationRuleSet validationRuleSet, DeviceConfiguration deviceConfiguration) {
        this.validationRuleSet = validationRuleSet;
        this.deviceConfiguration = deviceConfiguration;
        this.validationRuleSetId = validationRuleSet.getId();
        this.deviceConfigurationId = deviceConfiguration.getId();
        return this;
    }

    @Override
    public ValidationRuleSet getValidationRuleSet() {
        if (validationRuleSet == null) {
            Optional optional = validationService.getValidationRuleSet(validationRuleSetId);
            if (optional.isPresent()) {
                validationRuleSet = (ValidationRuleSet) optional.get();
            }
        }
        return validationRuleSet;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        if (deviceConfiguration == null) {
            deviceConfiguration = dataModel.mapper(DeviceConfiguration.class).getOptional(deviceConfigurationId).get();
        }
        return deviceConfiguration;
    }

    @Override
    public void save() {
        dataModel.persist(this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    void setDeviceConfigurationId(long deviceConfigurationId) {
        this.deviceConfigurationId = deviceConfigurationId;
    }

}