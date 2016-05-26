package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;

/**
 * Models an {@link ExpressionNode} that represents
 * one {@link PropertySpec property} of a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (12:05)
 */
public interface CustomPropertyNode extends ExpressionNode {

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();

    CustomPropertySet getCustomPropertySet();

    PropertySpec getPropertySpec();

}