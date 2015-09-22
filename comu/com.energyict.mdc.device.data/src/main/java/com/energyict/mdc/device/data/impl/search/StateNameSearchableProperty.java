package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
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

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public static final String VIRTUAL_FIELD_NAME = "device.state.name";

    private DeviceSearchDomain domain;
    private SearchableProperty parent;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private List<DeviceState> states = Collections.emptyList();

    static class DeviceState implements HasId, HasName {
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
    }

    @Inject
    public StateNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
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
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_STATUS).format();
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof DeviceState;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((DeviceState)value).getName();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.referencePropertySpec(
                VIRTUAL_FIELD_NAME,
                false,
                FactoryIds.FINITE_STATE,
                this.states);
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
        }
        else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }

    private void refreshWithConstrictionValues(List<Object> deviceTypes) {
        this.validateAllParentsAreDeviceTypes(deviceTypes);
        DisplayStrategy displayStrategy;
        if (deviceTypes.size() > 1) {
            displayStrategy = DisplayStrategy.WITH_LIFE_CYCLE;
        }
        else {
            displayStrategy = DisplayStrategy.NAME_ONLY;
        }
        this.states = deviceTypes.stream()
                .map(DeviceType.class::cast)
                .flatMap(deviceType1 -> this.getDeviceStatesFor(deviceType1, displayStrategy))
                .sorted((state1, state2) -> state1.getName().compareToIgnoreCase(state2.getName()))
                .collect(Collectors.toList());
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

    private Stream<DeviceState> getDeviceStatesFor(DeviceType deviceType, DisplayStrategy displayStrategy) {
        return deviceType
                .getDeviceLifeCycle()
                .getFiniteStateMachine()
                .getStates()
                .stream()
                .map(s -> displayStrategy.toDisplay(s, deviceType, this.thesaurus));
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

    private enum DisplayStrategy {
        NAME_ONLY {
            @Override
            public DeviceState toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus) {
                return new DeviceState(state.getId(), getStateName(state, thesaurus)) ;
            }
        },

        WITH_LIFE_CYCLE {
            @Override
            public DeviceState toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus) {
                return new DeviceState(state.getId(), getStateName(state, thesaurus) + "(" + deviceType.getDeviceLifeCycle().getName() + ")");
            }
        };

        protected String getStateName(State state, Thesaurus thesaurus) {
            Optional<DefaultState> defaultState = DefaultState.from(state);
            if (defaultState.isPresent()) {
                return thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getKey());
            } else {
                return state.getName();
            }
        }

        public abstract DeviceState toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus);
    }

}