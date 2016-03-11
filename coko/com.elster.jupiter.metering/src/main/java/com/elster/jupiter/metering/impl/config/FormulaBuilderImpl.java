package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaPart;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.NodeBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by igh on 26/02/2016.
 */
public class FormulaBuilderImpl implements FormulaBuilder {

    private Formula.Mode mode;
    private DataModel dataModel;
    private NodeBuilder nodebuilder; // use with api (default)
    private ExpressionNode node; // use with parser (first create a node from a String representation using ExpressionNodeParser)

    public FormulaBuilderImpl(Formula.Mode mode, DataModel dataModel) {
        this.mode = mode;
        this.dataModel = dataModel;
    }

    public FormulaBuilder init(NodeBuilder nodeBuilder) {
        this.nodebuilder = nodeBuilder;
        return this;
    }

    public FormulaBuilder init(FormulaPart formulaPart) {
        this.node = (ExpressionNode) formulaPart;
        return this;
    }

    public Formula build() {
        if (node == null) {
            node = (ExpressionNode) nodebuilder.create();
        }
        Formula formula = dataModel.getInstance(FormulaImpl.class).init(mode, node);
        formula.save();
        return formula;
    }

    public NodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        return () -> new ReadingTypeDeliverableNode(readingTypeDeliverable);
    }

    public NodeBuilder requirement(ReadingTypeRequirement value) {
        return () -> new ReadingTypeRequirementNode(value);
    }

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
        return () -> new OperationNode(Operator.DIVIDE, (ExpressionNode) dividend.create(), (ExpressionNode) divisor.create());
    }

    public NodeBuilder multiply(NodeBuilder multiplier, NodeBuilder multiplicand) {
        return () -> new OperationNode(Operator.MULTIPLY, (ExpressionNode) multiplier.create(), (ExpressionNode) multiplicand.create());
    }

    private NodeBuilder function(Function function, NodeBuilder... terms) {
        return () -> new FunctionCallNode(
                Arrays.stream(terms)
                        .map((NodeBuilder b) -> (ExpressionNode) b.create())
                        .collect(Collectors.toList()),
                        function);
    }


    public static void main(String[] args) {
        DataModel dataModel = null;

        FormulaBuilder builder = new FormulaBuilderImpl(Formula.Mode.EXPERT, dataModel);
        builder.init(builder.maximum(
                builder.sum(builder.constant(5.33), builder.constant(3)),
                builder.multiply(builder.constant(7), builder.constant(2)))).build();

    }
}
