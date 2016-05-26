package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Dimension;

/**
 * Models a {@link ServerExpressionNode} that represents.
 * one {@link PropertySpec property} of a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-26 (13:16)
 */
class CustomPropertyNode implements ServerExpressionNode {
    private final PropertySpec propertySpec;
    private final RegisteredCustomPropertySet customPropertySet;

    CustomPropertyNode(PropertySpec propertySpec, RegisteredCustomPropertySet customPropertySet) {
        this.propertySpec = propertySpec;
        this.customPropertySet = customPropertySet;
    }

    CustomPropertySet getCustomPropertySet() {
        return this.customPropertySet.getCustomPropertySet();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitProperty(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(Dimension.DIMENSIONLESS);
    }

    String sqlName() {
        return "rid_cps_" + this.customPropertySet.getId();
    }

}