/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.metering.impl.aggregation.DataSourceTableFactory.dual;

/**
 * Redefines a {@link ReadingTypeDeliverable} for a {@link MeterActivationSet}.
 * Maintains a copy of the original expression tree because the target
 * intervals of the nodes that reference e.g. a Channel may be different
 * depending on the actual reading types of those Channels.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (09:46)
 */
@LiteralSql
class ReadingTypeDeliverableForMeterActivationSet {

    private final ServerMeteringService meteringService;

    private final Formula.Mode mode;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivationSet meterActivationSet;
    private final int meterActivationSequenceNumber;
    private final ServerExpressionNode expressionNode;
    private final VirtualReadingType expressionReadingType;
    private final VirtualReadingType targetReadingType;
    private List<ReadingTypeRequirement> requirements;

    ReadingTypeDeliverableForMeterActivationSet(ServerMeteringService meteringService, Formula.Mode mode, ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, VirtualReadingType expressionReadingType) {
        this.meteringService = meteringService;
        this.mode = mode;
        this.deliverable = deliverable;
        this.meterActivationSet = meterActivationSet;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
        this.expressionNode = expressionNode;
        this.expressionReadingType = expressionReadingType;
        this.requirements =
                this.expressionNode
                        .accept(RequirementsFromExpressionNode.nonRecursiveOnDeliverables())
                        .stream()
                        .map(VirtualRequirementNode::getRequirement)
                        .collect(Collectors.toList());
        this.targetReadingType = VirtualReadingType.from(deliverable.getReadingType());
    }

    public List<? extends ReadingQualityRecord> getReadingQualities(Instant timestamp) {
        List<ReadingQualityRecord> result = new ArrayList<>();
        requirements.forEach(r -> result.addAll(meterActivationSet.getReadingQualitiesFor(r, getReadingQualitiesRange(timestamp))));
        return result;
    }

    Stream<ServerDataAggregationService.DetailedCalendarUsage> getDetailedCalendarUsages() {
        return this.expressionNode
                .accept(RequirementsFromExpressionNode.recursiveOnDeliverables())
                .stream()
                .map(VirtualRequirementNode::toDetailedCalendarUsage);
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

    Range<Instant> getRange() {
        return this.meterActivationSet.getRange();
    }

    ReadingTypeDeliverable getDeliverable() {
        return this.deliverable;
    }

    long getExpectedCount(Instant timestamp) {
        Range<Instant> range = getTargetRange(timestamp);
        List<Instant> expectedTimestamps = getExpectedTimestamps(range);
        return expectedTimestamps.size();
    }

    private List<Instant> getExpectedTimestamps(Range<Instant> range) {
        IntervalLength sourceIntervalLength = this.expressionReadingType.getIntervalLength();
        ZoneId zoneId = meterActivationSet.getZoneId();
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new IllegalArgumentException("Range must be finite");
        }
        ImmutableList.Builder<Instant> builder = ImmutableList.builder();
        Instant start = range.lowerEndpoint();
        start = sourceIntervalLength.addTo(start, zoneId);
        while (range.contains(start)) {
            builder.add(start);
            start = sourceIntervalLength.addTo(start, zoneId);
        }
        return builder.build();
    }

    private Range<Instant> getReadingQualitiesRange(Instant timestamp) {
        IntervalLength targetIntervalLength = this.targetReadingType.getIntervalLength();
        ZoneId zoneId = meterActivationSet.getZoneId();
        Instant endOfInterval = targetIntervalLength.truncate(timestamp, zoneId);
        endOfInterval = targetIntervalLength.addTo(endOfInterval, zoneId);
        return Range.openClosed(targetIntervalLength.subtractFrom(endOfInterval, zoneId), endOfInterval);
    }

