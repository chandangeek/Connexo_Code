/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.impl.properties.ChannelEstimationRuleOverriddenPropertiesImpl;
import com.energyict.mdc.device.data.impl.properties.ValidationEstimationRuleOverriddenPropertiesImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

class DeviceEstimationImpl implements DeviceEstimation {

    private ServerDevice device;
    private List<DeviceEstimationRuleSetActivation> estimationRuleSetActivations;
    private boolean active = false;

    private final DataModel dataModel;
    private final EstimationService estimationService;

    @Inject
    DeviceEstimationImpl(DataModel dataModel, EstimationService estimationService) {
        this.dataModel = dataModel;
        this.estimationService = estimationService;
    }

    DeviceEstimation init(ServerDevice device, boolean active, List<DeviceEstimationRuleSetActivation> estimationRuleSetActivations) {
        this.device = device;
        this.active = active;
        this.estimationRuleSetActivations = estimationRuleSetActivations; // do not create a copy of that list, we want the persistent list!
        return this;
    }

    @Override
    public boolean isEstimationActive() {
        return active;
    }

    @Override
    public Device getDevice() {
        return this.device;
    }

    @Override
    public void activateEstimation() {
        if (!active) {
            active = true;
            this.device.activateEstimation();
            saveDevice();
        }
    }

    @Override
    public void deactivateEstimation() {
        if (active) {
            active = false;
            this.device.deactivateEstimation();
            saveDevice();
        }
    }

    @Override
    public List<DeviceEstimationRuleSetActivation> getEstimationRuleSetActivations() {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<EstimationRuleSet> ruleSetsOnDeviceConfig = deviceConfiguration.getEstimationRuleSets();

        List<DeviceEstimationRuleSetActivation> returnList = ruleSetsOnDeviceConfig.stream()
                .map(r -> Pair.of(r, findEstimationRuleSetActivation(r)))
                .map(p -> p.getLast().orElseGet(
                        () -> dataModel.getInstance(DeviceEstimationRuleSetActivationImpl.class).init(this.device, p.getFirst(), deviceConfiguration.isEstimationRuleSetActiveOnDeviceConfig(p.getFirst().getId())))) //not saved intentionally
                .collect(toList());

        List<DeviceEstimationRuleSetActivation> removedFromDeviceConfiguration = estimationRuleSetActivations.stream()
                .filter(ruleSetActivation -> !ruleSetsOnDeviceConfig.contains(ruleSetActivation.getEstimationRuleSet()))
                .collect(toList());
        estimationRuleSetActivations.removeAll(removedFromDeviceConfiguration);
        return returnList;
    }

    @Override
    public void activateEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        applyEstimationRuleSet(estimationRuleSet, true);
    }

    @Override
    public void deactivateEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        applyEstimationRuleSet(estimationRuleSet, false);
    }

    public EstimationService getEstimationService() {
        return estimationService;
    }

    private void applyEstimationRuleSet(EstimationRuleSet estimationRuleSet, boolean active) {
        Optional<DeviceEstimationRuleSetActivation> ruleSetActivation = findEstimationRuleSetActivation(estimationRuleSet);
        if (ruleSetActivation.isPresent()) {
            if (ruleSetActivation.get().isActive() != active) {
                ruleSetActivation.get().setActive(active);
                touchDevice();
            }
        } else {
            estimationRuleSetActivations.add(dataModel.getInstance(DeviceEstimationRuleSetActivationImpl.class).init(this.device, estimationRuleSet, active));
            touchDevice();
        }
    }

    private Optional<DeviceEstimationRuleSetActivation> findEstimationRuleSetActivation(EstimationRuleSet estimationRuleSet) {
        return estimationRuleSetActivations.stream().filter(er -> er.getEstimationRuleSet().getId() == estimationRuleSet.getId()).findAny();
    }

    private void touchDevice() {
        this.device.touch();
    }

    private void saveDevice() {
        this.device.save();
    }

    @Override
    public List<ChannelEstimationRuleOverriddenPropertiesImpl> findAllOverriddenProperties() {
        return mapper().find(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.DEVICE.fieldName(), this.device);
    }

    @Override
    public Optional<ChannelEstimationRuleOverriddenPropertiesImpl> findAndLockChannelEstimationRuleOverriddenProperties(long id, long version) {
        return mapper().lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<ChannelEstimationRuleOverriddenPropertiesImpl> findChannelEstimationRuleOverriddenProperties(long id) {
        return mapper().getOptional(id);
    }

    @Override
    public Optional<ChannelEstimationRuleOverriddenPropertiesImpl> findOverriddenProperties(EstimationRule estimationRule, ReadingType readingType) {
        String[] fieldNames = {
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.DEVICE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.READINGTYPE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_NAME.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_IMPL.fieldName()
        };
        Object[] values = {
                this.device,
                readingType,
                estimationRule.getName(),
                estimationRule.getImplementation()
        };
        return mapper().getUnique(fieldNames, values);
    }

    private DataMapper<ChannelEstimationRuleOverriddenPropertiesImpl> mapper() {
        return dataModel.mapper(ChannelEstimationRuleOverriddenPropertiesImpl.class);
    }

    @Override
    public PropertyOverrider overridePropertiesFor(EstimationRule estimationRule, ReadingType readingType) {
        return new PropertyOverriderImpl(estimationRule, readingType);
    }

    class PropertyOverriderImpl implements PropertyOverrider {

        private final EstimationRule estimationRule;
        private final ReadingType readingType;

        private final Map<String, Object> properties = new HashMap<>();

        PropertyOverriderImpl(EstimationRule estimationRule, ReadingType readingType) {
            this.estimationRule = estimationRule;
            this.readingType = readingType;
        }

        @Override
        public PropertyOverrider override(String propertyName, Object propertyValue) {
            properties.put(propertyName, propertyValue);
            return this;
        }

        @Override
        public ChannelEstimationRuleOverriddenProperties complete() {
            ChannelEstimationRuleOverriddenPropertiesImpl overriddenProperties = createChannelEstimationRuleOverriddenProperties();
            overriddenProperties.setProperties(this.properties);
            overriddenProperties.validate();
            if (this.properties.isEmpty()) {
                return null;// we don't want to persist entity without overridden properties
            }
            overriddenProperties.save();
            return overriddenProperties;
        }

        private ChannelEstimationRuleOverriddenPropertiesImpl createChannelEstimationRuleOverriddenProperties() {
            return dataModel.getInstance(ChannelEstimationRuleOverriddenPropertiesImpl.class)
                    .init(device, readingType, estimationRule.getName(), estimationRule.getImplementation());
        }
    }
}
