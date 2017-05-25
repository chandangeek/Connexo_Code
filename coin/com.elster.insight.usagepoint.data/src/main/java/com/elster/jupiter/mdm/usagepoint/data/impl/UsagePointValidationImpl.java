/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelValidationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ChannelValidationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ValidationEstimationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRule;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class UsagePointValidationImpl implements UsagePointValidation {

    private final DataModel dataModel;

    private UsagePoint usagePoint;

    @Inject
    UsagePointValidationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    UsagePointValidationImpl init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public List<ChannelValidationRuleOverriddenPropertiesImpl> findAllOverriddenProperties() {
        return mapper().find(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.USAGEPOINT.fieldName(), this.usagePoint);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findAndLockChannelValidationRuleOverriddenProperties(long id, long version) {
        return mapper().lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findChannelValidationRuleOverriddenProperties(long id) {
        return mapper().getOptional(id);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findOverriddenProperties(ValidationRule validationRule, ReadingType readingType) {
        String[] fieldNames = {
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.USAGEPOINT.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.READINGTYPE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_NAME.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_IMPL.fieldName(),
                ChannelValidationRuleOverriddenPropertiesImpl.Fields.VALIDATION_ACTION.fieldName()
        };
        Object[] values = {
                this.usagePoint,
                readingType,
                validationRule.getName(),
                validationRule.getImplementation(),
                validationRule.getAction()
        };
        return mapper().getUnique(fieldNames, values);
    }

    private DataMapper<ChannelValidationRuleOverriddenPropertiesImpl> mapper() {
        return dataModel.mapper(ChannelValidationRuleOverriddenPropertiesImpl.class);
    }

    @Override
    public PropertyOverrider overridePropertiesFor(ValidationRule validationRule, ReadingType readingType) {
        return new PropertyOverriderImpl(validationRule, readingType);
    }

    class PropertyOverriderImpl implements PropertyOverrider {

        private final ValidationRule validationRule;
        private final ReadingType readingType;

        private final Map<String, Object> properties = new HashMap<>();

        PropertyOverriderImpl(ValidationRule validationRule, ReadingType readingType) {
            this.validationRule = validationRule;
            this.readingType = readingType;
        }

        @Override
        public PropertyOverrider override(String propertyName, Object propertyValue) {
            properties.put(propertyName, propertyValue);
            return this;
        }

        @Override
        public ChannelValidationRuleOverriddenProperties complete() {
            ChannelValidationRuleOverriddenPropertiesImpl overriddenProperties = createChannelValidationRuleOverriddenProperties();
            overriddenProperties.setProperties(this.properties);
            overriddenProperties.validate();
            if (this.properties.isEmpty()) {
                return null;// we don't want to persist entity without overridden properties
            }
            overriddenProperties.save();
            return overriddenProperties;
        }

        private ChannelValidationRuleOverriddenPropertiesImpl createChannelValidationRuleOverriddenProperties() {
            return dataModel.getInstance(ChannelValidationRuleOverriddenPropertiesImpl.class)
                    .init(usagePoint, readingType, validationRule.getName(), validationRule.getImplementation(), validationRule.getAction());
        }
    }
}
