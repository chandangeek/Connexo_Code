package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the supported functions that can be used in {@link FunctionCallNode}s.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19
 */
public enum Function {
    SUM,
    MAX,
    MIN,
    AVG,
    AGG_TIME,
    COALESCE;

    public static Function from(com.elster.jupiter.metering.config.Function function) {
        switch (function) {
            case SUM: {
                return SUM;
            }
            case MAX: {
                return MAX;
            }
            case MIN: {
                return MIN;
            }
            case AVG: {
                return AVG;
            }
            case AGG_TIME: {
                return AGG_TIME;
            }
            case FIRST_NOT_NULL: {
                return COALESCE;
            }
            default: {
                throw new IllegalArgumentException("Unsupported function: " + function.name());
            }
        }
    }

    public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        sqlBuilder.append(this.name());
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
        sqlBuilder.append(this.name());
        sqlBuilder.append("(");
        sqlBuilder.append(arguments.stream().collect(Collectors.joining(", ")));
        sqlBuilder.append(")");
    }

}
