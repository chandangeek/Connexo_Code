/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ReadingTypeDeliverableBuilder} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-03-24
 */
public class ReadingTypeDeliverableBuilderImpl implements ReadingTypeDeliverableBuilder {

    private final FormulaBuilderImpl formulaBuilder;
    private final String name;
    private final ServerMetrologyConfiguration metrologyConfiguration;
    private final ReadingType readingType;
    private final CustomPropertySetService customPropertySetService;
    private final DeliverableType deliverableType;

    ReadingTypeDeliverableBuilderImpl(ServerMetrologyConfiguration metrologyConfiguration, String name, DeliverableType deliverableType, ReadingType readingType, Formula.Mode mode, CustomPropertySetService customPropertySetService, DataModel dataModel, Thesaurus thesaurus) {
        this.formulaBuilder = new FormulaBuilderImpl(mode, dataModel, thesaurus);
        this.name = name;
        this.metrologyConfiguration = metrologyConfiguration;
        this.deliverableType = deliverableType;
        this.readingType = readingType;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public ReadingTypeDeliverable build(FormulaBuilder nodeBuilder) {
        formulaBuilder.setNodebuilder((FormulaAndExpressionNodeBuilder) nodeBuilder);
        return doBuild();
    }

    public ReadingTypeDeliverable build(ServerExpressionNode formulaPart) {
        formulaBuilder.setNode(formulaPart);
        return doBuild();
    }

    @Override
    public FormulaBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable) {
        if (!readingTypeDeliverable.getMetrologyConfiguration().equals(metrologyConfiguration)) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_DELIVERABLE, (int) readingTypeDeliverable.getId());
        }
        if ((isAutoMode() && readingTypeDeliverable.getFormula().getMode().equals(Formula.Mode.EXPERT)) ||
                (isExpertMode() && readingTypeDeliverable.getFormula().getMode().equals(Formula.Mode.AUTO))) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.AUTO_AND_EXPERT_MODE_CANNOT_BE_COMBINED);
        }
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.deliverable(readingTypeDeliverable));
    }

    @Override
    public FormulaBuilder requirement(ReadingTypeRequirement requirement) {
        if (!requirement.getMetrologyConfiguration().equals(metrologyConfiguration)) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.INVALID_METROLOGYCONFIGURATION_FOR_REQUIREMENT, (int) requirement.getId());
        }
        if ((isAutoMode()) && (!UnitConversionSupport.isValidForAggregation(requirement.getUnits()))) {
            throw new InvalidNodeException(this.formulaBuilder.getThesaurus(), MessageSeeds.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT);
        }
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.requirement(requirement));
    }

    @Override
    public FormulaBuilder property(CustomPropertySet customPropertySet, PropertySpec propertySpec) {
        if (!this.customPropertySetIsConfiguredOnMetrologyConfiguration(customPropertySet)) {
            throw InvalidNodeException.customPropertyNotConfigured(this.formulaBuilder.getThesaurus(), propertySpec, customPropertySet);
        }
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = this.customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId());
        if (!registeredCustomPropertySet.isPresent()) {
            throw InvalidNodeException.customPropertySetNoLongerActive(this.formulaBuilder.getThesaurus(), customPropertySet);
        }
        if (!registeredCustomPropertySet.get().getCustomPropertySet().isVersioned()) {
            throw InvalidNodeException.customPropertySetNotVersioned(this.formulaBuilder.getThesaurus(), customPropertySet);
        }
        this.checkCompatibility(propertySpec, customPropertySet);
        return new FormulaAndExpressionNodeBuilder(this.formulaBuilder.property(registeredCustomPropertySet.get(), propertySpec));
    }

    private boolean customPropertySetIsConfiguredOnMetrologyConfiguration(CustomPropertySet customPropertySet) {
        return this.metrologyConfiguration
                .getCustomPropertySets()
                .stream()
                .anyMatch(each -> each.getCustomPropertySet().getId().equals(customPropertySet.getId()));
    }

    private void checkCompatibility(PropertySpec propertySpec, CustomPropertySet customPropertySet) {
        if (propertySpec.isReference()) {
            if (!this.isCompatible(propertySpec, SyntheticLoadProfile.class)) {
                throw InvalidNodeException.customPropertyMustBeSyntheticLoadProfile(this.formulaBuilder.getThesaurus(), customPropertySet, propertySpec);
            }
        } else if (!this.isCompatible(propertySpec, Number.class, Quantity.class)) {
            throw InvalidNodeException.customPropertyMustBeNumerical(this.formulaBuilder.getThesaurus(), customPropertySet, propertySpec);
        }

    }

    @SuppressWarnings("unchecked")
    private boolean isCompatible(PropertySpec propertySpec, Class...supportedValueTypes) {
        Class valueType = propertySpec.getValueFactory().getValueType();
        return Stream.of(supportedValueTypes).anyMatch(each -> each.isAssignableFrom(valueType));
    }

    @Override
    public FormulaBuilder nullValue() {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.nullValue());
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
    public FormulaBuilder minimum(FormulaBuilder firstTerm, FormulaBuilder secondTerm, FormulaBuilder... remainingTerms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.minimum(this.toExpressionNodeBuilders(firstTerm, secondTerm, remainingTerms)));
    }

    @Override
    public FormulaBuilder maximum(FormulaBuilder firstTerm, FormulaBuilder secondTerm, FormulaBuilder... remainingTerms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.maximum(this.toExpressionNodeBuilders(firstTerm, secondTerm, remainingTerms)));
    }

    @Override
    public FormulaBuilder sum(AggregationLevel aggregationLevel, FormulaBuilder term) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.sum(aggregationLevel, this.toExpressionNodeBuilders(term)));
    }

    @Override
    public FormulaBuilder maximum(AggregationLevel aggregationLevel, FormulaBuilder term) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.maximum(aggregationLevel, this.toExpressionNodeBuilders(term)));
    }

    @Override
    public FormulaBuilder minimum(AggregationLevel aggregationLevel, FormulaBuilder term) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.minimum(aggregationLevel, this.toExpressionNodeBuilders(term)));
    }

    @Override
    public FormulaBuilder average(AggregationLevel aggregationLevel, FormulaBuilder term) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.average(aggregationLevel, this.toExpressionNodeBuilders(term)));
    }

    private List<ExpressionNodeBuilder> toExpressionNodeBuilders(FormulaBuilder firstTerm) {
        return this.allToExpressionNodeBuilders(firstTerm);
    }

    private List<ExpressionNodeBuilder> toExpressionNodeBuilders(FormulaBuilder firstTerm, FormulaBuilder... remainingTerms) {
        return this.allToExpressionNodeBuilders(
                Stream.of(
                        Stream.of(firstTerm),
                        Stream.of(remainingTerms))
                        .flatMap(Function.identity()));
    }

    private List<ExpressionNodeBuilder> toExpressionNodeBuilders(FormulaBuilder firstTerm, FormulaBuilder secondTerm, FormulaBuilder... remainingTerms) {
        return this.allToExpressionNodeBuilders(
                Stream.of(
                        Stream.of(firstTerm, secondTerm),
                        Stream.of(remainingTerms))
                        .flatMap(Function.identity()));
    }

    private List<ExpressionNodeBuilder> allToExpressionNodeBuilders(FormulaBuilder... terms) {
        return this.allToExpressionNodeBuilders(Stream.of(terms));
    }

    private List<ExpressionNodeBuilder> allToExpressionNodeBuilders(Stream<FormulaBuilder> terms) {
        return terms
                .map(FormulaAndExpressionNodeBuilder.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public FormulaBuilder aggregate(FormulaBuilder expression) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.aggregate((FormulaAndExpressionNodeBuilder) expression));
    }

    @Override
    public FormulaBuilder plus(FormulaBuilder term1, FormulaBuilder term2) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.plus(
                        (FormulaAndExpressionNodeBuilder) term1,
                        (FormulaAndExpressionNodeBuilder) term2));
    }

    @Override
    public FormulaBuilder minus(FormulaBuilder term1, FormulaBuilder term2) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.minus(
                        (FormulaAndExpressionNodeBuilder) term1,
                        (FormulaAndExpressionNodeBuilder) term2));
    }

    @Override
    public FormulaBuilder divide(FormulaBuilder dividend, FormulaBuilder divisor) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.divide(
                        (FormulaAndExpressionNodeBuilder) dividend,
                        (FormulaAndExpressionNodeBuilder) divisor));
    }

    @Override
    public FormulaBuilder safeDivide(FormulaBuilder dividend, FormulaBuilder divisor, FormulaBuilder zeroReplacement) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.safeDivide(
                        (FormulaAndExpressionNodeBuilder) dividend,
                        (FormulaAndExpressionNodeBuilder) divisor,
                        (FormulaAndExpressionNodeBuilder) zeroReplacement));
    }

    @Override
    public FormulaBuilder power(FormulaBuilder expression, FormulaBuilder exponent) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.power(
                        (FormulaAndExpressionNodeBuilder) expression,
                        (FormulaAndExpressionNodeBuilder) exponent));
    }

    @Override
    public FormulaBuilder squareRoot(FormulaBuilder expression) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.squareRoot((FormulaAndExpressionNodeBuilder) expression));
    }

    @Override
    public FormulaBuilder firstNotNull(FormulaBuilder firstTerm, FormulaBuilder... remainingTerms) {
        return new FormulaAndExpressionNodeBuilder(formulaBuilder.firstNotNull(this.toExpressionNodeBuilders(firstTerm, remainingTerms)));
    }

    @Override
    public FormulaBuilder multiply(FormulaBuilder multiplier, FormulaBuilder multiplicand) {
        return new FormulaAndExpressionNodeBuilder(
                formulaBuilder.multiply(
                        (FormulaAndExpressionNodeBuilder) multiplier,
                        (FormulaAndExpressionNodeBuilder) multiplicand));
    }

    public ReadingTypeDeliverable doBuild() {
        if (metrologyConfiguration.getDeliverables().stream().anyMatch(deliverable -> deliverable.getReadingType().equals(readingType))) {
            throw new ReadingTypeAlreadyUsedOnMetrologyConfiguration(formulaBuilder.getThesaurus());
        }
        return metrologyConfiguration.addReadingTypeDeliverable(name, deliverableType, readingType, formulaBuilder.build());
    }

    private class FormulaAndExpressionNodeBuilder implements FormulaBuilder, ExpressionNodeBuilder {
        private final ExpressionNodeBuilder expressionNodeBuilder;

        private FormulaAndExpressionNodeBuilder(ExpressionNodeBuilder expressionNodeBuilder) {
            this.expressionNodeBuilder = expressionNodeBuilder;
        }

        @Override
        public ServerExpressionNode create() {
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
