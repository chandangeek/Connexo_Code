package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.ExpressionNodeBuilder;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;

/**
 * Created by igh on 24/03/2016.
 */
public class ReadingTypeDeliverableBuilder {

    private FormulaBuilderImpl formulaBuilder;
    private String name;
    private MetrologyConfiguration metrologyConfiguration;
    private ReadingType readingType;

    public ReadingTypeDeliverableBuilder(MetrologyConfiguration metrologyConfiguration, String name, ReadingType readingType, Formula.Mode mode, DataModel dataModel, Thesaurus thesaurus) {
        this.formulaBuilder = new FormulaBuilderImpl(mode, dataModel, thesaurus);
        this.name = name;
        this.metrologyConfiguration = metrologyConfiguration;
        this.readingType = readingType;
    }

    public ReadingTypeDeliverable build(ExpressionNodeBuilder nodeBuilder) {
        formulaBuilder.setNodebuilder(nodeBuilder);
        return doBuild();
    }

    public ReadingTypeDeliverable build(ExpressionNode formulaPart) {
        formulaBuilder.setNode(formulaPart);
        return doBuild();
    }

    public ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        return formulaBuilder.deliverable(readingTypeDeliverable);
    }

    public ExpressionNodeBuilder requirement(ReadingTypeRequirement value) {
        return formulaBuilder.requirement(value);
    }

    public ExpressionNodeBuilder requirement(ReadingTypeRequirementNode existingNode) {
        return formulaBuilder.requirement(existingNode);
    }

    public ExpressionNodeBuilder constant(BigDecimal value) {
        return formulaBuilder.constant(value);
    }

    public ExpressionNodeBuilder constant(long value) {
        return formulaBuilder.constant(value);
    }

    public ExpressionNodeBuilder constant(double value) {
        return formulaBuilder.constant(value);
    }

    public ExpressionNodeBuilder sum(ExpressionNodeBuilder... terms) {
        return formulaBuilder.sum(terms);
    }

    public ExpressionNodeBuilder maximum(ExpressionNodeBuilder... terms) {
        return formulaBuilder.maximum(terms);
    }

    public ExpressionNodeBuilder minimum(ExpressionNodeBuilder... terms) {
        return formulaBuilder.minimum(terms);
    }

    public ExpressionNodeBuilder average(ExpressionNodeBuilder... terms) {
        return formulaBuilder.average(terms);
    }

    public ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression) {
        return formulaBuilder.aggregate(expression);
    }

    public ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return formulaBuilder.plus(term1, term2);
    }

    public ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2) {
        return formulaBuilder.minus(term1, term2);
    }

    public ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor) {
        return formulaBuilder.divide(dividend, divisor);
    }

    public ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand) {
        return formulaBuilder.multiply(multiplier, multiplicand);
    }

    public ReadingTypeDeliverable doBuild() {
        return metrologyConfiguration.addReadingTypeDeliverable(name, readingType, formulaBuilder.build());
    }


}
