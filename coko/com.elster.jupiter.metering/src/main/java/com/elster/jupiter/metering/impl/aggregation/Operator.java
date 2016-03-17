package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.math.BigDecimal;

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

        @Override
        public void appendTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            // Addition is commutative
            return this.node(rightOperand, leftOperand);
        }

        @Override
        public ServerExpressionNode node(BigDecimal leftOperand, ServerExpressionNode rightOperand) {
            if (BigDecimal.ZERO.compareTo(leftOperand) == 0) {
                return rightOperand;
            } else {
                return super.node(leftOperand, rightOperand);
            }
        }
    },
    MINUS {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }

        @Override
        public void appendTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            if (BigDecimal.ZERO.compareTo(rightOperand) == 0) {
                return leftOperand;
            } else {
                return super.node(leftOperand, rightOperand);
            }
        }
    },
    MULTIPLY {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }

        @Override
        public void appendTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            // Multiplication is commutative
            return this.node(rightOperand, leftOperand);
        }

        @Override
        public ServerExpressionNode node(BigDecimal leftOperand, ServerExpressionNode rightOperand) {
            if (BigDecimal.ONE.compareTo(leftOperand) == 0) {
                return rightOperand;
            } else if (BigDecimal.ZERO.compareTo(leftOperand) == 0) {
                return new NumericalConstantNode(BigDecimal.ZERO);
            } else {
                return super.node(leftOperand, rightOperand);
            }
        }
    },
    DIVIDE {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }

        @Override
        public void appendTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            if (BigDecimal.ZERO.compareTo(rightOperand) == 0) {
                // Protect against division by zero on database level
                throw new ArithmeticException("Division by zero");
            } else if (BigDecimal.ONE.compareTo(rightOperand) == 0) {
                return leftOperand;
            } else {
                return super.node(leftOperand, rightOperand);
            }
        }

        @Override
        public ServerExpressionNode node(BigDecimal leftOperand, ServerExpressionNode rightOperand) {
            if (BigDecimal.ZERO.compareTo(leftOperand) == 0) {
                // Will always produce zero, whatever the right operand is
                return new NumericalConstantNode(BigDecimal.ZERO);
            } else {
                return super.node(leftOperand, rightOperand);
            }
        }
    };

    public abstract void appendTo(SqlBuilder sqlBuilder);

    public abstract void appendTo(StringBuilder sqlBuilder);

    /**
     * Returns an {@link OperationNode} that applies this Operator
     * to the specified left and right operands.
     *
     * @param leftOperand The left operand
     * @param rightOperand The right operand
     * @return The OperationNodeImpl
     */
    public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
        return new OperationNode(this, leftOperand, rightOperand);
    }

    /**
     * Returns an {@link OperationNode} that applies this Operator
     * to the specified left operand and the specified numerical constant.
     * Note that specific operators may optimize this and ignore
     * the right operand if it represents the unity value for that operator.
     *
     * @param leftOperand The left operand
     * @param rightOperand The numerical constant
     * @return The OperationNodeImpl
     */
    public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
        return this.node(leftOperand, new NumericalConstantNode(rightOperand));
    }

    /**
     * Returns an {@link OperationNode} that applies this Operator
     * to the specified numerical constant and the specified right operand.
     * Note that specific operators may optimize this and ignore
     * the left operand if it represents the unity value for that operator.
     *
     * @param leftOperand The numerical constant
     * @param rightOperand The right operand
     * @return The OperationNodeImpl
     */
    public ServerExpressionNode node(BigDecimal leftOperand, ServerExpressionNode rightOperand) {
        return this.node(new NumericalConstantNode(leftOperand), rightOperand);
    }

    public static Operator from(com.elster.jupiter.metering.config.Operator operator) {
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