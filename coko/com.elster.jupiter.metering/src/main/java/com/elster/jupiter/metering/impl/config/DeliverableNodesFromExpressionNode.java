package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
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

public class DeliverableNodesFromExpressionNode implements ExpressionNode.Visitor<List<ReadingTypeDeliverableNode>> {

    @Override
    public List<ReadingTypeDeliverableNode> visitConstant(ConstantNode constant) {
        return new ArrayList<>();
    }

    @Override
    public List<ReadingTypeDeliverableNode> visitRequirement(ReadingTypeRequirementNode requirement) {
        return new ArrayList<>();
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

    @Override
    public List<ReadingTypeDeliverableNode> visitNull(NullNode nullNode) {
        return new ArrayList<>();
    }

    private List<ReadingTypeDeliverableNode> getDeliverableNodesFromChildren(ExpressionNode node) {
        return node.getChildren()
                .stream()
                .map(child -> child.accept(new DeliverableNodesFromExpressionNode()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}