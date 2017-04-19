/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@HasRequiredProperties
public class ChannelEstimationRuleOverriddenPropertiesImpl extends ValidationEstimationRuleOverriddenPropertiesImpl implements ChannelEstimationRuleOverriddenProperties {

    static final String TYPE_IDENTIFIER = "DER";

    private final EstimationService estimationService;
    private final Thesaurus thesaurus;

    @Inject
    ChannelEstimationRuleOverriddenPropertiesImpl(DataModel dataModel, EstimationService estimationService, Thesaurus thesaurus) {
        super(dataModel);
        this.estimationService = estimationService;
        this.thesaurus = thesaurus;
    }

    public ChannelEstimationRuleOverriddenPropertiesImpl init(Device device, ReadingType readingType, String ruleName, String ruleImpl) {
        super.init(device, readingType, ruleName, ruleImpl);
        return this;
    }

    @Override
    public String getEstimationRuleName() {
        return getRuleName();
    }

    @Override
    public String getEstimatorImpl() {
        return getRuleImpl();
    }

    @Override
    List<PropertySpec> getPropertySpecs() {
        return estimationService
                .getEstimator(this.getRuleImpl())
                .map(estimator -> estimator.getPropertySpecs(EstimationPropertyDefinitionLevel.TARGET_OBJECT))
                .orElse(Collections.emptyList());
    }

    @Override
    PropertySpec getPropertySpec(String propertyName) {
        return getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(propertyName))
                .findAny()
                .orElseThrow(() -> new PropertyCannotBeOverriddenException(thesaurus, MessageSeeds.ESTIMATION_RULE_PROPERTY_CANNOT_BE_OVERRIDDEN, propertyName));
    }

    @Override
    void validateProperties(Map<String, Object> properties) {
        estimationService.getEstimator(this.getRuleImpl()).get().validateProperties(properties);
    }
}