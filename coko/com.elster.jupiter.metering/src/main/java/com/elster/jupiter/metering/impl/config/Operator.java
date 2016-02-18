package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Models the supported mathematical operators that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-04
 */
public enum Operator {
    PLUS(1) {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }
    },
    MINUS(2) {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }
    },
    MULTIPLY(3) {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }
    },
    DIVIDE(4) {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }
    };

    private final int id;

    Operator(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract void appendTo(SqlBuilder sqlBuilder);

}