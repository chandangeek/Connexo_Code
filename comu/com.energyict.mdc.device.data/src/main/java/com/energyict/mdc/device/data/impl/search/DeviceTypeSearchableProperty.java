package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Exposes the {@link }
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:41)
 */
public class DeviceTypeSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = DeviceFields.DEVICETYPE.fieldName();

    private final DeviceSearchDomain domain;
    private final PropertySpecService mdcPropertySpecService;

    public DeviceTypeSearchableProperty(DeviceSearchDomain domain, PropertySpecService mdcPropertySpecService) {
        super();
        this.domain = domain;
        this.mdcPropertySpecService = mdcPropertySpecService;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.mdcPropertySpecService.referencePropertySpec(
                    PROPERTY_NAME,
                    false,
                    FactoryIds.DEVICE_TYPE);
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
        return this.toSqlFragment("devicetype", condition, now);
    }

}