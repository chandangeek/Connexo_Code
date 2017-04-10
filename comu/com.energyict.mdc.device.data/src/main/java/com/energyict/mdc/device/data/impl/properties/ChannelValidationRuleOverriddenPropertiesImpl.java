/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@HasRequiredProperties
public final class ChannelValidationRuleOverriddenPropertiesImpl extends ValidationEstimationRuleOverriddenPropertiesImpl implements ChannelValidationRuleOverriddenProperties {

    static final String TYPE_IDENTIFIER = "DVR";

    public enum Fields {

        VALIDATION_ACTION("validationAction");

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private ValidationAction validationAction;

    private final ValidationService validationService;
    private final Thesaurus thesaurus;

    @Inject
    ChannelValidationRuleOverriddenPropertiesImpl(DataModel dataModel, ValidationService validationService, Thesaurus thesaurus) {
        super(dataModel);
        this.validationService = validationService;
        this.thesaurus = thesaurus;
    }

    public ChannelValidationRuleOverriddenPropertiesImpl init(Device device, ReadingType readingType, String ruleName, String ruleImpl, ValidationAction validationAction) {
        super.init(device, readingType, ruleName, ruleImpl);
        this.validationAction = validationAction;
        return this;
    }

    @Override
    public String getValidationRuleName() {
        return getRuleName();
    }

    @Override
    public String getValidatorImpl() {
        return getRuleImpl();
    }

    @Override
    public ValidationAction getValidationAction() {
        return validationAction;
    }

    List<PropertySpec> getPropertySpecs() {
        return validationService
                .getValidator(this.getRuleImpl())
                .getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT);
    }

    @Override
    PropertySpec getPropertySpec(String propertyName) {
        return getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(propertyName))
                .findAny()
                .orElseThrow(() -> new PropertyCannotBeOverriddenException(thesaurus, MessageSeeds.VALIDATION_RULE_PROPERTY_CANNOT_BE_OVERRIDDEN, propertyName));
    }

    @Override
    void validateProperties(Map<String, Object> properties) {
        validationService.getValidator(this.getRuleImpl()).validateProperties(properties);
    }
}
