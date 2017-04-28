/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Models the fields that define the start of the gas year
 * as specified by {@link GasDayOptions} to support SQL generation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-20 (11:34)
 */
enum GasStartField {
    HOUR {
        @Override
        void appendValueTo(MathematicalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder) {
            sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getHour()));
        }
    },
    MONTH {
        @Override
        void appendValueTo(MathematicalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder) {
            if (MathematicalOperation.PLUS.equals(operation)) {
                sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getMonthValue() - 1));
            } else {
                sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getMonthValue()));
            }
        }
    };

    abstract void appendValueTo(MathematicalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder);

    void appendUnitTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.name());
    }
}