package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.List;

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
    AVG;

    public static Function from(com.elster.jupiter.metering.impl.config.Function function) {
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
            default: {
                throw new IllegalArgumentException("Unsupported function: " + function.name());
            }
        }
    }

    public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        // All currently known functions support only 1 argument
        if (arguments.size() != 1) {
            throw new IllegalArgumentException(this.name() + " takes exactly 1 argument but got " + arguments.size());
        }
        sqlBuilder.append(this.name());
        sqlBuilder.append("(");
        sqlBuilder.add(arguments.get(0));
        sqlBuilder.append(")");
    }

}
