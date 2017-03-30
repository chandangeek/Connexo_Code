/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models the supported functions that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 * The usage of the functions are limited as described below
 * <table>
 * <tr>
 *     <th>Function</th>
 *     <th>Auto mode</th>
 *     <th>Expert mode</th>
 * </tr>
 * <tr>
 *     <td>MIN</td>
 *     <td>yes</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>MAX</td>
 *     <td>yes</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>MIN_AGG</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>MAX_AGG</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>SUM</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>AVG</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>AGG_TIME</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>SQRT</td>
 *     <td>yes</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>FIRST_NOT_NULL</td>
 *     <td>yes</td>
 *     <td>yes</td>
 * </tr>
 * </table>
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    /**
     * Aggregates metering values to the requested {@link AggregationLevel}.
     * Expects two arguments, the AggregationLevel and the expression to aggregate.
     * All values that are part of the same aggregation interval are added up.
     */
    SUM {
        @Override
        public boolean requiresAggregationLevel() {
            return true;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return false;
        }
    },
    /**
     * Calculates the maximum of all arguments.
     */
    MAX {
        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return true;
        }
    },
    /**
     * Calculates the minimum of all arguments.
     */
    MIN {
        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return true;
        }
    },
    /**
     * Aggregates metering values to the requested {@link AggregationLevel}.
     * Expects two arguments, the AggregationLevel and the expression to aggregate.
     * The maximum of all values that are part of the same aggregation interval
     * is the result of the aggregation.
     */
    MAX_AGG {
        @Override
        public String toString() {
            return "maxOf";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return true;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return false;
        }
    },
    /**
     * Aggregates metering values to the requested {@link AggregationLevel}.
     * Expects two arguments, the AggregationLevel and the expression to aggregate.
     * The minimum of all values that are part of the same aggregation interval
     * is the result of the aggregation.
     */
    MIN_AGG {
        @Override
        public String toString() {
            return "minOf";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return true;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return false;
        }
    },
    /**
     * Aggregates metering values to the requested {@link AggregationLevel}.
     * Expects two arguments, the AggregationLevel and the expression to aggregate.
     * The average of all values that are part of the same aggregation interval
     * is the result of the aggregation.
     */
    AVG {
        @Override
        public boolean requiresAggregationLevel() {
            return true;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return false;
        }
    },
    /**
     * Aggregates the single timeseries argument to the
     * interval length of the {@link ReadingTypeDeliverable}.
     */
    AGG_TIME {
        @Override
        public String toString() {
            return "agg";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return false;
        }
    },
    /**
     * Returns x to the power of y.
     */
    POWER {
        @Override
        public String toString() {
            return "power";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return true;
        }
    },
    /**
     * Returns the square root of the single argument that is passed to this function.
     */
    SQRT {
        @Override
        public String toString() {
            return "sqrt";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return true;
        }
    },
    /**
     * Returns the first not null argument that is passed to this function.
     */
    FIRST_NOT_NULL {
        @Override
        public String toString() {
            return "firstNotNull";
        }

        @Override
        public boolean requiresAggregationLevel() {
            return false;
        }

        @Override
        protected boolean supportedByAutoMode() {
            return true;
        }
    };

    public String toString() {
        return this.name().toLowerCase();
    }

    public static Set<String> names() {
        return Stream.of(values()).map(Function::toString).collect(Collectors.toSet());
    }

    public static Optional<Function> from(String name) {
        return Stream.of(values()).filter(each -> each.toString().equals(name)).findFirst();
    }

    /**
     * Tests if this Function requires an argument of type {@link AggregationLevel}.
     *
     * @return A flag that indicates if this Function requires an AggregationLevel argument
     */
    public abstract boolean requiresAggregationLevel();

    /**
     * Tests if this Function is supported by the specified {@link Formula.Mode}.
     *
     * @param mode The Mode
     * @return A flag that indicates if this Function is supported by the Mode
     */
    public boolean supportedBy(Formula.Mode mode) {
        return Formula.Mode.EXPERT.equals(mode) || this.supportedByAutoMode();
    }

    protected abstract boolean supportedByAutoMode();

}