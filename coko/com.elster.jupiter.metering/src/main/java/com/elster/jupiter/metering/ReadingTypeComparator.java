/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Function;

public class ReadingTypeComparator implements Comparator<ReadingType> {

    private static final ReadingTypeComparator INSTANCE = new ReadingTypeComparator();

    public enum Attribute {
        MacroPeriod(ReadingType::getMacroPeriod),
        Aggregate(ReadingType::getAggregate),
        MeasuringPeriod(ReadingType::getMeasuringPeriod),
        Accumulation(ReadingType::getAccumulation),
        FlowDirection(ReadingType::getFlowDirection),
        Commodity(ReadingType::getCommodity),
        MeasurementKind(ReadingType::getMeasurementKind),
        InterharmonicNumerator(readingType -> readingType.getInterharmonic().getNumerator()),
        InterharmonicDenominator(readingType -> readingType.getInterharmonic().getDenominator()),
        ArgumentNumerator(readingType -> readingType.getArgument().getNumerator()),
        ArgumentDenominator(readingType -> readingType.getArgument().getDenominator()),
        TOU(ReadingType::getTou),
        CPP(ReadingType::getCpp),
        ConsumptionTier(ReadingType::getConsumptionTier),
        Phase(ReadingType::getPhases),
        Multiplier(ReadingType::getMultiplier),
        Unit(ReadingType::getUnit),
        Currency(readingType -> readingType.getCurrency().getNumericCode());

        private final Function<ReadingType, Comparable> attributeExtractor;

        Attribute(Function<ReadingType, Comparable> attributeExtractor) {
            this.attributeExtractor = attributeExtractor;
        }

        private Function<ReadingType, Comparable> getAttributeExtractor() {
            return attributeExtractor;
        }
    }

    private final EnumSet<Attribute> attributesToCompare = EnumSet.allOf(Attribute.class);

    private ReadingTypeComparator() {
    }

    public static ReadingTypeComparator instance() {
        return INSTANCE;
    }

    public static ReadingTypeComparator ignoring(Attribute... ignoredAttributes) {
        ReadingTypeComparator readingTypeComparator = new ReadingTypeComparator();
        readingTypeComparator.attributesToCompare.removeAll(Arrays.asList(ignoredAttributes));
        return readingTypeComparator;
    }

    @Override
    public int compare(ReadingType rt1, ReadingType rt2) {
        if (rt1.equals(rt2)) {
            return 0;
        }
        Comparator<ReadingType> comparator = Comparator.comparing(readingType -> 0);
        for (Attribute attribute : this.attributesToCompare) {
            Function<ReadingType, Comparable> attributeExtractor = readingType -> attribute.getAttributeExtractor().apply(readingType);
            comparator = comparator.thenComparing(attributeExtractor);
        }
        return comparator.compare(rt1, rt2);
    }
}
