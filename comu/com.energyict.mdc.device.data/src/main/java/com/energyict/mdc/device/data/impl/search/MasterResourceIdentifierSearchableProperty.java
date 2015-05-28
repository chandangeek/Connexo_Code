package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;

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
    public PropertySpec getSpecification() {
        return this.propertySpecService.basicPropertySpec(
                    DeviceFields.MRID.fieldName(),
                    false,
                    new StringFactory());
    }

    @Override
    public Optional<SearchableProperty> getParent() {
        return Optional.empty();
    }

    @Override
    public void refreshWithParents(List<Object> list) {
        // Nothing to refresh
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