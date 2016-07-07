package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Redefines a {@link ReadingTypeDeliverable} for a {@link MeterActivationSet}.
 * Maintains a copy of the original expression tree because the target
 * intervals of the nodes that reference e.g. a Channel may be different
 * depending on the actual reading types of those Channels.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (09:46)
 */
class ReadingTypeDeliverableForMeterActivationSet {

    private final Formula.Mode mode;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivationSet meterActivationSet;
    private final int meterActivationSequenceNumber;
    private final ServerExpressionNode expressionNode;
    private final VirtualReadingType expressionReadingType;
    private final VirtualReadingType targetReadingType;

    ReadingTypeDeliverableForMeterActivationSet(Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType) {
        super();
        this.mode = mode;
        this.deliverable = deliverable;
        this.meterActivationSet = meterActivationSet;
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
        return this.meterActivationSet.getRange();
    }

    ReadingTypeDeliverable getDeliverable() {
        return this.deliverable;
    }

    ReadingType getReadingType() {
        return this.deliverable.getReadingType();
    }

    VirtualReadingType getTargetReadingType() {
        return targetReadingType;
    }

    /**
     * Returns the String that should be used in SQL statements to refer
     * to the data produced by this VirtualReadingTypeRequirement.
     *
     * @return The id for SQL statements
     */
    String sqlName() {
        return "rod" + this.getId() + "_" + this.getMeterActivationSequenceNumber();
    }

    private String sqlComment() {
        return this.getName() + this.prettyPrintedReadingType() + " in " + this.prettyPrintMeterActivationPeriod();
    }

    private String prettyPrintedReadingType() {
        VirtualReadingType readingType = VirtualReadingType.from(this.getReadingType());
        StringBuilder prettyPrinted = new StringBuilder(" as ");
        if (!readingType.getIntervalLength().equals(IntervalLength.NOT_SUPPORTED)) {
            prettyPrinted.append(readingType.getIntervalLength());
        }
        prettyPrinted
                .append(" ")
                .append(readingType.getUnitMultiplier().getSymbol())
                .append(readingType.getUnit().getSymbol());
        return prettyPrinted.toString();
    }

    private String prettyPrintMeterActivationPeriod() {
        return this.getRange().toString();
    }

