package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;

import com.elster.jupiter.nls.Thesaurus;
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
 * Exposes the serial number of a {@link Device} as a {@link SearchableProperty}.
 * @see Device#getSerialNumber()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (17:04)
 */
public class SerialNumberSearchableProperty extends AbstractSearchableDeviceProperty {

    private final DeviceSearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    public SerialNumberSearchableProperty(DeviceSearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
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
    public String getDisplayName() {
        return PropertyTranslationKeys.DEVICE_SERIAL_NUMBER.getDisplayName(this.thesaurus);
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
        return this.propertySpecService.basicPropertySpec(
                    DeviceFields.SERIALNUMBER.fieldName(),
                    false,
                    new StringFactory());
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("No constraint to refresh");
        }
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        // No join clauses required
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("dev.SERIALNUMBER", condition, now);
    }

}