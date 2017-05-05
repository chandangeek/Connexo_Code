/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ChannelEstimationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ValidationEstimationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class UsagePointEstimationImpl implements UsagePointEstimation {

    private final DataModel dataModel;

    private UsagePoint usagePoint;

    @Inject
    UsagePointEstimationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    UsagePointEstimationImpl init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public List<ChannelEstimationRuleOverriddenPropertiesImpl> findAllOverriddenProperties() {
        return mapper().find(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.USAGEPOINT.fieldName(), this.usagePoint);
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
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.USAGEPOINT.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.READINGTYPE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_NAME.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_IMPL.fieldName()
        };
        Object[] values = {
                this.usagePoint,
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
    public UsagePointEstimation.PropertyOverrider overridePropertiesFor(EstimationRule estimationRule, ReadingType readingType) {
        return new PropertyOverriderImpl(estimationRule, readingType);
    }

    class PropertyOverriderImpl implements UsagePointEstimation.PropertyOverrider {

        private final EstimationRule estimationRule;
        private final ReadingType readingType;

        private final Map<String, Object> properties = new HashMap<>();

        PropertyOverriderImpl(EstimationRule estimationRule, ReadingType readingType) {
            this.estimationRule = estimationRule;
            this.readingType = readingType;
        }

        @Override
        public UsagePointEstimation.PropertyOverrider override(String propertyName, Object propertyValue) {
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
                    .init(usagePoint, readingType, estimationRule.getName(), estimationRule.getImplementation());
        }
    }
}
