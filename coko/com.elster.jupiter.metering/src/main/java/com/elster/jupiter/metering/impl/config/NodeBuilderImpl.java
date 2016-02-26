package com.elster.jupiter.metering.impl.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by igh on 26/02/2016.
 */
public class NodeBuilderImpl {

    public static NodeBuilder constant(BigDecimal value) {
        return () -> new ConstantNode(value);
    }

    public static NodeBuilder constant(long value) {
        return () -> new ConstantNode(BigDecimal.valueOf(value));
    }

    public static NodeBuilder constant(double value) {
        return () -> new ConstantNode(BigDecimal.valueOf(value));
    }

    public static NodeBuilder sum(NodeBuilder... terms) {
        return function(Function.SUM, terms);
    }

    public static NodeBuilder maximum(NodeBuilder... terms) {
        return function(Function.MAX, terms);
    }

    public static NodeBuilder minimum(NodeBuilder... terms) {
        return function(Function.MIN, terms);
    }

    public static NodeBuilder average(NodeBuilder... terms) {
        return function(Function.AVG, terms);
    }

    public static NodeBuilder plus(NodeBuilder term1, NodeBuilder term2) {
        return () -> new OperationNode(Operator.PLUS, term1.create(), term2.create());
    }

    public static NodeBuilder minus(NodeBuilder term1, NodeBuilder term2) {
        return () -> new OperationNode(Operator.MINUS, term1.create(), term2.create());
    }

    public static NodeBuilder divide(NodeBuilder dividend, NodeBuilder divisor) {
        return () -> new OperationNode(Operator.MINUS, dividend.create(), divisor.create());
    }

    public static NodeBuilder multiply(NodeBuilder multiplier, NodeBuilder multiplicand) {
        return () -> new OperationNode(Operator.MINUS, multiplier.create(), multiplicand.create());
    }

    private static NodeBuilder function(Function function, NodeBuilder... terms) {
        return () -> new FunctionCallNode(
                Arrays.stream(terms).map((NodeBuilder b) -> b.create()).collect(Collectors.toList()),
                function);
    }

    public static void main(String[] args) {
        ExpressionNode expressionNode = maximum(
                sum(constant(5.33), constant(3)),
                multiply(constant(7), constant(2)))
                        .create();

    }
}
