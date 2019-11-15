/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ConfigPropertiesService;
import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.metering.ConfigPropertiesService",
        service = {ConfigPropertiesService.class}, immediate = true,
        property = "name=" + MeteringDataModelService.COMPONENT_NAME)
public class ConfigPropertiesServiceImpl implements ConfigPropertiesService {
    private final List<ConfigPropertiesProvider> configProperties = Collections.synchronizedList(new ArrayList<ConfigPropertiesProvider>());

    // For OSGi purposes
    @Inject
    public ConfigPropertiesServiceImpl() {
    }

    @Override
    public Optional<ConfigPropertiesProvider> findConfigFroperties(String scope) {
        return this.configProperties.stream()
                .filter(cp -> cp.getScope().equals(scope))
                .findFirst();
    }

    @Override
    public Optional<String> getPropertyValue(String scope, String name) {
        return this.configProperties.stream()
                .filter(cp -> cp.getScope().equals(scope))
                .findFirst()
                .map(cp -> cp.getPropertyStringValues().get(name));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConfigPropertiesProvider(ConfigPropertiesProvider configProperties) {
        this.configProperties.add(configProperties);
    }

    public void removeConfigPropertiesProvider(ConfigPropertiesProvider configProperties) {
        this.configProperties.remove(configProperties);
    }
}