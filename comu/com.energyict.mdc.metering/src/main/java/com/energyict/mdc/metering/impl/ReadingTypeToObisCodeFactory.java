/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.ReadingTypeInformation;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class ReadingTypeToObisCodeFactory {

    private static final int NUMBER_OF_READING_TYPE_ARGUMENTS = 18;
    private static final int SECONDS_IN_MINUTE = 60;

    private static final int PHASE_INDEX = 14;
    private static final int SCALER_INDEX = 15;
    private static final int READING_TYPE_UNIT_INDEX = 16;
    private static final int CURRENCY_INDEX = 17;
    private static final int TOU_INDEX = 11;
    private static final int HARMONIC_NUMERATOR_INDEX = 7;
    private static final int HARMONIC_DENOMINATOR_INDEX = 8;
    private static final int KIND_INDEX = 6;
    private static final int COMMODITY_INDEX = 5;
    private static final int FLOW_DIRECTION_INDEX = 4;
    private static final int ACCUMULATION_INDEX = 3;
    private static final int TIME_ATTRIBUTE_INDEX = 2;
    private static final int AGGREGATE_INDEX = 1;
    private static final int MACRO_PERIOD_INDEX = 0;

    private static Candidates<Integer> initOrUpdate(Candidates<Integer> options, Matcher<Integer> candidates) {
        if (candidates.getAllMatches().size() > 0) {
            if (options == null) {
                options = createOptionListWithCandidates(candidates);
            } else {
                applyCandidates(options, candidates);
            }
        }
        return options;
    }

    private static void applyCandidates(Candidates<Integer> options, Matcher<Integer> candidates) {
        options.applyWith(candidates.getAllMatches().toArray(new Integer[candidates.getAllMatches().size()]));
    }

    private static Candidates<Integer> createOptionListWithCandidates(Matcher<Integer> candidates) {
        return Candidates.initWith(candidates.getAllMatches().toArray(new Integer[candidates.getAllMatches().size()]));
    }

    public static ReadingTypeInformation from(String readingTypeMrdi) {
        String[] arguments = readingTypeMrdi.split("\\.");
        if (arguments.length != NUMBER_OF_READING_TYPE_ARGUMENTS) {
            throw new IllegalArgumentException("The provided ReadingType code should contain " + NUMBER_OF_READING_TYPE_ARGUMENTS + " fields.");
        }

        int scaler;
        ObisCode obisCode = null;
        Unit unit = null;
        TimeDuration timeDuration = null;
        OptionalCollector optionals = new OptionalCollector();

        applyMacroPeriodMapping(arguments[MACRO_PERIOD_INDEX], optionals);
        applyAggregateMapping(arguments[AGGREGATE_INDEX], optionals);
        applyMeasuringPeriodMapping(arguments[TIME_ATTRIBUTE_INDEX], optionals);
        applyAccumulation(arguments[ACCUMULATION_INDEX], optionals);
        applyFlowDirection(arguments[FLOW_DIRECTION_INDEX], optionals);
        applyCommodity(arguments[COMMODITY_INDEX], optionals);
        applyMeasurementKind(arguments, optionals);
        applyHarmonics(arguments, optionals);
        applyTimeOfUse(arguments[TOU_INDEX], optionals);
        applyPhase(arguments[PHASE_INDEX], optionals);
        scaler = Integer.parseInt(arguments[SCALER_INDEX]);
        applyReadingTypeUnit(arguments[READING_TYPE_UNIT_INDEX], optionals);
        applyCurrency(arguments[CURRENCY_INDEX], optionals);

        if (optionals.getaFieldCandidates() != null
                && optionals.getcFieldCandidates() != null
                && optionals.getdFieldCandidates() != null
                && optionals.geteFieldCandidates() != null
                && optionals.getfFieldCandidates() != null) {
            if (optionals.getaFieldCandidates().singleMatch()
                    && optionals.getcFieldCandidates().singleMatch()
                    && optionals.getdFieldCandidates().singleMatch()
                    && optionals.geteFieldCandidates().singleMatch()
                    && optionals.getfFieldCandidates().singleMatch()) {
                obisCode = new ObisCode(
                        optionals.getaFieldCandidates().getTheCandidate(),
                        0, // B-field should be zero
                        optionals.getcFieldCandidates().getTheCandidate(),
                        optionals.getdFieldCandidates().getTheCandidate(),
                        optionals.geteFieldCandidates().getTheCandidate(),
                        optionals.getfFieldCandidates().getTheCandidate());
            } else {
                throw new UnableToExtractUniqueObisCodeFromReadingTypeException(readingTypeMrdi, optionals);
            }
        }

        if (optionals.getBaseUnitCandidates() != null && optionals.getBaseUnitCandidates().singleMatch()) {
            unit = Unit.get(optionals.getBaseUnitCandidates().getTheCandidate(), scaler);
        }

        if (optionals.getIntervalSeconds() != null && optionals.getIntervalSeconds().singleMatch()) {
            timeDuration = new TimeDuration(optionals.getIntervalSeconds().getTheCandidate());
        }
        return new ReadingTypeInformation(obisCode, unit, timeDuration);
    }


    private static void applyMacroPeriodMapping(String argument, OptionalCollector optionalCollector) {
        for (MacroPeriodMapping macroPeriodMapping : MacroPeriodMapping.values()) {
            if (macroPeriodMapping.getMacroPeriod().getId() == Integer.parseInt(argument)) {
                optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), macroPeriodMapping.geteFieldMatcher()));
                optionalCollector.setfFieldCandidates(initOrUpdate(optionalCollector.getfFieldCandidates(), macroPeriodMapping.getfFieldMatcher()));
                optionalCollector.setIntervalSeconds(initOrUpdate(optionalCollector.getIntervalSeconds(), macroPeriodMapping.getTimeDurationMatcher()));
            }
        }
    }

    private static void applyAggregateMapping(String argument, OptionalCollector optionalCollector) {
        for (AggregateMapping aggregateMapping : AggregateMapping.values()) {
            if (aggregateMapping.getAggregate().getId() == Integer.parseInt(argument)) {
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), aggregateMapping.getcFieldMatcher()));
                optionalCollector.setdFieldCandidates(initOrUpdate(optionalCollector.getdFieldCandidates(), aggregateMapping.getdFieldMatcher()));
            }
        }
    }

    private static void applyMeasuringPeriodMapping(String argument, OptionalCollector optionalCollector) {
        for (MeasuringPeriodMapping measuringPeriodMapping : MeasuringPeriodMapping.values()) {
            if (measuringPeriodMapping.getTimeAttribute().getId() == Integer.parseInt(argument)) {
                optionalCollector.setIntervalSeconds(initOrUpdate(optionalCollector.getIntervalSeconds(), ItemMatcher.itemMatcherFor(measuringPeriodMapping.getMinutes() * SECONDS_IN_MINUTE)));
            }
        }
    }

    private static void applyAccumulation(String argument, OptionalCollector optionalCollector) {
        for (AccumulationMapping accumulationMapping : AccumulationMapping.values()) {
            if (accumulationMapping.getAccumulation().getId() == Integer.parseInt(argument)) {
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), accumulationMapping.getcFieldMatcher()));
                optionalCollector.setdFieldCandidates(initOrUpdate(optionalCollector.getdFieldCandidates(), accumulationMapping.getdFieldMatcher()));
                optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), accumulationMapping.geteFieldMatcher()));
                optionalCollector.setIntervalSeconds(initOrUpdate(optionalCollector.getIntervalSeconds(), accumulationMapping.getTimeDurationMatcher()));
            }
        }
    }

    private static void applyFlowDirection(String argument, OptionalCollector optionalCollector) {
        for (FlowDirectionMapping flowDirectionMapping : FlowDirectionMapping.values()) {
            if (flowDirectionMapping.getFlowDirection().getId() == Integer.parseInt(argument)) {
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), flowDirectionMapping.getPossibleCValues()));
            }
        }
    }

    private static void applyCommodity(String argument, OptionalCollector optionalCollector) {
        for (CommodityMapping commodityMapping : CommodityMapping.values()) {
            if (commodityMapping.getCommodity().getId() == Integer.parseInt(argument)) {
                optionalCollector.setaFieldCandidates(initOrUpdate(optionalCollector.getaFieldCandidates(), ItemMatcher.itemMatcherFor(commodityMapping.getObisCodeAField())));
            }
        }
    }

    private static void applyMeasurementKind(String[] arguments, OptionalCollector optionalCollector) {
        for (MeasurementKindMapping measurementKindMapping : MeasurementKindMapping.values()) {
            if (measurementKindMapping.getKind().getId() == Integer.parseInt(arguments[KIND_INDEX])) {
                optionalCollector.setaFieldCandidates(initOrUpdate(optionalCollector.getaFieldCandidates(), measurementKindMapping.getaField()));
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), measurementKindMapping.getcField()));
                optionalCollector.setdFieldCandidates(initOrUpdate(optionalCollector.getdFieldCandidates(), measurementKindMapping.getdField()));
                optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), measurementKindMapping.geteField()));
                optionalCollector.setBaseUnitCandidates(initOrUpdate(optionalCollector.getBaseUnitCandidates(), measurementKindMapping.getUnitMatcher()));
            }
        }
    }

    private static void applyHarmonics(String[] arguments, OptionalCollector optionalCollector) {
        if (Integer.parseInt(arguments[HARMONIC_DENOMINATOR_INDEX]) > 0) {
            optionalCollector.setaFieldCandidates(initOrUpdate(optionalCollector.getaFieldCandidates(), ItemMatcher.itemMatcherFor(1)));
            optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), InterHarmonicMapping.getcFieldValues()));
            optionalCollector.setdFieldCandidates(initOrUpdate(optionalCollector.getdFieldCandidates(), InterHarmonicMapping.getdFieldValues()));
            optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), ItemMatcher.itemMatcherFor(Integer.parseInt(arguments[HARMONIC_NUMERATOR_INDEX]))));
        }
    }

    private static void applyTimeOfUse(String argument, OptionalCollector optionalCollector) {
        optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), ItemMatcher.itemMatcherFor(Integer.parseInt(argument))));
    }

    private static void applyPhase(String argument, OptionalCollector optionalCollector) {
        for (PhaseMapping phaseMapping : PhaseMapping.values()) {
            if (phaseMapping.getCimPhase().getId() == Integer.parseInt(argument)) {
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), phaseMapping.getcField()));
                optionalCollector.setdFieldCandidates(initOrUpdate(optionalCollector.getdFieldCandidates(), phaseMapping.getdField()));
                optionalCollector.seteFieldCandidates(initOrUpdate(optionalCollector.geteFieldCandidates(), phaseMapping.geteField()));
            }
        }
    }

    private static void applyReadingTypeUnit(String argument, OptionalCollector optionalCollector) {
        for (ReadingTypeUnitMapping readingTypeUnitMapping : ReadingTypeUnitMapping.values()) {
            if (readingTypeUnitMapping.getCimUnit().getId() == Integer.parseInt(argument)) {
                optionalCollector.setcFieldCandidates(initOrUpdate(optionalCollector.getcFieldCandidates(), readingTypeUnitMapping.getcField()));
                optionalCollector.setBaseUnitCandidates(initOrUpdate(optionalCollector.getBaseUnitCandidates(), ItemMatcher.itemMatcherFor(readingTypeUnitMapping.getMdcUnit())));
            }
        }
    }

    private static void applyCurrency(String argument, OptionalCollector optionalCollector) {
        for (CurrencyMapping currencyMapping : CurrencyMapping.values()) {
            if (currencyMapping.getCurrency().getCurrencyCode().equals(argument)) {
                optionalCollector.setBaseUnitCandidates(initOrUpdate(optionalCollector.getBaseUnitCandidates(), ItemMatcher.itemMatcherFor(currencyMapping.getBaseUnit())));
            }
        }
    }

    /**
     * Keeps track of possible candidates for a field
     *
     * @param <T>
     */
    private static class Candidates<T> {

        private final Set<T> candidates = new HashSet<>();

        @SafeVarargs
        static <T> Candidates<T> initWith(T... initializer) {
            return new Candidates<>(initializer);
        }

        @SafeVarargs
        private Candidates(T... initializer) {
            Collections.addAll(this.candidates, initializer);
        }

        /**
         * Applies all given values with the currently existing candidates.
         * The <i>Greatest common divisor</i> should remain.
         *
         * @param values the values to apply
         */
        @SafeVarargs
        public final void applyWith(T... values) {
            List<T> optionList = Arrays.asList(values);
            Iterator<T> optionIterator = candidates.iterator();
            while (optionIterator.hasNext()) {
                if (!optionList.contains(optionIterator.next())) {
                    optionIterator.remove();
                }
            }
        }

        /**
         * Checks if there is a single match
         *
         * @return if one and only one Candidate exists, false otherwise
         */
        public boolean singleMatch() {
            return candidates.size() == 1;
        }

        /**
         * @return the only existing Candidate
         */
        public T getTheCandidate() {
            if (singleMatch()) {
                return candidates.iterator().next();
            } else {
                throw new NotAUniqueCandidate();
            }
        }

        /**
         * @return <i>all</i> Candidates
         */
        public Set<T> getAllCandidates() {
            return candidates;
        }
    }

    private static class NotAUniqueCandidate extends RuntimeException {

        NotAUniqueCandidate() {
            super("Multiple candidates were remaining, only one is allowed.");
        }
    }

    private static class UnableToExtractUniqueObisCodeFromReadingTypeException extends RuntimeException {

        private final String readingTypeMrdi;
        private final OptionalCollector optionals;

        public UnableToExtractUniqueObisCodeFromReadingTypeException(String readingTypeMrdi, OptionalCollector optionals) {
            this.readingTypeMrdi = readingTypeMrdi;
            this.optionals = optionals;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("Could not extract a unique ObisCode for given ReadingType.\r\n");
            builder.append("\r\n\t - ReadingType : ").append(readingTypeMrdi);
            builder.append("\r\n\t - A : ").append(getAllCandidates(optionals.getaFieldCandidates()));
            builder.append("\r\n\t - B : ").append("0");
            builder.append("\r\n\t - C : ").append(getAllCandidates(optionals.getcFieldCandidates()));
            builder.append("\r\n\t - D : ").append(getAllCandidates(optionals.getdFieldCandidates()));
            builder.append("\r\n\t - E : ").append(getAllCandidates(optionals.geteFieldCandidates()));
            builder.append("\r\n\t - F : ").append(getAllCandidates(optionals.getfFieldCandidates()));
            builder.append("\r\n\t - Unit : ").append(getAllCandidates(optionals.getBaseUnitCandidates()));
            builder.append("\r\n\t - Interval : ").append(getAllCandidates(optionals.getIntervalSeconds()));
            return builder.toString();
        }

        private String getAllCandidates(Candidates<Integer> integerCandidates) {
            if(integerCandidates != null && !integerCandidates.getAllCandidates().isEmpty()){
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<Integer> iterator = integerCandidates.getAllCandidates().iterator();
                boolean next = iterator.hasNext();
                while(next){
                    stringBuilder.append(iterator.next());
                    if (next = iterator.hasNext()) {
                        stringBuilder.append(", ");
                    }
                }
                return stringBuilder.toString();
            }
            return "no candidates";
        }
    }

    private static class OptionalCollector {

        private Candidates<Integer> intervalSeconds;
        private Candidates<Integer> aFieldCandidates;
        private Candidates<Integer> cFieldCandidates;
        private Candidates<Integer> dFieldCandidates;
        private Candidates<Integer> eFieldCandidates;
        private Candidates<Integer> fFieldCandidates;
        private Candidates<Integer> baseUnitCandidates;

        public Candidates<Integer> getIntervalSeconds() {
            return intervalSeconds;
        }

        public Candidates<Integer> getaFieldCandidates() {
            return aFieldCandidates;
        }

        public Candidates<Integer> getcFieldCandidates() {
            return cFieldCandidates;
        }

        public Candidates<Integer> getdFieldCandidates() {
            return dFieldCandidates;
        }

        public Candidates<Integer> geteFieldCandidates() {
            return eFieldCandidates;
        }

        public Candidates<Integer> getfFieldCandidates() {
            return fFieldCandidates;
        }

        public Candidates<Integer> getBaseUnitCandidates() {
            return baseUnitCandidates;
        }

        private void setIntervalSeconds(Candidates<Integer> intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        private void setaFieldCandidates(Candidates<Integer> aFieldCandidates) {
            this.aFieldCandidates = aFieldCandidates;
        }

        private void setcFieldCandidates(Candidates<Integer> cFieldCandidates) {
            this.cFieldCandidates = cFieldCandidates;
        }

        private void setdFieldCandidates(Candidates<Integer> dFieldCandidates) {
            this.dFieldCandidates = dFieldCandidates;
        }

        private void seteFieldCandidates(Candidates<Integer> eFieldCandidates) {
            this.eFieldCandidates = eFieldCandidates;
        }

        private void setfFieldCandidates(Candidates<Integer> fFieldCandidates) {
            this.fFieldCandidates = fFieldCandidates;
        }

        private void setBaseUnitCandidates(Candidates<Integer> baseUnitCandidates) {
            this.baseUnitCandidates = baseUnitCandidates;
        }
    }
}