    void appendSimpleReferenceTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
    }

    void appendReferenceTo(SqlBuilder sqlBuilder, VirtualReadingType targetReadingType) {
        VirtualReadingType sourceReadingType = this.targetReadingType;
        sqlBuilder.append(
                sourceReadingType.buildSqlUnitConversion(
                        this.mode,
                        this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                        targetReadingType));
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        /* Check if the expression tree contains any CustomPropertyNode and
         * add the definition for it if that was not already done by another ReadingTypeDeliverableForMeterActivation. */
        this.appendNewCustomPropertyDefinitions(sqlBuilder);
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        this.appendWithClause(withClauseBuilder);
        SqlBuilder selectClause = sqlBuilder.select();
        this.appendSelectClause(selectClause);
        if (this.expertModeAppliesAggregation()) {
            this.appendWithGroupByClause(withClauseBuilder);
        }
    }

    private void appendNewCustomPropertyDefinitions(ClauseAwareSqlBuilder sqlBuilder) {
        Flatten visitor = new Flatten();
        this.expressionNode.accept(visitor);
        visitor
                .getFlattened()
                .stream()
                .filter(each -> each instanceof CustomPropertyNode)
                .map(CustomPropertyNode.class::cast)
                .filter(each -> !sqlBuilder.withExists(each.sqlName()))
                .forEach(each -> each.appendDefinitionTo(sqlBuilder));
    }

    private void appendWithClause(SqlBuilder withClauseBuilder) {
        this.appendWithSelectClause(withClauseBuilder);
        String sourceTableName = this.appendWithFromClause(withClauseBuilder);
        this.appendWithJoinClauses(withClauseBuilder, sourceTableName);
    }

    private void appendWithSelectClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT ");
        SqlConstants.TimeSeriesColumnNames
                .appendAllDeliverableSelectValues(
                        this.mode,
                        this.expressionNode,
                        this.expertModeIntervalLength(),
                        this.targetReadingType,
                        withClauseBuilder);
    }

    private String appendWithFromClause(SqlBuilder sqlBuilderBuilder) {
        sqlBuilderBuilder.append("  FROM ");
        // Use dual as a default when expression is not backed by a requirement or deliverable that produces a timeline
        FromClauseForExpressionNode fromClauseForExpressionNode = new FromClauseForExpressionNode("dual");
        this.expressionNode.accept(fromClauseForExpressionNode);
        String sourceTableName = fromClauseForExpressionNode.getTableName();
        sqlBuilderBuilder.append(sourceTableName);
        return sourceTableName;
    }

    private void appendWithJoinClauses(SqlBuilder sqlBuilder, String sourceTableName) {
        JoinClausesForExpressionNode visitor = new JoinClausesForExpressionNode(sourceTableName);
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

    private void appendWithGroupByClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" GROUP BY ");
        String sqlName = this.expressionNode.accept(new LocalDateFromExpressionNode());
        this.appendTruncatedTimeline(sqlBuilder, sqlName);
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
            Loggers.SQL.debug(() ->
                    "Statement for deliverable " + this.deliverable.getName() + " in meter activation " + this.meterActivationSet.getRange() +
                            " requires time based aggregation because raw data interval length is " + this.expressionReadingType.getIntervalLength() +
                            " and target interval length is " + this.targetReadingType.getIntervalLength());
            sqlBuilder.append(this.targetReadingType.aggregationFunction().sqlName());
            sqlBuilder.append("(");
            sqlBuilder.append(
                    this.expressionReadingType.buildSqlUnitConversion(
                            this.mode,
                            this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                            this.targetReadingType));
            sqlBuilder.append(")");
        } else {
            if (!this.expressionReadingType.equalsIgnoreCommodity(this.targetReadingType)) {
                sqlBuilder.append(
                        this.expressionReadingType.buildSqlUnitConversion(
                                this.mode,
                                this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                                this.targetReadingType));
            } else {
                this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder, this.sqlName());
            }
        }
    }

    private void appendTimelineToSelectClause(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            Loggers.SQL.debug(() -> "Truncating timeline for deliverable " + this.deliverable.getName() + " in meter activation " + this.meterActivationSet.getRange() + " to ");
            this.appendTruncatedTimeline(sqlBuilder, this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
            sqlBuilder.append(", ");
            sqlBuilder.append(AggregationFunction.MAX.sqlName());
            sqlBuilder.append("(");
            sqlBuilder.append(this.sqlName());
            sqlBuilder.append(".");
            sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
            sqlBuilder.append(")");
        } else {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.LOCALDATE, sqlBuilder, this.sqlName());
            sqlBuilder.append(", ");
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.TIMESTAMP, sqlBuilder, this.sqlName());
        }
    }

    private void appendTruncatedTimeline(SqlBuilder sqlBuilder, String sqlName) {
        IntervalLength intervalLength;
        if (Formula.Mode.EXPERT.equals(this.mode)) {
            intervalLength = this.expressionNode.accept(new IntervalLengthFromExpressionNode());
        } else {
            intervalLength = this.targetReadingType.getIntervalLength();
        }
        Loggers.SQL.debug(() -> "Truncating " + sqlName + " to " + intervalLength.toOracleTruncFormatModel());
        sqlBuilder.append("TRUNC(");
        sqlBuilder.append(sqlName);
        sqlBuilder.append(", '");
        sqlBuilder.append(intervalLength.toOracleTruncFormatModel());
        sqlBuilder.append("')");
    }

    private void appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames columnName, SqlBuilder sqlBuilder, String sqlName) {
        sqlBuilder.append(sqlName);
        sqlBuilder.append(".");
        sqlBuilder.append(columnName.sqlName());
    }

    private void appendProcessStatusToSelectClause(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            this.appendAggregatedProcessStatus(sqlBuilder);
            sqlBuilder.append(", count(*)");
        } else {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS, sqlBuilder, this.sqlName());
            sqlBuilder.append(", 1");
        }
    }

    private void appendAggregatedProcessStatus(SqlBuilder sqlBuilder) {
        AggregationFunction.AGGREGATE_FLAGS
                .appendTo(
                        sqlBuilder, Collections.singletonList(
                                new TextFragment(this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS.sqlName())));
    }

    private void appendGroupByClauseIfApplicable(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            this.appendGroupByClause(sqlBuilder);
        }
    }

    private void appendGroupByClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" GROUP BY ");
        this.appendTruncatedTimeline(sqlBuilder, this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
    }

    private boolean resultValueNeedsTimeBasedAggregation() {
        return !this.expressionReadingType.isDontCare()
                && Formula.Mode.AUTO.equals(this.mode)
                && (this.expressionReadingType.getIntervalLength() != this.targetReadingType.getIntervalLength()
                || this.unitConversionNodeRequiresTimeBasedAggregation());
    }

    private boolean unitConversionNodeRequiresTimeBasedAggregation() {
        Flatten flatteningVisitor = new Flatten();
        this.expressionNode.accept(flatteningVisitor);
        return flatteningVisitor
                .getFlattened()
                .stream()
                .filter(node -> node instanceof UnitConversionNode)
                .map(UnitConversionNode.class::cast)
                .anyMatch(node -> !node.getSourceReadingType().getIntervalLength().equals(node.getTargetReadingType().getIntervalLength()));
    }

    private boolean expertModeAppliesAggregation() {
        return this.expertModeIntervalLength().isPresent();
    }

    private Optional<IntervalLength> expertModeIntervalLength() {
        if (Formula.Mode.EXPERT.equals(this.mode)) {
            Flatten visitor = new Flatten();
            this.expressionNode.accept(visitor);
            return visitor
                    .getFlattened()
                    .stream()
                    .filter(each -> each instanceof TimeBasedAggregationNode)
                    .findFirst()
                    .map(TimeBasedAggregationNode.class::cast)
                    .map(TimeBasedAggregationNode::getIntervalLength);
        } else {
            return Optional.empty();
        }
    }

}