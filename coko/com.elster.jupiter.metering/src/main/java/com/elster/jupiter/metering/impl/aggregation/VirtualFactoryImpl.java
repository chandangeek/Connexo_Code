package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link VirtualFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (15:53)
 */
@SuppressWarnings("unused")
public class VirtualFactoryImpl implements VirtualFactory {

    private final Map<MeterActivation, VirtualFactory> factoriesPerMeterActivation = new HashMap<>();
    private VirtualFactory currentFactory = new NoCurrentMeterActivation();

    @Override
    public VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
        return this.currentFactory.requirementFor(mode, requirement, deliverable, readingType);
    }

    @Override
    public List<VirtualReadingTypeRequirement> allRequirements() {
        return this.collectFromActualFactories(VirtualFactory::allRequirements);
    }

    private <R> List<R> collectFromActualFactories(java.util.function.Function<VirtualFactory, ? extends List<? extends R>> mapper) {
        return this.factoriesPerMeterActivation
                .values()
                .stream()
                .flatMap(each -> mapper.apply(each).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void nextMeterActivation(MeterActivation meterActivation, Range<Instant> requestedPeriod) {
        MeterActivationFactory factory = new MeterActivationFactory(meterActivation, requestedPeriod, this.currentFactory.meterActivationSequenceNumber() + 1);
        this.factoriesPerMeterActivation.put(meterActivation, factory);
        this.currentFactory = factory;
    }

    @Override
    public int meterActivationSequenceNumber() {
        return this.currentFactory.meterActivationSequenceNumber();
    }

    /**
     * Models the null VirtualFactory.
     * Does state validation and provides the initial meter activation sequence number.
     */
    private class NoCurrentMeterActivation implements VirtualFactory {
        @Override
        public VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
            throw new IllegalStateException("You need to set a current MeterActivation first");
        }

        @Override
        public List<VirtualReadingTypeRequirement> allRequirements() {
            throw new UnsupportedOperationException("Parent class should not be delegating this");
        }

        @Override
        public void nextMeterActivation(MeterActivation meterActivation, Range<Instant> requestedPeriod) {
            throw new UnsupportedOperationException("Parent class should not be delegating this");
        }

        @Override
        public int meterActivationSequenceNumber() {
            return 0;
        }

    }

    /**
     * Provides an implementation for the {@link VirtualFactory} interface
     * that works for a single {@link MeterActivation}.
     */
    private class MeterActivationFactory implements VirtualFactory {
        private final MeterActivation meterActivation;
        private final Range<Instant> requestedPeriod;
        private final int sequenceNumber;
        private final Map<ReadingTypeRequirement, MeterActivationAndRequirementFactory> factoriesPerRequirement = new HashMap<>();

        private MeterActivationFactory(MeterActivation meterActivation, Range<Instant> requestedPeriod, int sequenceNumber) {
            super();
            this.meterActivation = meterActivation;
            this.sequenceNumber = sequenceNumber;
            this.requestedPeriod = requestedPeriod;
        }

        @Override
        public VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
            return this.factoriesPerRequirement
                    .computeIfAbsent(requirement, key -> new MeterActivationAndRequirementFactory(this))
                    .requirementFor(mode, requirement, deliverable, readingType);
        }

        @Override
        public List<VirtualReadingTypeRequirement> allRequirements() {
            return this.factoriesPerRequirement
                    .values()
                    .stream()
                    .flatMap(each -> each.allRequirements().stream())
                    .collect(Collectors.toList());
        }

        @Override
        public void nextMeterActivation(MeterActivation meterActivation, Range<Instant> requestedPeriod) {
            throw new UnsupportedOperationException("Parent class should not be delegating this");
        }

        @Override
        public int meterActivationSequenceNumber() {
            return this.sequenceNumber;
        }

        private MeterActivation getMeterActivation() {
            return meterActivation;
        }

        private Range<Instant> getRequestedPeriod() {
            return this.requestedPeriod;
        }

    }

    /**
     * Supports the {@link VirtualFactory} interface and
     * focusses on a single {@link ReadingTypeRequirement}
     * in the context of a single {@link MeterActivation}.
     */
    private class MeterActivationAndRequirementFactory {
        private final MeterActivationFactory parent;
        private final Map<VirtualReadingType, MeterActivationAndRequirementInDeliverableFactory> requirements = new HashMap<>();

        private MeterActivationAndRequirementFactory(MeterActivationFactory parent) {
            super();
            this.parent = parent;
        }

        public VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
            return this.requirements
                    .computeIfAbsent(readingType, key -> new MeterActivationAndRequirementInDeliverableFactory(this))
                    .requirementFor(mode, requirement, deliverable, readingType);
        }

        public List<VirtualReadingTypeRequirement> allRequirements() {
            return this.requirements
                    .values()
                    .stream()
                    .flatMap(factory -> factory.allRequirements().stream())
                    .collect(Collectors.toList());
        }

        int meterActivationSequenceNumber() {
            return this.parent.meterActivationSequenceNumber();
        }

        private MeterActivation getMeterActivation() {
            return this.parent.getMeterActivation();
        }

        private Range<Instant> getRequestedPeriod() {
            return this.parent.getRequestedPeriod();
        }
    }

    /**
     * Supports the {@link VirtualFactory} interface and
     * focusses on a single {@link ReadingTypeRequirement}
     * in the context of a {@link ReadingTypeDeliverable}
     * for a single {@link MeterActivation}.
     */
    private class MeterActivationAndRequirementInDeliverableFactory {
        private final MeterActivationAndRequirementFactory parent;
        private final Map<ReadingTypeDeliverable, VirtualReadingTypeRequirement> requirements = new HashMap<>();

        private MeterActivationAndRequirementInDeliverableFactory(MeterActivationAndRequirementFactory parent) {
            super();
            this.parent = parent;
        }

        public VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
            return this.requirements.computeIfAbsent(
                    deliverable,
                    key -> this.newRequirement(mode, requirement, deliverable, readingType));
        }

        private VirtualReadingTypeRequirement newRequirement(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType) {
            return new VirtualReadingTypeRequirement(
                    mode,
                    requirement,
                    deliverable,
                    requirement.getMatchingChannelsFor(this.parent.getMeterActivation()),
                    readingType,
                    this.parent.getMeterActivation(),
                    this.parent.getRequestedPeriod(),
                    this.parent.meterActivationSequenceNumber());
        }

        public List<VirtualReadingTypeRequirement> allRequirements() {
            return new ArrayList<>(this.requirements.values());
        }

    }

}