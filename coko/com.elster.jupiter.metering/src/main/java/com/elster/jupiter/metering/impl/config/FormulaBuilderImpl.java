package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.ExpressionNodeBuilder;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by igh on 26/02/2016.
 */
public class FormulaBuilderImpl implements ServerFormulaBuilder {

    private Formula.Mode mode;
    private DataModel dataModel;
    private ExpressionNodeBuilder nodebuilder; // use with api (default)
    private ExpressionNode node; // use with parser (first create a node from a String representation using ExpressionNodeParser)
    private Thesaurus thesaurus;

    public FormulaBuilderImpl(Formula.Mode mode, DataModel dataModel, Thesaurus thesaurus) {
        this.mode = mode;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public FormulaBuilder init(ExpressionNodeBuilder nodeBuilder) {
        this.nodebuilder = nodeBuilder;
        return this;
    }

    public FormulaBuilder init(ExpressionNode formulaPart) {
        this.node = formulaPart;
        return this;
    }

    public Formula build() {
        if (node == null) {
            node = nodebuilder.create();
        }
        Formula formula = dataModel.getInstance(FormulaImpl.class).init(mode, node);
        formula.save();
        return formula;
    }

    public ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        return () -> new ReadingTypeDeliverableNodeImpl(readingTypeDeliverable);
    }

    public ExpressionNodeBuilder requirement(ReadingTypeRequirement value) {
        return () -> new ReadingTypeRequirementNodeImpl(value);
    }

    public ExpressionNodeBuilder requirement(ReadingTypeRequirementNode existingNode) {
        return () -> existingNode;
    }

    public ExpressionNodeBuilder constant(BigDecimal value) {
        return () -> new ConstantNodeImpl(value);
    }

    public ExpressionNodeBuilder constant(long value) {
        return () -> new ConstantNodeImpl(BigDecimal.valueOf(value));
    }

    public ExpressionNodeBuilder constant(double value) {
        return () -> new ConstantNodeImpl(BigDecimal.valueOf(value));
    }

    public ExpressionNodeBuilder sum(ExpressionNodeBuilder... terms) {
        return function(Function.SUM, terms);
    }

    public ExpressionNodeBuilder maximum(ExpressionNodeBuilder... terms) {
        return function(Function.MAX, terms);
    }

    public ExpressionNodeBuilder minimum(ExpressionNodeBuilder... terms) {
        return function(Function.MIN, terms);
    }

    public ExpressionNodeBuilder average(ExpressionNodeBuilder... terms) {
        return function(Function.AVG, terms);
    }

    @Override
    public ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression) {
        return function(Function.AGG_TIME, expression);
    }

    public ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return () -> new OperationNodeImpl(Operator.PLUS,  term1.create(),  term2.create(), thesaurus);
    }

    public ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return () -> new OperationNodeImpl(Operator.MINUS, term1.create(), term2.create(), thesaurus);
    }

    public ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor) {
        return () -> new OperationNodeImpl(Operator.DIVIDE, dividend.create(), divisor.create(), thesaurus);
    }

    public ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand) {
        return () -> new OperationNodeImpl(Operator.MULTIPLY,  multiplier.create(), multiplicand.create(), thesaurus);
    }

    private ExpressionNodeBuilder function(Function function, ExpressionNodeBuilder... terms) {
        return () -> new FunctionCallNodeImpl(
                Arrays.stream(terms)
                        .map(ExpressionNodeBuilder::create)
                        .collect(Collectors.toList()),
                        function, thesaurus);
    }

    public void setNodebuilder(ExpressionNodeBuilder nodebuilder) {
        this.nodebuilder = nodebuilder;
    }

    public void setNode(ExpressionNode node) {
        this.node = node;
    }

    public Formula.Mode getMode() {
        return mode;
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}