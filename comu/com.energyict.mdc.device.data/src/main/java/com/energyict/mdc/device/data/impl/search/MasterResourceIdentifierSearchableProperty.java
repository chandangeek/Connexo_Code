package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
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
 * Exposes the master resource identifier (mRID)
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:41)
 */
public class MasterResourceIdentifierSearchableProperty extends AbstractSearchableDeviceProperty {

    private final DeviceSearchDomain domain;
    private final PropertySpecService propertySpecService;

    public MasterResourceIdentifierSearchableProperty(DeviceSearchDomain domain, PropertySpecService propertySpecService) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
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
        return SelectionMode.SINGLE;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.basicPropertySpec(
                    DeviceFields.MRID.fieldName(),
                    false,
                    new StringFactory());
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
    public void appendJoinClauses(JoinClauseBuilder builder) {
        // No join clauses required
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("dev.mRID", condition, now);
    }

}