/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.util.Pair;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides code reuse for the different ReadingTypeGenerators
 */
abstract class AbstractReadingTypeGenerator {

    /**
     * Serves as a template for implementations who define a <i>partial</i> ReadingType
     */
    interface ReadingTypeTemplate {

        String getName();

        ReadingTypeCodeBuilder getReadingTypeCodeBuilder();

        /**
         * Indicate whether the generator should create a ReadingType based on the current ReadingTypeCodeBuilder for all
         * elements in the {@link #possibleTimeAttributes} enumeration.
         *
         * @return false by default, implementations can override
         */
        default boolean needsTimeAttributeExpansion() {
            return false;
        }

        /**
         * Indicate whether the generator should create a ReadingType based on the current ReadingTypeCodeBuilder for all
         * elements in the {@link #possibleAccumulations} enumeration.
         *
         * @return false by default, implementations can override
         */
        default boolean needsAccumulationExpansion() {
            return false;
        }

        /**
         * Indicate whether the generator should create a ReadingType based on the current ReadingTypeCodeBuilder for all
         * elements in the {@link #possibleTimeOfUseAttributes} enumeration.
         *
         * @return false by default, implementations can override
         */
        default boolean needsTOUExpansion() {
            return false;
        }

        /**
         * Indicate whether the generator should create a ReadingType based on the current ReadingTypeCodeBuilder for all
         * elements in the {@link #possiblePhases} enumeration.
         *
         * @return false by default, implementations can override
         */
        default boolean needsPhaseExpansion() {
            return false;
        }

        /**
         * Indicate whether the generator should create a ReadingType based on the current ReadingTypeCodeBuilder for all
         * elements in the {@link #possibleMultipliers} enumeration.
         *
         * @return false by default, implementations can override
         */
        default boolean needsMetricMultiplierExpansion() {
            return false;
        }
    }

    private Set<Accumulation> possibleAccumulations = EnumSet.of(Accumulation.BULKQUANTITY, Accumulation.DELTADELTA, Accumulation.SUMMATION);
    private Set<TimeAttribute> possibleTimeAttributes = EnumSet.of(TimeAttribute.NOTAPPLICABLE, TimeAttribute.MINUTE15, TimeAttribute.MINUTE60);
    private List<Integer> possibleTimeOfUseAttributes = Arrays.asList(0, 1, 2);
    private Set<Phase> possiblePhases = EnumSet.of(Phase.NOTAPPLICABLE, Phase.PHASEA, Phase.PHASEB, Phase.PHASEC, Phase.PHASEABC);
    private Set<MetricMultiplier> possibleMultipliers = EnumSet.of(MetricMultiplier.ZERO, MetricMultiplier.KILO);

    protected AbstractReadingTypeGenerator() {
    }

    /**
     * @return a Stream of all the ReadingTypeTemplates
     */
    abstract Stream<ReadingTypeTemplate> getReadingTypeTemplates();


    /**
     * Generate a list of Pairs. The Pair contains a <b>ReadingTypeCode</b> and an <b>AliasNames</b>.
     *
     * @return a list of pairs
     */
    public List<Pair<String, String>> generateReadingTypes() {
        return getReadingTypeTemplates().parallel().flatMap(this::startGenerationBasedOnAccumulation).collect(Collectors.toList());
    }

    private Stream<Pair<String, String>> startGenerationBasedOnAccumulation(ReadingTypeTemplate readingTypeTemplate) {
        if (readingTypeTemplate.needsAccumulationExpansion()) {
            return possibleAccumulations.stream()
                    .flatMap(possibleAccumulation -> continueGenerationWithTimeAttribute(readingTypeTemplate, possibleAccumulation));
        } else {
            return continueGenerationWithTimeAttribute(readingTypeTemplate, readingTypeTemplate.getReadingTypeCodeBuilder().getCurrentAccumulation());
        }
    }

    private Stream<Pair<String, String>> continueGenerationWithTimeAttribute(ReadingTypeTemplate readingTypeTemplate, Accumulation possibleAccumulation) {
        if (readingTypeTemplate.needsTimeAttributeExpansion()) {
            return possibleTimeAttributes.stream()
                    .flatMap(timeAttribute -> continueGenerationWithTimeOfUse(readingTypeTemplate, possibleAccumulation, timeAttribute));
        } else {
            return continueGenerationWithTimeOfUse(readingTypeTemplate, possibleAccumulation, readingTypeTemplate.getReadingTypeCodeBuilder().getCurrentTimeAttribute());
        }
    }

