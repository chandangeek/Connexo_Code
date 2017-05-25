/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.estimation.CachedEstimationPropertyProvider;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationPropertyProvider;
import com.elster.jupiter.estimation.EstimationPropertyResolver;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.mdm.usagepoint.data.impl.properties.UsagePointEstimationPropertyResolver",
        service = EstimationPropertyResolver.class,
        immediate = true
)
@SuppressWarnings("unused")
public class UsagePointEstimationPropertyResolver implements EstimationPropertyResolver {

    private volatile UsagePointService usagePointService;

    public UsagePointEstimationPropertyResolver() {
    }

    public UsagePointEstimationPropertyResolver(UsagePointService usagePointService) {
        this();
        this.setUsagePointService(usagePointService);
    }

    @Override
    public Optional<EstimationPropertyProvider> resolve(ChannelsContainer channelsContainer) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            MetrologyContractChannelsContainer container = (MetrologyContractChannelsContainer) channelsContainer;
            Set<ReadingType> readingTypesOfInterest = container.getReadingTypes(Range.all());
            UsagePointEstimation usagePointEstimation = usagePointService.forEstimation(container.getUsagePoint().get());
            CachedEstimationPropertyProvider propertyProvider = new CachedEstimationPropertyProvider();
            usagePointEstimation.findAllOverriddenProperties().stream()
                    .filter(properties -> readingTypesOfInterest.contains(properties.getReadingType()))
                    .forEach(properties -> this.putPropertiesInto(properties, propertyProvider));
            return Optional.of(propertyProvider);
        }
        return Optional.empty();
    }

    private void putPropertiesInto(ChannelEstimationRuleOverriddenProperties properties, CachedEstimationPropertyProvider estimationPropertyProvider) {
        estimationPropertyProvider.setProperties(
                properties.getReadingType(),
                properties.getEstimationRuleName(),
                properties.getEstimatorImpl(),
                properties.getProperties()
        );
    }

    @Override
    public EstimationPropertyDefinitionLevel getLevel() {
        return EstimationPropertyDefinitionLevel.TARGET_OBJECT;
    }

    @Reference
    public void setUsagePointService(UsagePointService usagePointService) {
        this.usagePointService = usagePointService;
    }
}
