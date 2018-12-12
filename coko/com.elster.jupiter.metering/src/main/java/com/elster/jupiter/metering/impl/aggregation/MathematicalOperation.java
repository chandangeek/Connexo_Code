/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Models mathematical operations to support SQL generation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-20 (11:32)
 */
enum MathematicalOperation {
    PLUS {
        @Override
        void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }
    },
    MINUS {
        @Override
        void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }
    };

    abstract void appendTo(SqlBuilder sqlBuilder);
}