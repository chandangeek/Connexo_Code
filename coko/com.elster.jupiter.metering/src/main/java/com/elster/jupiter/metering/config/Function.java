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
 *     <td>SUM</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>MAX</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * <tr>
 *     <td>MIN</td>
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
 *     <td>FIRST_NOT_NULL</td>
 *     <td>no</td>
 *     <td>yes</td>
 * </tr>
 * </table>
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    SUM,
    MAX,
    MIN,
    AVG,
    /**
     * Aggregates the single timeseries argument to the
     * interval length of the {@link ReadingTypeDeliverable}.
     */
    AGG_TIME {
        @Override
        public String toString() {
            return "agg";
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

}