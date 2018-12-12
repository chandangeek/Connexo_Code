/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.properties.PropertySpec;

import java.util.HashMap;
import java.util.Map;


public class CustomPropertiesUsageChecker implements ExpressionNode.Visitor<Void> {

    private Map<RegisteredCustomPropertySet, PropertySpec> customPropertiesUsages = new HashMap<>();

    @Override
    public Void visitConstant(ConstantNode constant) {
        return null;
    }

    @Override
    public Void visitRequirement(ReadingTypeRequirementNode requirement) {
        return null;
    }

    @Override
    public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        customPropertiesUsages.put(property.getRegisteredCustomPropertySet(), property.getPropertySpec());
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        operationNode.getChildren().forEach(n -> n.accept(this));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    public Map<RegisteredCustomPropertySet, PropertySpec> getCustomPropertiesUsages() {
        return this.customPropertiesUsages;
    }
}