    private Range<Instant> getTargetRange(Instant timestamp) {
        IntervalLength targetIntervalLength = this.targetReadingType.getIntervalLength();
        ZoneId zoneId = meterActivationSet.getZoneId();
        Instant endOfInterval = targetIntervalLength.truncate(timestamp, zoneId);
        return Range.openClosed(targetIntervalLength.subtractFrom(endOfInterval, zoneId), endOfInterval);
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

    String nestedSqlName() {
        return "real" + sqlName();
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

    Set<String> sourceChannelValues() {
        SourceChannelSqlNamesCollector collector = new SourceChannelSqlNamesCollector(false);
        this.expressionNode.accept(collector);
        return collector.getSourceChannelSqlNames();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        /* Check if the expression tree contains any CustomPropertyNode and
         * add the definition for it if that was not already done by another ReadingTypeDeliverableForMeterActivation. */
        this.appendNewCustomPropertyDefinitions(sqlBuilder);
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        this.appendWithClause(withClauseBuilder);
        SqlBuilder selectClause = sqlBuilder.select();
        this.appendSelectClause(selectClause);
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
        if (this.resultValueNeedsTimeBasedAggregation()) {
            Loggers.SQL.debug(() ->
                    "Statement for deliverable " + this.deliverable.getName() + " in meter activation " + this.meterActivationSet.getRange() +
                    " requires time based aggregation because raw data interval length is " + this.expressionReadingType.getIntervalLength() +
                    " and target interval length is " + this.targetReadingType.getIntervalLength());
            withClauseBuilder.append("SELECT  ");
            this.appendAllAggregatedDeliverableSelectValues(withClauseBuilder);
            withClauseBuilder.append(" FROM (");
        }
        this.appendWithSelectClause(withClauseBuilder);
        DataSourceTable source = this.appendWithFromClause(withClauseBuilder);
        this.appendWithJoinClauses(withClauseBuilder, source);
        if (this.resultValueNeedsTimeBasedAggregation()) {
            withClauseBuilder.append(") ");
            withClauseBuilder.append(nestedSqlName());
        }
        if (this.expertModeAppliesAggregation()) {
            this.appendWithGroupByClause(withClauseBuilder);
        } else {
            this.appendGroupByClauseIfApplicable(withClauseBuilder);
        }
    }

    private void appendAllAggregatedDeliverableSelectValues(SqlBuilder sqlBuilder) {
        for (SqlConstants.TimeSeriesColumnNames columnName : SqlConstants.TimeSeriesColumnNames.values()) {
            this.appendAggregatedDeliverableSelectValue(columnName, sqlBuilder);
            if (columnName != SqlConstants.TimeSeriesColumnNames.LOCALDATE) {
                sqlBuilder.append(", ");
            }
        }
    }

    private void appendAggregatedDeliverableSelectValue(SqlConstants.TimeSeriesColumnNames columnName, SqlBuilder sqlBuilder) {
        switch (columnName) {
            case VERSIONCOUNT:  // Intentional fall-through
            case ID: {
                columnName
                    .aggregationFunctionFor(this.targetReadingType)
                    .appendTo(sqlBuilder, Collections.singletonList(new TextFragment(columnName.sqlName())));
                break;
            }
            case TIMESTAMP: {
                sqlBuilder.append(AggregationFunction.MAX.sqlName());
                sqlBuilder.append("(");
                sqlBuilder.append(this.nestedSqlName());
                sqlBuilder.append(".");
                sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
                sqlBuilder.append(")");
                break;
            }
            case RECORDTIME: {
                sqlBuilder.append(AggregationFunction.MAX.sqlName());
                sqlBuilder.append("(");
                sqlBuilder.append(this.nestedSqlName());
                sqlBuilder.append(".");
                sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.RECORDTIME.sqlName());
                sqlBuilder.append(")");
                break;
            }
            case READINGQUALITY: {
                this.appendAggregatedReadingQuality(sqlBuilder);
                break;
            }
            case SOURCECHANNELS: {
                this.appendAggregatedSourceChannelsToSelectClause(sqlBuilder);
                break;
            }
            case VALUE: {
                sqlBuilder.append(this.targetReadingType.aggregationFunction().sqlName());
                sqlBuilder.append("(");
                sqlBuilder.append(
                        this.expressionReadingType.buildSqlUnitConversion(
                                Formula.Mode.AUTO,
                                this.nestedSqlName() + "." + columnName.sqlName(),
                                this.targetReadingType));
                sqlBuilder.append(")");
                break;
            }
            case LOCALDATE: {
                this.appendTruncatedTimeline(sqlBuilder, this.nestedSqlName());
                break;
            }
            default: {
                throw new IllegalArgumentException("Unexpected TimeSeriesColumnName: " + columnName.name());
            }
        }
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

    private DataSourceTable appendWithFromClause(SqlBuilder sqlBuilderBuilder) {
        sqlBuilderBuilder.append("  FROM ");
        // Use dual as a default when expression is not backed by a requirement or deliverable that produces a timeline
        FromClauseForExpressionNode fromClauseForExpressionNode = new FromClauseForExpressionNode(dual());
        this.expressionNode.accept(fromClauseForExpressionNode);
        DataSourceTable sourceTable = fromClauseForExpressionNode.getSource();
        sqlBuilderBuilder.append(sourceTable.getName());
        return sourceTable;
    }

    private void appendWithJoinClauses(SqlBuilder sqlBuilder, DataSourceTable source) {
        JoinClausesForExpressionNode visitor = new JoinClausesForExpressionNode(source);
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
        String sqlName = this.expressionNode.accept(new TimeLineSqlNameFromExpressionNode());
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
        this.appendReadingQualityToSelectClause(sqlBuilder);
        sqlBuilder.append(", ");
        this.appendSourceChannelsToSelectClause(sqlBuilder);
        sqlBuilder.append("\n  FROM ");
        sqlBuilder.append(this.sqlName());
    }

    private void appendValueToSelectClause(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            // required aggregation and unit conversion has already been done in the with-select clause
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder, this.sqlName());
        }
        else if (!this.expressionReadingType.equalsIgnoreCommodity(this.targetReadingType)) {
            sqlBuilder.append(
                    this.expressionReadingType.buildSqlUnitConversion(
                            this.mode,
                            this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                            this.targetReadingType));
        } else {
            this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.VALUE, sqlBuilder, this.sqlName());
        }
    }

    private void appendTimelineToSelectClause(SqlBuilder sqlBuilder) {
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.LOCALDATE, sqlBuilder, this.sqlName());
        sqlBuilder.append(", ");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.TIMESTAMP, sqlBuilder, this.sqlName());
        sqlBuilder.append(", ");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.RECORDTIME, sqlBuilder, this.sqlName());
    }

    private void appendTruncatedTimeline(SqlBuilder sqlBuilder, String sqlName) {
        IntervalLength intervalLength;
        if (Formula.Mode.EXPERT.equals(this.mode)) {
            intervalLength = this.expressionNode.accept(new IntervalLengthFromExpressionNode());
        } else {
            intervalLength = this.targetReadingType.getIntervalLength();
        }
        this.appendTruncatedTimeline(sqlBuilder, sqlName, intervalLength);
    }

    private void appendTruncatedTimeline(SqlBuilder sqlBuilder, String sqlName, IntervalLength intervalLength) {
        TruncatedTimelineSqlBuilder builder =
                TruncatedTimelineSqlBuilderFactory
                        .truncate(this.targetReadingType)
                        .to(intervalLength)
                        .using(sqlBuilder, this.meteringService);
        builder.append(sqlName);
    }

    private void appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames columnName, SqlBuilder sqlBuilder, String sqlName) {
        sqlBuilder.append(sqlName);
        sqlBuilder.append(".");
        sqlBuilder.append(columnName.sqlName());
    }

    private void appendReadingQualityToSelectClause(SqlBuilder sqlBuilder) {
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.READINGQUALITY, sqlBuilder, this.sqlName());
        sqlBuilder.append(", 1");
    }

    private void appendAggregatedReadingQuality(SqlBuilder sqlBuilder) {
        sqlBuilder.append("MAX(");
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.READINGQUALITY, sqlBuilder, this.nestedSqlName());
        sqlBuilder.append(")");
    }

    private void appendSourceChannelsToSelectClause(SqlBuilder sqlBuilder) {
        this.appendTimeSeriesColumnName(SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS, sqlBuilder, this.sqlName());
    }

    private void appendAggregatedSourceChannelsToSelectClause(SqlBuilder sqlBuilder) {
        SourceChannelSqlNamesCollector.appendLeafsTo(sqlBuilder, this.expressionNode);
    }

    private void appendGroupByClauseIfApplicable(SqlBuilder sqlBuilder) {
        if (this.resultValueNeedsTimeBasedAggregation()) {
            this.appendGroupByClause(sqlBuilder);
        }
    }

    private void appendGroupByClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" GROUP BY ");
        this.appendTruncatedTimeline(sqlBuilder, this.nestedSqlName());
    }

    private boolean resultValueNeedsTimeBasedAggregation() {
        return !this.expressionReadingType.isDontCare()
            && Formula.Mode.AUTO.equals(this.mode)
            && (   this.expressionReadingType.getIntervalLength() != this.targetReadingType.getIntervalLength()
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

    Collection<Pair<ReadingTypeRequirement, ChannelContract>> getPreferredChannels() {
        return this.expressionNode
                    .accept(RequirementsFromExpressionNode.recursiveOnDeliverables())
                    .stream()
                    .map(node -> Pair.of(node.getRequirement(), node.getPreferredChannel()))
                    .collect(Collectors.toList());
    }

    Set<Calendar> getUsedCalendars() {
        return this.expressionNode
                .accept(RequirementsFromExpressionNode.recursiveOnDeliverables())
                .stream()
                .map(VirtualRequirementNode::getCalendar)
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
    }

    List<VirtualRequirementNode> nestedRequirements(ServerExpressionNode.Visitor<List<VirtualRequirementNode>> visitor) {
        return this.expressionNode.accept(visitor);
    }

}