/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.mdm.usagepoint.data.ChannelValidationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.properties.CachedValidationPropertyProvider;
import com.elster.jupiter.validation.properties.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.properties.ValidationPropertyProvider;
import com.elster.jupiter.validation.properties.ValidationPropertyResolver;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.mdm.usagepoint.data.impl.UsagePointValidationPropertyResolver",
        service = ValidationPropertyResolver.class,
        immediate = true
)
@SuppressWarnings("unused")
public class UsagePointValidationPropertyResolver implements ValidationPropertyResolver {

    private volatile UsagePointDataModelService usagePointDataModelService;

    public UsagePointValidationPropertyResolver() {
    }

    public UsagePointValidationPropertyResolver(UsagePointDataModelService usagePointDataModelService) {
        this();
        setUsagePointDataModelService(usagePointDataModelService);
    }

    @Override
    public Optional<ValidationPropertyProvider> resolve(ChannelsContainer channelsContainer) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            MetrologyContractChannelsContainer container = (MetrologyContractChannelsContainer) channelsContainer;
            Set<ReadingType> readingTypesOfInterest = container.getReadingTypes(Range.all());
            UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(container.getUsagePoint().get());
            CachedValidationPropertyProvider propertyProvider = new CachedValidationPropertyProvider();
            usagePointValidation.findAllOverriddenProperties().stream()
                    .filter(properties -> readingTypesOfInterest.contains(properties.getReadingType()))
                    .forEach(properties -> this.putPropertiesInto(properties, propertyProvider));
            return Optional.of(propertyProvider);
        }
        return Optional.empty();
    }

    private void putPropertiesInto(ChannelValidationRuleOverriddenProperties properties, CachedValidationPropertyProvider validationPropertyProvider) {
        validationPropertyProvider.setProperties(
                properties.getReadingType(),
                properties.getValidationRuleName(),
                properties.getValidatorImpl(),
                properties.getValidationAction(),
                properties.getProperties()
        );
    }

    @Override
    public ValidationPropertyDefinitionLevel getLevel() {
        return ValidationPropertyDefinitionLevel.TARGET_OBJECT;
    }

    @Reference
    public void setUsagePointDataModelService(UsagePointDataModelService usagePointDataModelService) {
        this.usagePointDataModelService = usagePointDataModelService;
    }
}
