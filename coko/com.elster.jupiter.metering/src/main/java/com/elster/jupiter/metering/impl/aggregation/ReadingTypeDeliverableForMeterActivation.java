package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

/**
 * Redefines a {@link ReadingTypeDeliverable} for a {@link MeterActivation}.
 * Maintains a copy of the original expression tree because the target
 * intervals of the nodes that reference e.g. a Channel may be different
 * depending on the actual reading types of those Channels.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (09:46)
 */
class ReadingTypeDeliverableForMeterActivation {

    private final Formula.Mode mode;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private final Range<Instant> requestedPeriod;
    private final int meterActivationSequenceNumber;
    private final ServerExpressionNode expressionNode;
    private final VirtualReadingType expressionReadingType;
    private final VirtualReadingType targetReadingType;

    ReadingTypeDeliverableForMeterActivation(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType) {
        super();
        this.mode = mode;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.requestedPeriod = requestedPeriod;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
        this.expressionNode = expressionNode;
        this.expressionReadingType = expressionReadingType;
        this.targetReadingType = VirtualReadingType.from(deliverable.getReadingType());
    }

    private long getId() {
        return this.getDeliverable().getId();
    }

    private String getName() {
        return this.getDeliverable().getName();
    }

    private int getMeterActivationSequenceNumber() {
        return meterActivationSequenceNumber;
    }

    private Range<Instant> getRange() {
        return this.meterActivation.getRange();
    }

    ReadingTypeDeliverable getDeliverable() {
        return this.deliverable;
    }

    ReadingType getReadingType () {
        return this.deliverable.getReadingType();
    }

    /**
     * Returns the String that should be used in SQL statements to refer
     * to the data produced by this VirtualReadingTypeRequirement.
     *
     * @return The id for SQL statements
     */
    String sqlName () {
        return "rod" + this.getId() + "_" + this.getMeterActivationSequenceNumber();
    }

    private String sqlComment() {
        return this.getName() + " in " + this.prettyPrintMeterActivationPeriod();
    }

    private String prettyPrintMeterActivationPeriod() {
        return this.getRange().toString();
    }

    void finish() {
        this.expressionNode.accept(new FinishRequirementAndDeliverableNodes());
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        this.appendWithClause(withClauseBuilder);
        this.appendSelectClause(sqlBuilder.select());
    }

    private void appendWithClause(SqlBuilder withClauseBuilder) {
        this.appendWithSelectClause(withClauseBuilder);
        this.appendWithFromClause(withClauseBuilder);
        this.appendWithJoinClauses(withClauseBuilder);
    }

    private void appendWithSelectClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT ");
        SqlConstants.TimeSeriesColumnNames.appendAllDeliverableSelectValues(this.expressionNode, this.resultValueNeedsTimeBasedAggregation(), withClauseBuilder);
    }

    private void appendWithFromClause(SqlBuilder sqlBuilderBuilder) {
        sqlBuilderBuilder.append("  FROM ");
        sqlBuilderBuilder.append(this.expressionNode.accept(new FromClauseForExpressionNode()));
    }

    private void appendWithJoinClauses(SqlBuilder sqlBuilder) {
        JoinClausesForExpressionNode visitor = new JoinClausesForExpressionNode("  JOIN ");
        this.expressionNode.accept(visitor);
        Iterator<String> iterator = visitor.joinClauses().iterator();
        while (iterator.hasNext()) {
            String joinClause = iterator.next();
            sqlBuilder.append(joinClause);
            if (iterator.hasNext()) {
                sqlBuilder.append("\n  ");
            }
        }
    }

    private void appendSelectClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append("'");
        sqlBuilder.append(this.getReadingType().getMRID());
        sqlBuilder.append("', ");
        this.appendValueToSelectClause(sqlBuilder);
        sqlBuilder.append(", ");
        this.appendTimelineToSelectClause(sqlBuilder);
        sqlBuilder.append(", ");
        this.appendProcessStatusToSelectClause(sqlBuilder);
        sqlBuilder.append("\n  FROM ");
        sqlBuilder.append(this.sqlName());
        this.appendGroupByClauseIfApplicable(sqlBuilder);
    }

    private void appendValueToSelectClause(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            sqlBuilder.append(this.defaultValueAggregationFunctionFor(this.targetReadingType).sqlName());
            sqlBuilder.append("(");
            sqlBuilder.append(
                    this.expressionReadingType.buildSqlUnitConversion(
                            this.mode,
                            this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                            this.targetReadingType));
            sqlBuilder.append(")");
        } else {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder);
        }
    }

    private AggregationFunction defaultValueAggregationFunctionFor(VirtualReadingType readingType) {
        return readingType.aggregationFunction();
    }

    private void appendTimelineToSelectClause(SqlBuilder sqlBuilder) {
        if (!this.resultValueNeedsTimeBasedAggregation()) {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.TIMESTAMP, sqlBuilder);
        } else {
            this.appendTrucatedTimeline(sqlBuilder);
        }
    }

    private void appendTrucatedTimeline(SqlBuilder sqlBuilder) {
        sqlBuilder.append("TRUNC(");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.LOCALDATE, sqlBuilder);
        sqlBuilder.append(", '");
        sqlBuilder.append(this.targetReadingType.getIntervalLength().toOracleTruncFormatModel());
        sqlBuilder.append("')");
    }

    private void appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames columnName, SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append(".");
        sqlBuilder.append(columnName.sqlName());
    }

    private void appendProcessStatusToSelectClause(SqlBuilder sqlBuilder) {
        if (!this.resultValueNeedsTimeBasedAggregation()) {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS, sqlBuilder);
        } else {
            this.appendAggregatedProcessStatus(sqlBuilder);
        }
    }

    private void appendAggregatedProcessStatus(SqlBuilder sqlBuilder) {
        sqlBuilder.append(AggregationFunction.BIT_OR.sqlName());
        sqlBuilder.append("(");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS, sqlBuilder);
        sqlBuilder.append(")");
    }

    private void appendGroupByClauseIfApplicable(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            this.appendGroupByClause(sqlBuilder);
        }
    }

    private void appendGroupByClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" GROUP BY ");
        this.appendTrucatedTimeline(sqlBuilder);
    }

    private boolean resultValueNeedsTimeBasedAggregation() {
        return this.expressionReadingType.getIntervalLength() != this.targetReadingType.getIntervalLength();
    }

    private class FinishRequirementAndDeliverableNodes implements ServerExpressionNode.Visitor<Void> {
        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.finish();
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.finish();
            return null;
        }

        @Override
        public Void visitConstant(NumericalConstantNode constant) {
            // Nothing to finish here
            return null;
        }

        @Override
        public Void visitConstant(StringConstantNode constant) {
            // Nothing to finish here
            return null;
        }

        @Override
        public Void visitVariable(VariableReferenceNode variable) {
            // Nothing to finish here
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operation) {
            operation.getLeftOperand().accept(this);
            operation.getRightOperand().accept(this);
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            functionCall.getArguments().forEach(child -> child.accept(this));
            return null;
        }
    }

}