package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

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
    private String[] stateNames = new String[0];

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
        return PropertyTranslationKeys.DEVICE_STATUS.getDisplayName(this.thesaurus);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof String;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return String.valueOf(value);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.stringPropertySpecWithValues(
                VIRTUAL_FIELD_NAME,
                false,
                this.stateNames);
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

    private void refreshWithConstrictionValues(List<Object> list) {
        this.validateAllParentsAreDeviceTypes(list);
        DisplayStrategy displayStrategy;
        if (list.size() > 1) {
            displayStrategy = DisplayStrategy.WITH_DEVICE_TYPE;
        }
        else {
            displayStrategy = DisplayStrategy.NAME_ONLY;
        }
        Set<String> stateNames = new HashSet<>();
        for (Object o : list) {
            DeviceType deviceType = (DeviceType) o;
            stateNames.addAll(this.stateNamesIn(deviceType, displayStrategy));
        }
        this.stateNames = stateNames.toArray(new String[stateNames.size()]);
    }

    private Set<String> stateNamesIn(DeviceType deviceType, DisplayStrategy displayStrategy) {
        return deviceType
                    .getDeviceLifeCycle()
                    .getFiniteStateMachine()
                    .getStates()
                    .stream()
                    .map(s -> displayStrategy.toDisplay(s, deviceType, this.thesaurus))
                    .collect(Collectors.toSet());
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
        sqlBuilder.add(this.toSqlFragment("fs.name", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    private enum DisplayStrategy {
        NAME_ONLY {
            @Override
            public String toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus) {
                return getStateName(state, thesaurus);
            }
        },

        WITH_DEVICE_TYPE {
            @Override
            public String toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus) {
                return getStateName(state, thesaurus) + "(" + deviceType.getName() + ")";
            }
        };

        protected String getStateName(State state, Thesaurus thesaurus) {
            return thesaurus.getStringBeyondComponent(state.getName(), state.getName());
        }

        public abstract String toDisplay(State state, DeviceType deviceType, Thesaurus thesaurus);
    }

}