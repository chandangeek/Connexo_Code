package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import java.util.List;

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
        List<PropertySpec> uniquePropertySpecsForMicroActions = transition.getMicroActionsProperties();
        info.properties = this.propertyValueInfoService.getPropertyInfos(uniquePropertySpecsForMicroActions);
        return info;
    }
}
