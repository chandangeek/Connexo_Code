/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SUM {
        @Override
        BigDecimal applyTo(BigDecimal... values) {
            return Stream.of(values).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    },

    /**
     * Aggregates values of flow related {@link ReadingType}s.
     */
    AVG {
        @Override
        BigDecimal applyTo(BigDecimal... values) {
            if (values.length == 0) {
                return BigDecimal.ZERO;
            } else {
                long count = Stream.of(values).count();
                return SUM.applyTo(values).divide(BigDecimal.valueOf(count), 6, BigDecimal.ROUND_HALF_UP);
            }
        }
    },

    MIN {
        @Override
        BigDecimal applyTo(BigDecimal... values) {
            return Stream.of(values).min(BigDecimal::compareTo).get();
        }
    },
    MAX {
        @Override
        BigDecimal applyTo(BigDecimal... values) {
            return Stream.of(values).max(BigDecimal::compareTo).get();
        }
    },

    /**
     * Truncates localdate values
     */
    TRUNC {
        @Override
        BigDecimal applyTo(BigDecimal... values) {
            throw new UnsupportedOperationException("LocalDate is not compatible with BigDecimal");
        }
    },

    /**
     * Aggregates flags that are bitwise encoded in long values.
     */
    AGGREGATE_FLAGS {
        @Override
        String sqlName() {
            return "aggFlags";
        }

        @Override
        public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
            sqlBuilder.append(this.sqlName());
            sqlBuilder.append("(cast(collect(distinct ");
            this.appendSqlFragments(sqlBuilder, arguments);
            sqlBuilder.append(") as Flags_Array))");
        }

        @Override
        public void appendTo(StringBuilder stringBuilder, List<String> arguments) {
            stringBuilder.append(this.sqlName());
            stringBuilder.append("(cast(collect(distinct ");
            this.appendStringArguments(stringBuilder, arguments);
            stringBuilder.append(") as Flags_Array))");
        }

        @Override
        BigDecimal applyTo(BigDecimal... values) {
            throw new UnsupportedOperationException("Aggregation flags are not compatible with BigDecimal");
        }
    };


    static AggregationFunction from(com.elster.jupiter.metering.config.Function function) {
        switch (function) {
            case AVG: {
                return AggregationFunction.AVG;
            }
            case SUM: {
                return AggregationFunction.SUM;
            }
            case MIN_AGG: {
                return AggregationFunction.MIN;
            }
            case MAX_AGG: {
                return AggregationFunction.MAX;
            }
            case MAX:   // Intentional fall-through
            case MIN:   // Intentional fall-through
            case AGG_TIME:   // Intentional fall-through
            case FIRST_NOT_NULL:   // Intentional fall-through
            default: {
                throw new IllegalArgumentException("Unsupported aggregation function " + function.name());
            }
        }
    }

    String sqlName() {
        return this.name();
    }

    public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append("(");
        this.appendSqlFragments(sqlBuilder, arguments);
        sqlBuilder.append(")");
    }

    protected void appendSqlFragments(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        Iterator<SqlFragment> iterator = arguments.iterator();
        while (iterator.hasNext()) {
            SqlFragment sqlFragment = iterator.next();
            sqlBuilder.add(sqlFragment);
            if (iterator.hasNext()) {
                sqlBuilder.append(", ");
            }
        }
    }

    public void appendTo(StringBuilder stringBuilder, List<String> arguments) {
        stringBuilder.append(this.sqlName());
        stringBuilder.append("(");
        this.appendStringArguments(stringBuilder, arguments);
        stringBuilder.append(")");
    }

    protected void appendStringArguments(StringBuilder stringBuilder, List<String> arguments) {
        stringBuilder.append(arguments.stream().collect(Collectors.joining(", ")));
    }

    abstract BigDecimal applyTo(BigDecimal... values);

}