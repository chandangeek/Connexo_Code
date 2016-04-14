package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.impl.aggregation.*;

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

public class DeliverableNodesFromExpressionNode implements ExpressionNode.Visitor<List<ReadingTypeDeliverableNode>> {

    @Override
    public List<ReadingTypeDeliverableNode> visitConstant(ConstantNode constant) {
        return new ArrayList<ReadingTypeDeliverableNode>();
    }

    @Override
    public List<ReadingTypeDeliverableNode> visitRequirement(ReadingTypeRequirementNode requirement) {
        return new ArrayList<ReadingTypeDeliverableNode>();
    }

    @Override
    public List<ReadingTypeDeliverableNode> visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return Collections.singletonList(deliverable);
    }

    @Override
    public List<ReadingTypeDeliverableNode> visitOperation(OperationNode operationNode) {
        return getDeliverableNodesFromChildren(operationNode);
    }

    @Override
    public List<ReadingTypeDeliverableNode> visitFunctionCall(FunctionCallNode functionCall) {
        return getDeliverableNodesFromChildren(functionCall);
    }

    private List<ReadingTypeDeliverableNode> getDeliverableNodesFromChildren(ExpressionNode node) {
        List<ReadingTypeDeliverableNode> result = new ArrayList<ReadingTypeDeliverableNode>();
        for (ExpressionNode child : node.getChildren()) {
            result.addAll(child.accept(new DeliverableNodesFromExpressionNode()));
        }
        return result;
    }


}
