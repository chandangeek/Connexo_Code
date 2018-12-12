/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Exposes the name of the current {@link State}
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (15:52)
 */
public class StateNameSearchableProperty extends AbstractSearchableDeviceProperty {

    /**
     * The name of the "virtual" field that holds the name
     * of the current {@link com.elster.jupiter.fsm.State}
     * of a {@link Device}.
     */
    private static final String VIRTUAL_FIELD_NAME = "device.state.name";
    private final PropertySpecService propertySpecService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private DeviceSearchDomain domain;
    private SearchableProperty parent;
    private DeviceState[] states = new DeviceState[0];

    @Inject
    public StateNameSearchableProperty(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    StateNameSearchableProperty init(DeviceSearchDomain domain, DeviceTypeSearchableProperty parent) {
        this.domain = domain;
        this.parent = parent;
        return this;
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
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_STATUS;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof DeviceState;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((DeviceState) value).getName();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new DeviceStateValueFactory())
                .named(VIRTUAL_FIELD_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(this.states)
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.parent);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // We have at most one constraint
        if (constrictions.size() != 1) {
            throw new IllegalArgumentException("Expecting at most 1 constriction, i.e. the constraint on the device type");
        }
        this.refreshWithConstrictions(constrictions.get(0));
    }

    private void refreshWithConstrictions(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            this.refreshWithConstrictionValues(constriction.getConstrainingValues());
        } else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }

    private void refreshWithConstrictionValues(List<Object> deviceTypes) {
        this.validateAllParentsAreDeviceTypes(deviceTypes);
        this.states = deviceTypes.stream()
                .map(DeviceType.class::cast)
                .flatMap(this::getDeviceStatesFor)
                .sorted((state1, state2) -> state1.getName().compareToIgnoreCase(state2.getName()))
                .toArray(DeviceState[]::new);
    }

    private void validateAllParentsAreDeviceTypes(List<Object> list) {
        Optional<Object> anyDeviceType =
                list.stream()
                        .filter(Predicates.not(DeviceType.class::isInstance))
                        .findAny();
        if (anyDeviceType.isPresent()) {
            throw new IllegalArgumentException("Parents are expected to be of type " + DeviceType.class.getName());
        }
    }

    private Stream<DeviceState> getDeviceStatesFor(DeviceType deviceType) {
        return deviceType
                .getDeviceLifeCycle()
                .getFiniteStateMachine()
                .getStates()
                .stream()
                .map(s -> new DeviceState(s.getId(), getStateName(s, deviceLifeCycleConfigurationService) + " (" + deviceType.getDeviceLifeCycle().getName() + ")"));
    }

    private String getStateName(State state, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder
                .addEndDevice()
                .addEndDeviceStatus()
                .addFiniteState();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.spaceOpenBracket();
        sqlBuilder.openBracket();
        sqlBuilder.append(MessageFormat.format(Operator.LESSTHANOREQUAL.getFormat(), "eds.starttime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.GREATERTHAN.getFormat(), "eds.endtime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment("fs.id", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    static final class DeviceState implements HasId, HasName {
        private long id;
        private String name;

        DeviceState(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DeviceState that = (DeviceState) o;

            return id == that.id;

        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private class DeviceStateValueFactory extends SearchHelperValueFactory<DeviceState> {
        private DeviceStateValueFactory() {
            super(DeviceState.class);
        }

        @Override
        public DeviceState fromStringValue(String stringValue) {
            return Arrays.stream(states)
                    .filter(state -> String.valueOf(state.getId()).equals(stringValue))
                    .findAny()
                    .orElse(null);
        }

        @Override
        public String toStringValue(DeviceState object) {
            return String.valueOf(object.getId());
        }
    }
}
