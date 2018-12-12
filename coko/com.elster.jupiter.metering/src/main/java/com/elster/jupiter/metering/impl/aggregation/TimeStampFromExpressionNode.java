/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * and returns a SQL construct (as String) that provides the UTC timestamp for the visited
 * {@link ExpressionNode} or <code>0</code> if the ExpressionNode cannot provide a UTC timestamp.
 * A {@link NumericalConstantNode} is a good example of that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
class TimeStampFromExpressionNode implements ServerExpressionNode.Visitor<Void> {

    private ValueHolderWithPriority value = new Null();

    String getSqlName() {
        return this.value.get();
    }

    @Override
    public Void visitConstant(NumericalConstantNode constant) {
        this.value = this.value.accept(new Null());
        return null;
    }

    @Override
    public Void visitConstant(StringConstantNode constant) {
        this.value = this.value.accept(new Null());
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        ValueHolderWithPriority newValue;
        if (property.getCustomPropertySet().isVersioned()) {
            newValue = new CustomProperty(property.sqlName() + ".starttime");
        } else {
            newValue = new Null();
        }
        this.value = this.value.accept(newValue);
        return null;
    }

    @Override
    public Void visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        this.value = this.value.accept(new TimeSeries(slp.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName()));
        return null;
    }

    @Override
    public Void visitSqlFragment(SqlFragmentNode variable) {
        this.value = this.value.accept(new Null());
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        this.value = this.value.accept(new Null());
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        this.value = this.value.accept(new TimeSeries(deliverable.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName()));
        return null;
    }

    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        this.value = this.value.accept(new TimeSeries(requirement.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName()));
        return null;
    }

    @Override
    public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getExpressionNode().accept(this);
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        this.visitAll(Arrays.asList(
                operationNode.getLeftOperand(),
                operationNode.getRightOperand()));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        this.visitAll(functionCall.getArguments());
        return null;
    }

    private void visitAll(List<ServerExpressionNode> children) {
        children.forEach(child -> child.accept(this));
    }

    @Override
    public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

    private interface ValueHolderWithPriority {
        String get();

        /**
         * Start of double dispatch mechanism,
         * receiver will call acceptXXX method on the newValue
         * depending on its type.
         *
         * @param newValue The new ValueHolderWithPriority
         * @return The value with highest prioriy
         */
        ValueHolderWithPriority accept(ValueHolderWithPriority newValue);

        /**
         * Second phase in double dispath,
         * the receiver now knows the value is
         * being updated from a TimeSeries.
         *
         * @param newValue The value extracted from a node containing a TimeSeries
         * @return The value that has priority
         */
        ValueHolderWithPriority acceptTimeSeries(ValueHolderWithPriority newValue);

        /**
         * Second phase in double dispath,
         * the receiver now knows the value is
         * being updated from a TimeSeries.
         *
         * @param newValue The value extracted from a CustomPropertyNode
         * @return The value that has priority
         */
        ValueHolderWithPriority acceptCustomProperty(ValueHolderWithPriority newValue);
    }

    private static class Null implements ValueHolderWithPriority {
        @Override
        public String get() {
            return null;
        }

        @Override
        public ValueHolderWithPriority accept(ValueHolderWithPriority newValue) {
            // Everything else has priority over the null value so shortcut double dispath
            return newValue;
        }

        @Override
        public ValueHolderWithPriority acceptTimeSeries(ValueHolderWithPriority newValue) {
            // Everything else has priority over the null value
            return newValue;
        }

        @Override
        public ValueHolderWithPriority acceptCustomProperty(ValueHolderWithPriority newValue) {
            // Everything else has priority over the null value
            return newValue;
        }
    }

    private static class TimeSeries implements ValueHolderWithPriority {
        private final String sqlName;

        private TimeSeries(String sqlName) {
            this.sqlName = sqlName;
        }

        @Override
        public String get() {
            return this.sqlName;
        }

        @Override
        public ValueHolderWithPriority accept(ValueHolderWithPriority newValue) {
            return newValue.acceptTimeSeries(this);
        }

        @Override
        public ValueHolderWithPriority acceptTimeSeries(ValueHolderWithPriority newValue) {
            /* First TimeSeries wins (backwards compatibility)
             * Remember that this is the second call in the double dispatch mechanism
             * so newValue is actually the old value that dispatched the call
             * passing itself as an method parameter. */
            return newValue;
        }

        @Override
        public ValueHolderWithPriority acceptCustomProperty(ValueHolderWithPriority newValue) {
            // TimeSeries has more valuable time line information so that one has priority
            return this;
        }
    }

    private static class CustomProperty implements ValueHolderWithPriority {
        private final String sqlName;

        private CustomProperty(String sqlName) {
            this.sqlName = sqlName;
        }

        @Override
        public String get() {
            return this.sqlName;
        }

        @Override
        public ValueHolderWithPriority accept(ValueHolderWithPriority newValue) {
            return newValue.acceptCustomProperty(this);
        }

        @Override
        public ValueHolderWithPriority acceptTimeSeries(ValueHolderWithPriority newValue) {
            // TimeSeries has more valuable time line information so that one has priority
            return newValue;
        }

        @Override
        public ValueHolderWithPriority acceptCustomProperty(ValueHolderWithPriority newValue) {
            /* First CustomProperty wins (backwards compatibility)
             * Remember that this is the second call in the double dispatch mechanism
             * so newValue is actually the old value that dispatched the call
             * passing itself as an method parameter. */
            return newValue;
        }
    }

}