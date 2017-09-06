/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.impl.search.NameSearchableProperty;
import com.elster.jupiter.metering.impl.search.ServiceCategorySearchableProperty;
import com.elster.jupiter.metering.impl.search.UsagePointStateSearchableProperty;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Builder} that creates a {@link UsagePointGroup} for the given usage point properties
 */
public class UsagePointGroupBuilder extends NamedBuilder<UsagePointGroup, UsagePointGroupBuilder> {
    private static final String PROPERTY_USAGE_POINT_NAME = "name";
    private static final String PROPERTY_SERVICE_CATEGORY = "SERVICEKIND";
    private static final String PROPERTY_STATE = "state.state";

    private final MeteringGroupsService meteringGroupsService;
    private final SearchService searchService;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    private String namePrefix;
    private Set<ServiceKind> serviceKinds;
    private Set<String> stateNames;
    private List<String> searchablePropertyNames = new ArrayList<>();

    @Inject
    public UsagePointGroupBuilder(MeteringGroupsService meteringGroupsService, SearchService searchService,
                                  UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        super(UsagePointGroupBuilder.class);
        this.meteringGroupsService = meteringGroupsService;
        this.searchService = searchService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    public UsagePointGroupBuilder withNamePrefix(String namePrefix) {
        if (namePrefix == null) {
            searchablePropertyNames.remove(PROPERTY_USAGE_POINT_NAME);
        } else {
            searchablePropertyNames.add(PROPERTY_USAGE_POINT_NAME);
        }
        this.namePrefix = namePrefix;
        return this;
    }

    public UsagePointGroupBuilder ofServiceKinds(Set<ServiceKind> serviceKinds) {
        if (!serviceKinds.isEmpty()) {
            searchablePropertyNames.add(PROPERTY_SERVICE_CATEGORY);
        }
        this.serviceKinds = serviceKinds;
        return this;
    }

    public UsagePointGroupBuilder inStates(Set<String> stateNames) {
        if (!stateNames.isEmpty()) {
            searchablePropertyNames.add(PROPERTY_STATE);
        }
        this.stateNames = stateNames;
        return this;
    }

    @Override
    public Optional<UsagePointGroup> find() {
        return meteringGroupsService.findUsagePointGroupByName(getName());
    }

    @Override
    public UsagePointGroup create() {
        Log.write(this);
        UsagePointGroup usagePointGroup = meteringGroupsService.createQueryUsagePointGroup(getSearchablePropertyValues())
                .setName(getName())
                .setSearchDomain(findUsagePointSearchDomain())
                .setMRID("MDM:" + getName())
                .setQueryProviderName("com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider")
                .create();
        applyPostBuilders(usagePointGroup);
        return usagePointGroup;
    }

    private SearchDomain findUsagePointSearchDomain() {
        return searchService.findDomain(UsagePoint.class.getName()).orElseThrow(() -> new UnableToCreate("Unable to find usage point search domain"));
    }

    private SearchablePropertyValue[] getSearchablePropertyValues() {
        List<SearchablePropertyValue> values = new ArrayList<>(searchablePropertyNames.size());
        if (searchablePropertyNames.contains(PROPERTY_USAGE_POINT_NAME)) {
            values.add(createSearchablePropertyValue(PROPERTY_USAGE_POINT_NAME, Collections.singletonList(namePrefix)));
        }
        if (searchablePropertyNames.contains(PROPERTY_SERVICE_CATEGORY)) {
            values.add(createSearchablePropertyValue(PROPERTY_SERVICE_CATEGORY, serviceKinds.stream()
                    .map(ServiceKind::name)
                    .collect(Collectors.toList())));
        }
        if (searchablePropertyNames.contains(PROPERTY_STATE)) {
            List<State> states = usagePointLifeCycleConfigurationService.getUsagePointStates();
            values.add(createSearchablePropertyValue(PROPERTY_STATE, states.stream()
                    .filter(state -> stateNames.contains(state.getName()))
                    .map(State::getId)
                    .map(String::valueOf)
                    .collect(Collectors.toList())));
        }
        return values.toArray(new SearchablePropertyValue[searchablePropertyNames.size()]);
    }

    private static SearchablePropertyValue createSearchablePropertyValue(String searchableProperty, List<String> values) {
        return new SearchablePropertyValue(null, new SearchablePropertyValue.ValueBean(searchableProperty, SearchablePropertyOperator.EQUAL, values));
    }
}
