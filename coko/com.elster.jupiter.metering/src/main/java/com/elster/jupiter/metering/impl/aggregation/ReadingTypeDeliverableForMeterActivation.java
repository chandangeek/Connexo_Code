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
        return "rid" + this.getId() + "_" + this.getMeterActivationSequenceNumber();
    }

    private String sqlComment() {
        return this.getName() + " in " + this.prettyPrintMeterActivationPeriod();
    }

    private String prettyPrintMeterActivationPeriod() {
        return this.getRange().toString();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        // Todo: 1. clip the MeterActivation's range to the requested period
        // Todo: 2. support aggregation at this level
        if (this.expressionAggregationInterval != IntervalLength.from(this.deliverable.getReadingType())) {
            throw new UnsupportedOperationException("Aggregating " + ReadingTypeDeliverableForMeterActivation.class.getSimpleName() + " to IntervalLength different from expression interval length is not supported yet");
        }
        this.appendSelectClause(withClauseBuilder);
        this.appendFromClause(withClauseBuilder);
        this.appendJoinClauses(withClauseBuilder);
    }

    private void appendSelectClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT -1, ");
        withClauseBuilder.add(this.expressionNode.accept(new ExpressionNodeToSql()));
        withClauseBuilder.append(", ");
        withClauseBuilder.append(this.expressionNode.accept(new LocalDateFromExpressionNode()));
    }

    private void appendFromClause(SqlBuilder sqlBuilderBuilder) {
        sqlBuilderBuilder.append("  FROM ");
        sqlBuilderBuilder.append(this.expressionNode.accept(new FromClauseForExpressionNode()));
    }

    private void appendJoinClauses(SqlBuilder sqlBuilder) {
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

}