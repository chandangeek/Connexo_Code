package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    String sqlName() {
        return this.name();
    }

    public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append("(");
        Iterator<SqlFragment> iterator = arguments.iterator();
        while (iterator.hasNext()) {
            SqlFragment sqlFragment = iterator.next();
            sqlBuilder.add(sqlFragment);
            if (iterator.hasNext()) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(")");
    }

    public void appendTo(StringBuilder sqlBuilder, List<String> arguments) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append("(");
        sqlBuilder.append(arguments.stream().collect(Collectors.joining(", ")));
        sqlBuilder.append(")");
    }

}