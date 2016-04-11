package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * Created by igh on 24/03/2016.
 */
public class ReadingTypeDeliverableBuilderImpl implements ReadingTypeDeliverableBuilder {

    private FormulaBuilderImpl formulaBuilder;
    private String name;
    private ServerMetrologyConfiguration metrologyConfiguration;
    private ReadingType readingType;

    public ReadingTypeDeliverableBuilderImpl(ServerMetrologyConfiguration metrologyConfiguration, String name, ReadingType readingType, Formula.Mode mode, DataModel dataModel, Thesaurus thesaurus) {
        this.formulaBuilder = new FormulaBuilderImpl(mode, dataModel, thesaurus);
        this.name = name;
        this.metrologyConfiguration = metrologyConfiguration;
        this.readingType = readingType;
    }

    @Override
    public ReadingTypeDeliverable build(FormulaBuilder nodeBuilder) {
        formulaBuilder.setNodebuilder((FormulaAndExpressionNodeBuilder) nodeBuilder);
        return doBuild();
    }

    public ReadingTypeDeliverable build(ExpressionNode formulaPart) {
        formulaBuilder.setNode(formulaPart);
        return doBuild();
    }

    @Override
    public FormulaBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        if (!readingTypeDeliverable.getMetrologyConfiguration().equals(metrologyConfiguration)) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE, (int) readingTypeDeliverable.getId());
        }
        if ((isAutoMode() && readingTypeDeliverable.getFormula().getMode().equals(Formula.Mode.EXPERT)) ||
            (isExpertMode() && readingTypeDeliverable.getFormula().getMode().equals(Formula.Mode.AUTO))){
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED);
        }
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.deliverable(readingTypeDeliverable));
    }

    @Override
    public FormulaBuilder requirement(ReadingTypeRequirement requirement) {
        if (!requirement.getMetrologyConfiguration().equals(metrologyConfiguration)) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT, (int) requirement.getId());
        }
        if ((isAutoMode()) && (!requirement.isRegular())) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.IRREGULAR_READINGTYPE_IN_REQUIREMENT);
        }
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.requirement(requirement));
    }

    @Override
    public FormulaBuilder requirement(ReadingTypeRequirementNode existingNode) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.requirement(existingNode));
    }

    @Override
    public FormulaBuilder constant(BigDecimal value) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.constant(value));
    }

    @Override
    public FormulaBuilder constant(long value) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.constant(value));
    }

    @Override
    public FormulaBuilder constant(double value) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.constant(value));
    }

    @Override
    public FormulaBuilder sum(FormulaBuilder... terms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.sum(this.toExpressionNodeBuilders(terms)));
    }

    protected FormulaAndExpressionNodeBuilder[] toExpressionNodeBuilders(FormulaBuilder[] terms) {
        return Stream.of(terms).map(FormulaAndExpressionNodeBuilder.class::cast).toArray(FormulaAndExpressionNodeBuilder[]::new);
    }

    @Override
    public FormulaBuilder maximum(FormulaBuilder... terms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.maximum(this.toExpressionNodeBuilders(terms)));
    }

    @Override
    public FormulaBuilder minimum(FormulaBuilder... terms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.minimum(this.toExpressionNodeBuilders(terms)));
    }

    @Override
    public FormulaBuilder average(FormulaBuilder... terms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.average(this.toExpressionNodeBuilders(terms)));
    }

    @Override
    public FormulaBuilder aggregate(FormulaBuilder expression) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.aggregate((FormulaAndExpressionNodeBuilder) expression));
    }

    @Override
    public FormulaBuilder plus(FormulaBuilder term1, FormulaBuilder term2) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.plus((FormulaAndExpressionNodeBuilder) term1, (FormulaAndExpressionNodeBuilder) term2));
    }

    @Override
    public FormulaBuilder minus(FormulaBuilder term1, FormulaBuilder term2) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.minus((FormulaAndExpressionNodeBuilder) term1, (FormulaAndExpressionNodeBuilder) term2));
    }

    @Override
    public FormulaBuilder divide(FormulaBuilder dividend, FormulaBuilder divisor) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.divide((FormulaAndExpressionNodeBuilder) dividend, (FormulaAndExpressionNodeBuilder) divisor));
    }

    @Override
    public FormulaBuilder multiply(FormulaBuilder multiplier, FormulaBuilder multiplicand) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.multiply((FormulaAndExpressionNodeBuilder) multiplier, (FormulaAndExpressionNodeBuilder) multiplicand));
    }

    public ReadingTypeDeliverable doBuild() {
        return metrologyConfiguration.addReadingTypeDeliverable(name, readingType, formulaBuilder.build());
    }

    private class FormulaAndExpressionNodeBuilder implements FormulaBuilder, ExpressionNodeBuilder {
        private final ExpressionNodeBuilder expressionNodeBuilder;

        private FormulaAndExpressionNodeBuilder(ExpressionNodeBuilder expressionNodeBuilder) {
            this.expressionNodeBuilder = expressionNodeBuilder;
        }

        @Override
        public ExpressionNode create() {
            return this.expressionNodeBuilder.create();
        }
    }

    private boolean isAutoMode() {
        return formulaBuilder.getMode().equals(Formula.Mode.AUTO);
    }

    private boolean isExpertMode() {
        return !isAutoMode();
    }




}