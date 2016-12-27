package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfo;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class UsagePointTransitionInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public UsagePointTransitionInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public UsagePointTransitionInfo from(UsagePointTransition transition) {
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.id = transition.getId();
        info.name = transition.getName();
        List<PropertySpec> uniquePropertySpecsForMicroActions =
                DecoratedStream.decorate(transition.getActions().stream())
                        .flatMap(microAction -> microAction.getPropertySpecs().stream())
                        .distinct(PropertySpec::getName)
                        .collect(Collectors.toList());
        info.properties = this.propertyValueInfoService.getPropertyInfos(uniquePropertySpecsForMicroActions);
        return info;
    }
}
