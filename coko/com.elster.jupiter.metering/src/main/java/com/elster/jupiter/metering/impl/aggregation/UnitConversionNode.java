/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.util.units.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models a {@link ServerExpressionNode} that will
 * apply unit conversion to another ServerExpressionNode
 * and visitor components should treat this as a single unit.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-28 (09:47)
 */
class UnitConversionNode implements ServerExpressionNode {

    private final ServerExpressionNode expressionNode;
    private final VirtualReadingType sourceReadingType;
    private final IntermediateDimension intermediateDimension;
    private VirtualReadingType targetReadingType;

    UnitConversionNode(ServerExpressionNode expressionNode, Dimension dimension, VirtualReadingType targetReadingType) {
        this.expressionNode = expressionNode;
        this.intermediateDimension = IntermediateDimension.of(dimension);
        VirtualReadingType candidate = expressionNode.accept(new GetSourceReadingType(targetReadingType, dimension));
        this.sourceReadingType = VirtualReadingType.from(candidate.getIntervalLength(), dimension, candidate.getAccumulation(), candidate.getCommodity())
                .withMetricMultiplier(candidate.getUnitMultiplier());
        this.targetReadingType = targetReadingType;
    }

    UnitConversionNode(ServerExpressionNode expressionNode, VirtualReadingType sourceReadingType, VirtualReadingType targetReadingType) {
        this.intermediateDimension = expressionNode.getIntermediateDimension();
        this.expressionNode = expressionNode;
        this.sourceReadingType = sourceReadingType;
        this.targetReadingType = targetReadingType;
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return intermediateDimension;
    }

    ServerExpressionNode getExpressionNode() {
        return expressionNode;
    }

    VirtualReadingType getSourceReadingType() {
        return sourceReadingType;
    }

    VirtualReadingType getTargetReadingType() {
        return targetReadingType;
    }

    void setTargetReadingType(VirtualReadingType targetReadingType) {
        this.targetReadingType = targetReadingType;
    }

    void setTargetIntervalLength(IntervalLength intervalLength) {
        this.setTargetReadingType(this.targetReadingType.withIntervalLength(intervalLength));
    }

    void setTargetMultiplier(MetricMultiplier multiplier) {
        this.setTargetReadingType(this.targetReadingType.withMetricMultiplier(multiplier));
    }

    void setTargetCommodity(Commodity commodity) {
        this.setTargetReadingType(this.targetReadingType.withCommondity(commodity));
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnitConversion(this);
    }

    /**
     * Implementation note: extends from InferReadingType to reuse calculation
     * of preferred reading type for complex expressions (including search for compromise)
     * but avoid enforcing the calculated preferred reading type down the expression.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2016-04-28 (15:51)
     */
    private static class GetSourceReadingType extends InferReadingType {

        private final Dimension dimension;
        private final VirtualReadingType dimensionReadingType;

        private GetSourceReadingType(VirtualReadingType requestedReadingType, Dimension dimension) {
            super(requestedReadingType);
            this.dimension = dimension;
            this.dimensionReadingType = requestedReadingType.withDimension(dimension);
        }

        @Override
        public VirtualReadingType visitVirtualRequirement(VirtualRequirementNode node) {
            return node.getPreferredReadingType();
        }

        @Override
        protected Optional<VirtualReadingType> searchCompromise(List<ServerExpressionNode> nodes, List<VirtualReadingType> preferredReadingTypes) {
            List<VirtualReadingType> additionalLevels = this.addMultiples(preferredReadingTypes);
            List<VirtualReadingType> additionalLengthAndMetricMultipliers = new ArrayList<>(this.dimensionMultiples(additionalLevels));
            additionalLengthAndMetricMultipliers.addAll(additionalLevels);
            // addMultiples has also added the target interval length which is always going to be the best match
            Optional<VirtualReadingType> candidate = super.searchCompromise(nodes, this.filterTargetIntervalLength(additionalLengthAndMetricMultipliers));
            if (candidate.isPresent()) {
                return candidate;
            } else {
                return super.searchCompromise(nodes, preferredReadingTypes);
            }
        }

