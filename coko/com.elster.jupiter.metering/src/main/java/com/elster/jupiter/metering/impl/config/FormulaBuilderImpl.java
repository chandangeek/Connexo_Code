/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by igh on 26/02/2016.
 */
public class FormulaBuilderImpl implements ServerFormulaBuilder {

    private Formula.Mode mode;
    private DataModel dataModel;
    private ExpressionNodeBuilder nodebuilder; // use with api (default)
    private ServerExpressionNode node; // use with parser (first create a node from a String representation using ExpressionNodeParser)
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
    public ServerFormulaBuilder init(ServerExpressionNode formulaPart) {
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
        return () -> ((ServerExpressionNode) existingNode);
    }

    @Override
    public ExpressionNodeBuilder property(RegisteredCustomPropertySet customPropertySet, PropertySpec propertySpec) {
        return () -> new CustomPropertyNodeImpl(propertySpec, customPropertySet);
    }

    @Override
    public ExpressionNodeBuilder constant(BigDecimal value) {
        return () -> new ConstantNodeImpl(value);
    }

    @Override
    public ExpressionNodeBuilder nullValue() {
        return NullNodeImpl::new;
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
    public ExpressionNodeBuilder minimum(List<ExpressionNodeBuilder> terms) {
        return function(Function.MIN, null, terms);
    }

    @Override
    public ExpressionNodeBuilder maximum(List<ExpressionNodeBuilder> terms) {
        return function(Function.MAX, null, terms);
    }

    @Override
    public ExpressionNodeBuilder sum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms) {
        return function(Function.SUM, aggregationLevel, terms);
    }

    @Override
    public ExpressionNodeBuilder maximum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms) {
        return function(Function.MAX_AGG, aggregationLevel, terms);
    }

    @Override
    public ExpressionNodeBuilder minimum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms) {
        return function(Function.MIN_AGG, aggregationLevel, terms);
    }

    @Override
    public ExpressionNodeBuilder average(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms) {
        return function(Function.AVG, aggregationLevel, terms);
    }

    @Override
    public ExpressionNodeBuilder firstNotNull(List<ExpressionNodeBuilder> terms) {
        return function(Function.FIRST_NOT_NULL, null, terms);
    }

    @Override
    public ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression) {
        return function(Function.AGG_TIME, null, Collections.singletonList(expression));
    }

    @Override
    public ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return () -> new OperationNodeImpl(Operator.PLUS, term1.create(), term2.create(), thesaurus);
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
        return () -> new OperationNodeImpl(Operator.MULTIPLY, multiplier.create(), multiplicand.create(), thesaurus);
    }

    @Override
    public ExpressionNodeBuilder power(ExpressionNodeBuilder term, ExpressionNodeBuilder exponent) {
        return function(Function.POWER, null, Arrays.asList(term, exponent));
    }

    @Override
    public ExpressionNodeBuilder squareRoot(ExpressionNodeBuilder term) {
        return function(Function.SQRT, null, Collections.singletonList(term));
    }

    private ExpressionNodeBuilder function(Function function, AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms) {
        this.validateUseOfFunctions(function);
        if (function.requiresAggregationLevel()) {
            if (aggregationLevel == null) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.AGGREGATION_FUNCTION_REQUIRES_AGGREGATION_LEVEL);
            }
            if (terms.isEmpty()) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED);
            }
        }
        return () -> new FunctionCallNodeImpl(
                terms.stream()
                        .map(ExpressionNodeBuilder::create)
                        .collect(Collectors.toList()),
                function,
                aggregationLevel,
                thesaurus);
    }

    private void validateUseOfFunctions(Function function) {
        if (!this.mode.supports(function)) {
            throw InvalidNodeException.functionNotAllowedInAutoMode(this.thesaurus, function);
        }
    }

    public void setNodebuilder(ExpressionNodeBuilder nodebuilder) {
        this.nodebuilder = nodebuilder;
    }

    public void setNode(ServerExpressionNode node) {
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