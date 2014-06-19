package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.google.common.base.Optional;

import javax.inject.Inject;

public class DeviceConfValidationRuleSetUsageImpl implements DeviceConfValidationRuleSetUsage {

    private long validationRuleSetId;
    private long deviceConfigurationId;

    private transient ValidationRuleSet validationRuleSet;
    private transient DeviceConfiguration deviceConfiguration;

    private final DataModel dataModel;
    private final EventService eventService;
    private final ValidationService validationService;

    @Inject
    DeviceConfValidationRuleSetUsageImpl(DataModel dataModel, EventService eventService, ValidationService validationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
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

    void setValidationRuleSetIdRuleSetId(long validationRuleSetId) {
        this.validationRuleSetId = validationRuleSetId;
    }

    void setDeviceConfigurationId(long deviceConfigurationId) {
        this.deviceConfigurationId = deviceConfigurationId;
    }


}