        @Override
        protected CheckEnforceReadingType newCheckInforceReadingType(VirtualReadingType readingType) {
            if (readingType.getDimension().equals(this.dimension)) {
                return super.newCheckInforceReadingType(readingType);
            } else {
                return new CheckEnforceReadingTypeForDimension(readingType);
            }
        }

        /**
         * Adds multiples (up to the level of the requested reading type)
         * to support intermediate aggregation of values to a common aggregation level
         * that is not necessarily the same as the target aggregation level.
         * As an example, when 15 and 30 minute data are part of a UnitConversionNode
         * that has daily values as the target interval then we need a common
         * intermediate source level for the UnitConversionNode to aggregate the values
         * to the final daily aggregation level. In that case, the common aggregation
         * level is hour but the super class is only considering 15', 30' and daily intervals.
         *
         * @param limitedList The limited list of VirtualReadingType
         * @return The extended list of VirtualReadingType that will also contain the multiples of the limited list
         */
        private List<VirtualReadingType> addMultiples(List<VirtualReadingType> limitedList) {
            IntervalLength requestedIntervalLength = this.getRequestedReadingType().getIntervalLength();
            return limitedList
                    .stream()
                    .flatMap(each -> this.multiples(each, requestedIntervalLength))
                    .distinct()
                    .collect(Collectors.toList());
        }

        private Stream<VirtualReadingType> multiples(VirtualReadingType readingType, IntervalLength requestedIntervalLength) {
            return this.multiples(readingType, IntervalLength.multiples(readingType.getIntervalLength(), requestedIntervalLength));
        }

        private Stream<VirtualReadingType> multiples(VirtualReadingType readingType, Set<IntervalLength> intervalLengths) {
            return intervalLengths
                    .stream()
                    .map(readingType::withIntervalLength);
        }

        private List<VirtualReadingType> filterTargetIntervalLength(Collection<VirtualReadingType> readingTypes) {
            IntervalLength requestedIntervalLength = this.getRequestedReadingType().getIntervalLength();
            return readingTypes
                    .stream()
                    .filter(readingType -> !readingType.getIntervalLength().equals(requestedIntervalLength))
                    .collect(Collectors.toList());
        }

        private Set<VirtualReadingType> dimensionMultiples(List<VirtualReadingType> readingTypes) {
            Set<VirtualReadingType> multiples = new HashSet<>();
            Set<IntervalLength> intervalLengths = this.uniqueIntervalLengths(readingTypes);
            Set<MetricMultiplier> metricMultipliers = this.uniqueMetricMultipliers(readingTypes);
            for (IntervalLength intervalLength : intervalLengths) {
                VirtualReadingType withIntervalLength = this.dimensionReadingType.withIntervalLength(intervalLength);
                metricMultipliers
                        .stream()
                        .map(withIntervalLength::withMetricMultiplier)
                        .forEach(multiples::add);
            }
            return multiples;
        }

        private Set<IntervalLength> uniqueIntervalLengths(List<VirtualReadingType> readingTypes) {
            Set<IntervalLength> intervalLengths = EnumSet.noneOf(IntervalLength.class);
            readingTypes
                    .stream()
                    .map(VirtualReadingType::getIntervalLength)
                    .forEach(intervalLengths::add);
            return intervalLengths;
        }

        private Set<MetricMultiplier> uniqueMetricMultipliers(List<VirtualReadingType> readingTypes) {
            Set<MetricMultiplier> metricMultipliers = EnumSet.noneOf(MetricMultiplier.class);
            readingTypes
                    .stream()
                    .map(VirtualReadingType::getUnitMultiplier)
                    .forEach(metricMultipliers::add);
            return metricMultipliers;
        }

    }

    private static class CheckEnforceReadingTypeForDimension extends CheckEnforceReadingTypeImpl {

        CheckEnforceReadingTypeForDimension(VirtualReadingType readingType) {
            super(readingType);
        }

        @Override
        public Boolean visitVirtualRequirement(VirtualRequirementNode requirement) {
            return requirement.supportsInUnitConversion(this.getReadingType());
        }
    }

}