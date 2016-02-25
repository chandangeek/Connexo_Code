package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;

/**
 * Models the functions that can be used to aggregate energy values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-25 (12:45)
 */
enum AggregationFunction {

    /**
     * Aggregates values of volume related {@link ReadingType}s.
     */
    SUM,

    /**
     * Aggregates values of flow related {@link ReadingType}s.
     */
    AVG,

    MIN,
    MAX,

    /**
     * Truncates localdate values
     */
    TRUNC,

    /**
     * Aggregates flags that are bitwise encoded in long values.
     */
    BIT_OR;

    static AggregationFunction from(ReadingType readingType) {
        /* Todo: consider the unit of the ReadingType
         *       flow units will use AVG
         *       volume units will use SUM
         */
        return SUM;
    }

    String sqlName() {
        return this.name();
    }

}