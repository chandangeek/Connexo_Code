package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.FormulaPart;
import com.elster.jupiter.metering.config.NodeBuilder;
import com.elster.jupiter.metering.config.FormulaBuilder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by igh on 26/02/2016.
 */
public class FormulaBuilderImpl implements FormulaBuilder {

    public NodeBuilder constant(BigDecimal value) {
        return () -> new ConstantNode(value);
    }

    public NodeBuilder constant(long value) {
        return () -> new ConstantNode(BigDecimal.valueOf(value));
    }

    public NodeBuilder constant(double value) {
        return () -> new ConstantNode(BigDecimal.valueOf(value));
    }

    public NodeBuilder sum(NodeBuilder... terms) {
        return function(Function.SUM, terms);
    }

    public NodeBuilder maximum(NodeBuilder... terms) {
        return function(Function.MAX, terms);
    }

    public NodeBuilder minimum(NodeBuilder... terms) {
        return function(Function.MIN, terms);
    }

    public NodeBuilder average(NodeBuilder... terms) {
        return function(Function.AVG, terms);
    }

    public NodeBuilder plus(NodeBuilder term1, NodeBuilder term2) {
        return () -> new OperationNode(Operator.PLUS, (ExpressionNode) term1.create(), (ExpressionNode) term2.create());
    }

    public NodeBuilder minus(NodeBuilder term1, NodeBuilder term2) {
        return () -> new OperationNode(Operator.MINUS, (ExpressionNode) term1.create(), (ExpressionNode) term2.create());
    }

    public NodeBuilder divide(NodeBuilder dividend, NodeBuilder divisor) {
        return () -> new OperationNode(Operator.MINUS, (ExpressionNode) dividend.create(), (ExpressionNode) divisor.create());
    }

    public NodeBuilder multiply(NodeBuilder multiplier, NodeBuilder multiplicand) {
        return () -> new OperationNode(Operator.MINUS, (ExpressionNode) multiplier.create(), (ExpressionNode) multiplicand.create());
    }

    private NodeBuilder function(Function function, NodeBuilder... terms) {
        return () -> new FunctionCallNode(
                Arrays.stream(terms)
                        .map((NodeBuilder b) -> (ExpressionNode) b.create())
                        .collect(Collectors.toList()),
                        function);
    }

    public static void main(String[] args) {
        FormulaBuilder builder = new FormulaBuilderImpl();
        NodeBuilder nodeBuilder = builder.maximum(
                builder.sum(builder.constant(5.33), builder.constant(3)),
                builder.multiply(builder.constant(7), builder.constant(2)));
        FormulaPart formulaPart = nodeBuilder.create();

    }
}
