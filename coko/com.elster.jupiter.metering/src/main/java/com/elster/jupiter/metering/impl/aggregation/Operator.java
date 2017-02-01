/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;

/**
 * Models the supported mathematical operators that can be used in {@link ServerExpressionNode}s.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19
 */
enum Operator {
    PLUS {
        @Override
        public void appendSqlOperatorTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }

        @Override
        public void appendSqlOperatorTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" + ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            // Addition is commutative
            return this.node(rightOperand, leftOperand);
        }

        @Override
        public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
            IntermediateDimension intermediateDimension = leftOperand.getIntermediateDimension().add(rightOperand.getIntermediateDimension());
            return new OperationNode(this, intermediateDimension, leftOperand, rightOperand);
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
        public void appendSqlOperatorTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }

        @Override
        public void appendSqlOperatorTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" - ");
        }

        @Override
        public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
            IntermediateDimension intermediateDimension = leftOperand.getIntermediateDimension().substract(rightOperand.getIntermediateDimension());
            return new OperationNode(this, intermediateDimension, leftOperand, rightOperand);
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
        public void appendSqlOperatorTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }

        @Override
        public void appendSqlOperatorTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" * ");
        }

        @Override
        public ServerExpressionNode node(ServerExpressionNode leftOperand, BigDecimal rightOperand) {
            // Multiplication is commutative
            return this.node(rightOperand, leftOperand);
        }

        @Override
        public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
            IntermediateDimension intermediateDimension =
                    leftOperand.getIntermediateDimension()
                            .multiply(rightOperand.getIntermediateDimension().getDimension().orElse(Dimension.DIMENSIONLESS));
            return new OperationNode(this, intermediateDimension, leftOperand, rightOperand);
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
        public void appendSqlOperatorTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }

        @Override
        public void appendSqlOperatorTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(" / ");
        }

        @Override
        public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
            IntermediateDimension intermediateDimension =
                    leftOperand.getIntermediateDimension()
                            .divide(rightOperand.getIntermediateDimension().getDimension().orElse(Dimension.DIMENSIONLESS));
            return new OperationNode(this, intermediateDimension, leftOperand, rightOperand);
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
    },
    SAFE_DIVIDE {
        @Override
        public void appendTo(SqlBuilder sqlBuilder, SqlFragment operand1, SqlFragment operand2, SqlFragment zeroReplacementOperand) {
            sqlBuilder.add(operand1);
            this.appendSqlOperatorTo(sqlBuilder);
            sqlBuilder.append("decode(");
            sqlBuilder.add(operand2);
            sqlBuilder.append(", 0, ");
            sqlBuilder.add(zeroReplacementOperand);
            sqlBuilder.append(", ");
            sqlBuilder.add(operand2);
            sqlBuilder.append(")");
        }

        @Override
        public void appendSqlOperatorTo(SqlBuilder sqlBuilder) {
            DIVIDE.appendSqlOperatorTo(sqlBuilder);
        }

        @Override
        public void appendTo(StringBuilder builder, String operand1, String operand2, String zeroReplacementOperand) {
            builder.append(operand1);
            this.appendSqlOperatorTo(builder);
            builder.append("decode(");
            builder.append(operand2);
            builder.append(", 0, ");
            builder.append(zeroReplacementOperand);
            builder.append(", ");
            builder.append(operand2);
            builder.append(")");
        }

        @Override
        public void appendSqlOperatorTo(StringBuilder sqlBuilder) {
            DIVIDE.appendSqlOperatorTo(sqlBuilder);
        }

        @Override
        public OperationNode safeNode(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand, ServerExpressionNode safeOperand) {
            IntermediateDimension intermediateDimension =
                    leftOperand.getIntermediateDimension()
                            .divide(rightOperand.getIntermediateDimension().getDimension().orElse(Dimension.DIMENSIONLESS));
            return new OperationNode(this, intermediateDimension, leftOperand, rightOperand, safeOperand);
        }

        @Override
        public OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand) {
            throw new UnsupportedOperationException("Must specify safe operand, use com.elster.jupiter.metering.impl.aggregation.Operator.safeNode instead");
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

    public void appendTo(SqlBuilder sqlBuilder, SqlFragment operand1, SqlFragment operand2, SqlFragment zeroReplacementOperand) {
        sqlBuilder.add(operand1);
        this.appendSqlOperatorTo(sqlBuilder);
        sqlBuilder.add(operand2);
    }

    ;

    protected abstract void appendSqlOperatorTo(SqlBuilder sqlBuilder);

    public void appendTo(StringBuilder builder, String operand1, String operand2, String zeroReplacementOperand) {
        builder.append(operand1);
        this.appendSqlOperatorTo(builder);
        builder.append(operand2);
    }

    protected abstract void appendSqlOperatorTo(StringBuilder builder);

    /**
     * Returns an {@link OperationNode} that applies this Operator
     * to the specified left and right operands.
     *
     * @param leftOperand The left operand
     * @param rightOperand The right operand
     * @return The OperationNodeImpl
     */
    public abstract OperationNode node(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand);

    /**
     * Returns a {@link OperationNode safe division node} for
     * the specified left and right operands.
     * Note that this will throw an UnsupportedOperationException
     * on all operators except {@link #SAFE_DIVIDE}.
     *
     * @param leftOperand The left operand
     * @param rightOperand The right operand
     * @return The OperationNodeImpl
     */
    public OperationNode safeNode(ServerExpressionNode leftOperand, ServerExpressionNode rightOperand, ServerExpressionNode safeOperand) {
        throw new UnsupportedOperationException("Reserved for SAFE_DIVIDE");
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

    public ServerExpressionNode node(BigDecimal leftOperand, BigDecimal rightOperand) {
        return this.node(leftOperand, new NumericalConstantNode(rightOperand));
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
            case SAFE_DIVIDE: {
                return SAFE_DIVIDE;
            }
            default: {
                throw new IllegalArgumentException("Unsupported operator: " + operator.name());
            }
        }
    }

}