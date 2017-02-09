/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Dimension;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertyNode} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (12:32)
 */
public class CustomPropertyNodeImpl extends AbstractNode implements CustomPropertyNode {
    static final String TYPE_IDENTIFIER = "CPS";

    @Size(min = 0, max = Table.NAME_LENGTH, message = MessageSeeds.Constants.REQUIRED)
    private String propertySpecName;
    private PropertySpec propertySpec;
    @IsPresent(message = MessageSeeds.Constants.REQUIRED)
    private Reference<RegisteredCustomPropertySet> customPropertySet = ValueReference.absent();

    // For ORM layer
    @Inject
    public CustomPropertyNodeImpl() {
        super();
    }

    public CustomPropertyNodeImpl(PropertySpec propertySpec, RegisteredCustomPropertySet customPropertySet) {
        this();
        this.propertySpecName = propertySpec.getName();
        this.propertySpec = propertySpec;
        this.customPropertySet.set(customPropertySet);
    }

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return this.customPropertySet.orNull();
    }

    @Override
    public CustomPropertySet getCustomPropertySet() {
        return this.customPropertySet.map(RegisteredCustomPropertySet::getCustomPropertySet).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PropertySpec getPropertySpec() {
        if (this.propertySpec == null) {
            List<PropertySpec> propertySpecs = this.getCustomPropertySet().getPropertySpecs();
            this.propertySpec = propertySpecs.stream().filter(each -> each.getName().equals(this.propertySpecName)).findFirst().get();
        }
        return this.propertySpec;
    }

    @Override
    public Dimension getDimension() {
        return Dimension.DIMENSIONLESS;
    }

    @Override
    public void validate() {
        // No validation for custom properties
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitProperty(this);
    }

    @Override
    public String toString() {
        return "property(" + this.customPropertySet.map(RegisteredCustomPropertySet::getCustomPropertySet)
                .map(CustomPropertySet::getName)
                .orElse("") + ", " + this.getPropertySpec().getDisplayName() + ")";
    }

}