package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
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
import java.util.stream.Stream;

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

    @Override
    public ServerFormulaBuilder init(ExpressionNodeBuilder nodeBuilder) {
        this.nodebuilder = nodeBuilder;
        return this;
    }

    @Override
    public ServerFormulaBuilder init(ExpressionNode formulaPart) {
        this.node = formulaPart;
        return this;
    }

    @Override
    public Formula build() {
        if (node == null) {
            node = nodebuilder.create();
        }
        Formula formula = dataModel.getInstance(FormulaImpl.class).init(mode, node);
        formula.save();
        return formula;
    }

    @Override
    public ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        return () -> new ReadingTypeDeliverableNodeImpl(readingTypeDeliverable);
    }

    @Override
    public ExpressionNodeBuilder requirement(ReadingTypeRequirement value) {
        return () -> new ReadingTypeRequirementNodeImpl(value);
    }

    public ExpressionNodeBuilder requirement(ReadingTypeRequirementNode existingNode) {
        return () -> existingNode;
    }

    @Override
    public ExpressionNodeBuilder constant(BigDecimal value) {
        return () -> new ConstantNodeImpl(value);
    }

    @Override
    public ExpressionNodeBuilder nullValue() {
        return () -> new NullNodeImpl();
    }

    @Override
    public ExpressionNodeBuilder constant(long value) {
        return () -> new ConstantNodeImpl(BigDecimal.valueOf(value));
    }

    @Override
    public ExpressionNodeBuilder constant(double value) {
        return () -> new ConstantNodeImpl(BigDecimal.valueOf(value));
    }

    @Override
    public ExpressionNodeBuilder sum(ExpressionNodeBuilder... terms) {
        return function(Function.SUM, terms);
    }

    @Override
    public ExpressionNodeBuilder maximum(ExpressionNodeBuilder... terms) {
        return function(Function.MAX, terms);
    }

    @Override
    public ExpressionNodeBuilder minimum(ExpressionNodeBuilder... terms) {
        return function(Function.MIN, terms);
    }

    @Override
    public ExpressionNodeBuilder average(ExpressionNodeBuilder... terms) {
        return function(Function.AVG, terms);
    }

    @Override
    public ExpressionNodeBuilder firstNotNull(ExpressionNodeBuilder... terms) {
        return function(Function.FIRST_NOT_NULL, terms);
    }

    @Override
    public ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression) {
        return function(Function.AGG_TIME, expression);
    }

    @Override
    public ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return () -> new OperationNodeImpl(Operator.PLUS,  term1.create(),  term2.create(), thesaurus);
    }

    @Override
    public ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return () -> new OperationNodeImpl(Operator.MINUS, term1.create(), term2.create(), thesaurus);
    }

    @Override
    public ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor) {
        return () -> new OperationNodeImpl(Operator.DIVIDE, dividend.create(), divisor.create(), thesaurus);
    }

    @Override
    public ExpressionNodeBuilder safeDivide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor, ExpressionNodeBuilder zeroReplacementNode) {
        return () -> new OperationNodeImpl(Operator.SAFE_DIVIDE, dividend.create(), divisor.create(), zeroReplacementNode.create(), thesaurus);
    }

    @Override
    public ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand) {
        return () -> new OperationNodeImpl(Operator.MULTIPLY,  multiplier.create(), multiplicand.create(), thesaurus);
    }

    private ExpressionNodeBuilder function(Function function, ExpressionNodeBuilder... terms) {
        if (mode.equals(Formula.Mode.AUTO)) {
            throw new InvalidNodeException(thesaurus, MessageSeeds.NO_FUNCTIONS_ALLOWED_IN_AUTOMODE);
        }
        return () -> new FunctionCallNodeImpl(
                        Arrays.stream(terms)
                            .map(ExpressionNodeBuilder::create)
                            .collect(Collectors.toList()),
                        function,
                        thesaurus);
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

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

}