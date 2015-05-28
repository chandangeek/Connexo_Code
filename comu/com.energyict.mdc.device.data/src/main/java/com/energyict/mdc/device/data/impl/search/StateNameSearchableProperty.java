package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.streams.Predicates;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final DeviceSearchDomain domain;
    private final SearchableProperty parent;
    private final PropertySpecService propertySpecService;
    private String[] stateNames = new String[0];

    public StateNameSearchableProperty(DeviceSearchDomain domain, SearchableProperty parent, PropertySpecService propertySpecService) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.parent = parent;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.stringPropertySpecWithValues(
                VIRTUAL_FIELD_NAME,
                false,
                this.stateNames);
    }

    @Override
    public Optional<SearchableProperty> getParent() {
        return Optional.of(this.parent);
    }

    @Override
    public void refreshWithParents(List<Object> list) {
        this.validateAllParentsAreDeviceTypes(list);
        Set<String> stateNames =
            list.stream()
                .map(DeviceType.class::cast)
                .map(DeviceType::getDeviceLifeCycle)
                .map(DeviceLifeCycle::getFiniteStateMachine)
                .flatMap(fsm -> fsm.getStates().stream())
                .map(State::getName)
                .collect(Collectors.toSet());
        this.stateNames = stateNames.toArray(new String[stateNames.size()]);
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
    public Condition toCondition(Condition specification) {
        specification.visit(this);
        return this.constructed();
    }

    @Override
    public void visitComparison(Comparison comparison) {
        // mRID is simple enough such that the specification can also serve as actual condition
        this.and(comparison);
    }

}