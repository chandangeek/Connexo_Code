package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
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

    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private final Range<Instant> requestedPeriod;
    private final int meterActivationSequenceNumber;
    private final ServerExpressionNode expressionNode;
    private final IntervalLength expressionAggregationInterval;

    ReadingTypeDeliverableForMeterActivation(ReadingTypeDeliverable deliverable, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, IntervalLength expressionAggregationInterval) {
        super();
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.requestedPeriod = requestedPeriod;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
        this.expressionNode = expressionNode;
        this.expressionAggregationInterval = expressionAggregationInterval;
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
        this.appendWithSelectClause(withClauseBuilder);
        this.appendWithFromClause(withClauseBuilder);
        this.appendWithJoinClauses(withClauseBuilder);
        this.appendSelectClause(sqlBuilder.select());
    }

    private void appendWithSelectClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT ");
        SqlConstants.TimeSeriesColumnNames.appendAllDeliverableSelectValues(this.expressionNode, withClauseBuilder);
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
        sqlBuilder.append(this.deliverable.getReadingType().getMRID());
        sqlBuilder.append("', ");
        this.appendValueToSelectClause(sqlBuilder);
        sqlBuilder.append(", ");
        this.appendTimelineToSelectClause(sqlBuilder);
        sqlBuilder.append("\n  FROM ");
        sqlBuilder.append(this.sqlName());
        this.appendGroupByClauseIfApplicable(sqlBuilder);
    }

    private void appendValueToSelectClause(SqlBuilder sqlBuilder) {
        if (!this.resultValueNeedsAggregation()) {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder);
        } else {
            sqlBuilder.append(this.defaultValueAggregationFunctionFor(this.deliverable.getReadingType()));
            sqlBuilder.append("(");
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder);
            sqlBuilder.append(")");
        }
    }

    private String defaultValueAggregationFunctionFor(ReadingType readingType) {
        /* Todo: consider the unit of the ReadingType
         *       flow units will use AVG
         *       volume units will use SUM
         */
        return "SUM";
    }

    private void appendTimelineToSelectClause(SqlBuilder sqlBuilder) {
        if (!this.resultValueNeedsAggregation()) {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.TIMESTAMP, sqlBuilder);
        } else {
            this.appendTrucatedTimeline(sqlBuilder);
        }
    }

    private void appendTrucatedTimeline(SqlBuilder sqlBuilder) {
        sqlBuilder.append("TRUNC(");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.LOCALDATE, sqlBuilder);
        sqlBuilder.append(", '");
        sqlBuilder.append(IntervalLength.from(this.deliverable.getReadingType()).toOracleTruncFormatModel());
        sqlBuilder.append("')");
    }

    private void appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames columnName, SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append(".");
        sqlBuilder.append(columnName.sqlName());
    }

    private void appendGroupByClauseIfApplicable(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsAggregation()) {
            this.appendGroupByClause(sqlBuilder);
        }
    }

    private void appendGroupByClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append("GROUP BY ");
        this.appendTrucatedTimeline(sqlBuilder);
    }

    private boolean resultValueNeedsAggregation() {
        return this.expressionAggregationInterval != IntervalLength.from(this.deliverable.getReadingType());
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