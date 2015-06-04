package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the properties of a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-03 (14:02)
 */
public class ConnectionTypeSearchableProperty extends AbstractSearchableDeviceProperty {

    private final DeviceSearchDomain domain;
    private final ConnectionTypeSearchablePropertyGroup group;
    private final ConnectionTypePluggableClass pluggableClass;
    private final PropertySpec propertySpec;

    public ConnectionTypeSearchableProperty(DeviceSearchDomain domain, ConnectionTypeSearchablePropertyGroup group, ConnectionTypePluggableClass pluggableClass, PropertySpec propertySpec) {
        super();
        this.domain = domain;
        this.group = group;
        this.pluggableClass = pluggableClass;
        this.propertySpec = propertySpec;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
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
    public PropertySpec getSpecification() {
        return this.propertySpec;
    }

    @Override
    public String getDisplayName() {
        return this.propertySpec.getName();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // Nothing to refresh
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        try {
            this.propertySpec.validateValueIgnoreRequired(value);
            return true;
        }
        catch (InvalidValueException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String toDisplayAfterValidation(Object value) {
        return this.propertySpec.getValueFactory().toStringValue(value);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addConnectionTaskProperties(this.pluggableClass);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("props." + this.propertySpec.getName(), condition, now);
    }

}