    private Stream<Pair<String, String>> continueGenerationWithTimeOfUse(ReadingTypeTemplate readingTypeTemplate, Accumulation possibleAccumulation, TimeAttribute timeAttribute) {
        if (readingTypeTemplate.needsTOUExpansion()) {
            return possibleTimeOfUseAttributes.stream()
                    .filter(tou -> inCaseOfBulkQuantity(possibleAccumulation, tou) || inCaseOfSummation(possibleAccumulation, tou) || inAnyOtherCase(possibleAccumulation))
                    .flatMap(timeOfUseAttribute -> continueGenerationWithPhase(readingTypeTemplate, possibleAccumulation, timeAttribute, timeOfUseAttribute));
        } else {
            return continueGenerationWithPhase(readingTypeTemplate, possibleAccumulation, timeAttribute, readingTypeTemplate.getReadingTypeCodeBuilder().getCurrentTimeOfUseAttribute());
        }
    }

    private boolean inCaseOfBulkQuantity(Accumulation possibleAccumulation, Integer timeOfUseAttribute) {
        return possibleAccumulation.equals(Accumulation.BULKQUANTITY) && timeOfUseAttribute == 0;
    }

    private boolean inCaseOfSummation(Accumulation possibleAccumulation, Integer timeOfUseAttribute) {
        return (possibleAccumulation.equals(Accumulation.SUMMATION) && timeOfUseAttribute > 0);
    }

    private boolean inAnyOtherCase(Accumulation possibleAccumulation) {
        return !(possibleAccumulation.equals(Accumulation.BULKQUANTITY) || possibleAccumulation.equals(Accumulation.SUMMATION));
    }

    private Stream<Pair<String, String>> continueGenerationWithPhase(ReadingTypeTemplate readingTypeTemplate, Accumulation possibleAccumulation, TimeAttribute timeAttribute, Integer timeOfUseAttribute) {
        if (readingTypeTemplate.needsPhaseExpansion()) {
            return possiblePhases.stream()
                    .flatMap(phase -> continueGenerationWithMultiplier(readingTypeTemplate, possibleAccumulation, timeAttribute, timeOfUseAttribute, phase));
        } else {
            return continueGenerationWithMultiplier(readingTypeTemplate, possibleAccumulation, timeAttribute, timeOfUseAttribute, readingTypeTemplate.getReadingTypeCodeBuilder().getCurrentPhase());
        }
    }

    private Stream<Pair<String, String>> continueGenerationWithMultiplier(ReadingTypeTemplate readingTypeTemplate, Accumulation possibleAccumulation, TimeAttribute timeAttribute, Integer timeOfUseAttribute, Phase phase) {
        if (readingTypeTemplate.needsMetricMultiplierExpansion()) {
            return possibleMultipliers.stream()
                    .map(metricMultiplier -> createReadingType(readingTypeTemplate, possibleAccumulation, timeAttribute, timeOfUseAttribute, phase, metricMultiplier));
        } else {
            return Stream.of(createReadingType(readingTypeTemplate, possibleAccumulation, timeAttribute, timeOfUseAttribute, phase, readingTypeTemplate.getReadingTypeCodeBuilder().getCurrentMetricMultiplier()));
        }
    }

    private Pair<String, String> createReadingType(ReadingTypeTemplate readingTypeTemplate, Accumulation accumulation, TimeAttribute possibleTimeAttribute, Integer possibleTimeOfUseAttribute, Phase possiblePhase, MetricMultiplier possibleMultiplier) {
        readingTypeTemplate.getReadingTypeCodeBuilder()
                .accumulate(accumulation)
                .period(possibleTimeAttribute)
                .tou(possibleTimeOfUseAttribute)
                .phase(possiblePhase)
                .in(possibleMultiplier);
        return Pair.of(readingTypeTemplate.getReadingTypeCodeBuilder().code(), readingTypeTemplate.getName());
    }
}
