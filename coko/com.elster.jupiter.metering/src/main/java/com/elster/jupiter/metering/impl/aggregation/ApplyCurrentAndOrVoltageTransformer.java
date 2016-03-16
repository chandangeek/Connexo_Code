package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.ExpressionNode;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that applies current and/or voltage transformer configuration according to
 * the rules defined by: <a href="https://confluence.eict.vpdc/display/COPL/Support+for+CT%2C+VT+and+transformer+on+meter+and+usagepoint">confluence</a>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class ApplyCurrentAndOrVoltageTransformer implements ExpressionNode.Visitor<ServerExpressionNode> {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeDeliverableForMeterActivationProvider deliverableProvider;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;

    ApplyCurrentAndOrVoltageTransformer(VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivationProvider deliverableProvider, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.virtualFactory = virtualFactory;
        this.deliverableProvider = deliverableProvider;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
    }

    @Override
    public ServerExpressionNode visitConstant(ConstantNode constant) {
        return new NumericalConstantNode(constant.getValue());
    }

    @Override
    public ServerExpressionNode visitRequirement(ReadingTypeRequirementNode node) {
        // Replace this one with a VirtualRequirementNode
        return new VirtualRequirementNode(
                this.virtualFactory,
                node.getReadingTypeRequirement(),
                this.deliverable,
                this.meterActivation);
    }

    @Override
    public ServerExpressionNode visitDeliverable(ReadingTypeDeliverableNode node) {
        // Replace this one with a VirtualDeliverableNode
        return new VirtualDeliverableNode(
                this.virtualFactory,
                this.deliverableProvider.from(
                        node.getReadingTypeDeliverable(),
                        this.meterActivation));
    }

    @Override
    public ServerExpressionNode visitOperation(com.elster.jupiter.metering.impl.config.OperationNode operationNode) {
        return new OperationNode(
                Operator.from(operationNode.getOperator()),
                operationNode.getLeftOperand().accept(this),
                operationNode.getRightOperand().accept(this));
    }

    @Override
    public ServerExpressionNode visitFunctionCall(com.elster.jupiter.metering.impl.config.FunctionCallNode functionCall) {
        List<ServerExpressionNode> arguments = functionCall.getChildren().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        Function function = Function.from(functionCall.getFunction());
        return new FunctionCallNode(function, arguments);
    }

}