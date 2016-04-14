package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.impl.aggregation.*;
import com.elster.jupiter.metering.impl.config.AbstractNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * and returns all deliverables contained in the visited
 * {@link ExpressionNode}
 * or <code>0</code> if the ExpressionNode cannot provide process status flags.
 * A {@link NumericalConstantNode} is a good example of that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */

public class RequirementsFromExpressionNode implements ExpressionNode.Visitor<List<ReadingTypeRequirement>> {

    @Override
    public List<ReadingTypeRequirement> visitConstant(ConstantNode constant) {
        return new ArrayList<ReadingTypeRequirement>();
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

    private List<ReadingTypeRequirement> getRequirementsFromChildren(ExpressionNode node) {
        List<ReadingTypeRequirement> result = new ArrayList<ReadingTypeRequirement>();
        for (ExpressionNode child : node.getChildren()) {
            result.addAll(child.accept(new RequirementsFromExpressionNode()));
        }
        return result;
    }



}
