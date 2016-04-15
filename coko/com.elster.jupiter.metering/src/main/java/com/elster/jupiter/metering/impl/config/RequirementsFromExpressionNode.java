package com.elster.jupiter.metering.impl.config;


import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * and returns all deliverables contained in the visited
 * {@link ExpressionNode}
 * or <code>0</code> if the ExpressionNode cannot provide process status flags.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */

public class RequirementsFromExpressionNode implements ExpressionNode.Visitor<List<ReadingTypeRequirement>> {

    @Override
    public List<ReadingTypeRequirement> visitConstant(ConstantNode constant) {
        return new ArrayList<>();
    }

    @Override
    public List<ReadingTypeRequirement> visitRequirement(ReadingTypeRequirementNode requirement) {
        return Collections.singletonList(requirement.getReadingTypeRequirement());
    }

    @Override
    public List<ReadingTypeRequirement> visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return deliverable.getReadingTypeDeliverable().getFormula().getExpressionNode().accept(new RequirementsFromExpressionNode());
    }

    @Override
    public List<ReadingTypeRequirement> visitOperation(OperationNode operationNode) {
        return getRequirementsFromChildren(operationNode);
    }

    @Override
    public List<ReadingTypeRequirement> visitFunctionCall(FunctionCallNode functionCall) {
        return getRequirementsFromChildren(functionCall);
    }

    @Override
    public List<ReadingTypeRequirement> visitNull(NullNode nullNode) {
        return new ArrayList<>();
    }

    private List<ReadingTypeRequirement> getRequirementsFromChildren(ExpressionNode node) {
        return node.getChildren()
                .stream()
                .map(child -> child.accept(new RequirementsFromExpressionNode()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
