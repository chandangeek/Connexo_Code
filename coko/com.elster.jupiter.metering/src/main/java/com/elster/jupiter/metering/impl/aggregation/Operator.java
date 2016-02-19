package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Models the supported mathematical operators that can be used in {@link ServerExpressionNode}s.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19
 */
public enum Operator {
    PLUS {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }
    },
    MINUS {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }
    },
    MULTIPLY {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }
    },
    DIVIDE {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }
    };

    public abstract void appendTo(SqlBuilder sqlBuilder);

    public static Operator from(com.elster.jupiter.metering.impl.config.Operator operator) {
        switch (operator) {
            case PLUS: {
                return PLUS;
            }
            case MINUS: {
                return MINUS;
            }
            case MULTIPLY: {
                return MULTIPLY;
            }
            case DIVIDE: {
                return DIVIDE;
            }
            default: {
                throw new IllegalArgumentException("Unsupported operator: " + operator.name());
            }
        }
    }

}