/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class UsagePointStateSearchableProperty implements SearchableUsagePointProperty {
    private static final String FIELD_NAME = "state.state";

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final UsagePointLifeCycleConfigurationService configurationService;

    public UsagePointStateSearchableProperty(SearchDomain domain,
                                             PropertySpecService propertySpecService,
                                             Thesaurus thesaurus,
                                             UsagePointLifeCycleConfigurationService configurationService) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.configurationService = configurationService;
    }

    @Override
    public Condition toCondition(Condition condition) {
        return condition.and(where("state.interval").isEffective());
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return FIELD_NAME;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .referenceSpec(State.class)
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_STATE)
                .fromThesaurus(this.thesaurus)
                .addValues(this.configurationService.getUsagePointStates()
                        .stream()
                        .sorted(Comparator.comparing(State::getName))
                        .collect(Collectors.toList()))
                .markExhaustive()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_STATE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof State) {
            State state = (State) value;
            String stateName = thesaurus.getString(state.getName(), state.getName());
            String stateMachineName = state.getFiniteStateMachine().getName();
            //all usage point lifecycles are prefixed as an fsm. Because it's not possible to get the UP lifecycle here, a small workarround is done
            //using the finite state machine name with the prefix
            //plus just a small check to be absolutely sure
            int index = stateMachineName.indexOf(UsagePointLifeCycleConfigurationService.FSM_NAME_PREFIX);
            if(index == 0) {
                stateMachineName = stateMachineName.substring(UsagePointLifeCycleConfigurationService.FSM_NAME_PREFIX.length());
            }
            String lifecycleName = thesaurus.getString(stateMachineName, stateMachineName);
            return new StringBuilder(stateName).append(" (").append(lifecycleName).append(")").toString();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // do nothing
    }
}
