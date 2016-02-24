package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DataAggregationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (12:56)
 */
@Component(name = "com.elster.jupiter.metering.aggregation", service = {DataAggregationService.class})
public class DataAggregationServiceImpl implements DataAggregationService, ReadingTypeDeliverableForMeterActivationProvider {

    private ServerMeteringService meteringService;
    private VirtualFactory virtualFactory;
    private SqlBuilderFactory sqlBuilderFactory;
    private Map<MeterActivation, List<ReadingTypeDeliverableForMeterActivation>> deliverablesPerMeterActivation;
    private ClauseAwareSqlBuilder sqlBuilder;

    // For OSGi only
    public DataAggregationServiceImpl() {
        super();
    }

    // For testing purposes only
    @Inject
    public DataAggregationServiceImpl(ServerMeteringService meteringService, VirtualFactory virtualFactory, SqlBuilderFactory sqlBuilderFactory) {
        this();
        this.setMeteringService(meteringService);
        this.setVirtualFactory(virtualFactory);
        this.setSqlBuilderFactory(sqlBuilderFactory);
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setVirtualFactory(VirtualFactory virtualFactory) {
        this.virtualFactory = virtualFactory;
    }

    @Reference
    public void setSqlBuilderFactory(SqlBuilderFactory sqlBuilderFactory) {
        this.sqlBuilderFactory = sqlBuilderFactory;
    }

    @Override
    public List<? extends BaseReadingRecord> calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        this.deliverablesPerMeterActivation = new HashMap<>();
        this.getOverlappingMeterActivations(usagePoint, period).forEach(meterActivation -> this.prepare(meterActivation, contract, period));
        try {
            return this.execute(this.generateSql());
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Stream<? extends MeterActivation> getOverlappingMeterActivations(UsagePoint usagePoint, Range<Instant> period) {
        return usagePoint.getMeterActivations().stream().filter(each -> each.overlaps(period));
    }

    private void prepare(MeterActivation meterActivation, MetrologyContract contract, Range<Instant> period) {
        this.virtualFactory.nextMeterActivation(meterActivation, period);
        this.deliverablesPerMeterActivation.put(meterActivation, new ArrayList<>());
        contract.getDeliverables().stream().forEach(deliverable -> this.prepare(meterActivation, deliverable, period));
    }

    /**
     * Prepares the data aggregation of the specified {@link ReadingTypeDeliverable}
     * with the data provided in the {@link MeterActivation}.
     * Will copy the formula of the deliverable to be able to change the
     * interval specs on each reference node (both deliverable and requirement)
     * depending on the inference engine.<br>
     * Will have created a {@link  VirtualReadingTypeRequirement} for each
     * {@link ReadingTypeRequirement} that is referenced in the formula with
     * an appropriate {@link IntervalLength} that best matches the available data
     * and the requested target interval of the deliverable.<br>
     * Will also have created a {@link VirtualReadingTypeDeliverable} for each
     * {@link ReadingTypeDeliverable} that is referenced in the formula with
     * an appropriate {@link IntervalLength} that best matches the interval
     * of the expression that calculates the deliverable.
     *
     * @param meterActivation The MeterActivation
     * @param deliverable The ReadingTypeDeliverable
     * @param period The requested period in time
     */
    private void prepare(MeterActivation meterActivation, ReadingTypeDeliverable deliverable, Range<Instant> period) {
        ServerExpressionNode preparedExpression = this.copyAndVirtualizeReferences(deliverable, meterActivation);
        this.deliverablesPerMeterActivation
                .get(meterActivation)
                .add(new ReadingTypeDeliverableForMeterActivation(
                        deliverable,
                        meterActivation,
                        period,
                        this.virtualFactory.meterActivationSequenceNumber(),
                        preparedExpression,
                        this.inferAggregationInterval(deliverable, preparedExpression)));
    }

    /**
     * Copies the formula of the {@link ReadingTypeDeliverable} and replaces
     * references to requirements and deliverables with virtual references
     * as described by {@link CopyAndVirtualizeReferences}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param meterActivation The MeterActivation
     * @return The copied formula with virtual requirements and deliverables
     */
    private ServerExpressionNode copyAndVirtualizeReferences(ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        ServerFormula formula = (ServerFormula) deliverable.getFormula();
        CopyAndVirtualizeReferences visitor = new CopyAndVirtualizeReferences(this.virtualFactory, this, deliverable, meterActivation);
        return formula.expressionNode().accept(visitor);
    }

    /**
     * Infers the most appropriate aggregation interval for the expressions in the tree.
     * Uses information provided by
     * <ul>
     * <li>the {@link ReadingTypeDeliverable} (for which the expression tree defines the calculation) that specifies the requested or desired aggregation interval</li>
     * <li>other ReadingTypeDeliverable that are found in the expression tree</li>
     * <li>ReadingTypeRequirements that are not using wildcards in the interval</li>
     * </ul>
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param expressionTree The expression tree that defines how the ReadingTypeDeliverable should be calculated
     * @return The most appropriate aggregation interval for all expressions in the tree
     */
    private IntervalLength inferAggregationInterval(ReadingTypeDeliverable deliverable, ServerExpressionNode expressionTree) {
        return expressionTree.accept(new InferAggregationInterval(IntervalLength.from(deliverable.getReadingType())));
    }

    @Override
    public ReadingTypeDeliverableForMeterActivation from(ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        List<ReadingTypeDeliverableForMeterActivation> candidates = this.deliverablesPerMeterActivation.get(meterActivation);
        return candidates
                .stream()
                .filter(candidate -> candidate.getDeliverable().equals(deliverable))
                .findAny()
                .orElseThrow(() -> new UnsupportedOperationException("Forward references to other deliverables is not supported yet"));
    }

    private SqlBuilder generateSql() {
        this.sqlBuilder = this.sqlBuilderFactory.newClauseAwareSqlBuilder();
        this.appendAllToSqlBuilder();
        SqlBuilder fullSqlBuilder = this.sqlBuilder.finish();
        fullSqlBuilder.append("\n ORDER BY 3 DESC, 1");
        return fullSqlBuilder;
    }

    private void appendAllToSqlBuilder() {
        /* Oracle requires that all WITH clauses are generated in the correct order,
         * i.e. one WITH clause can only refer to an already defined with clause.
         * It suffices to append the definition for all requirements and deliverables
         * first but therefore we need to virtualize them all first. */
        this.deliverablesPerMeterActivation
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(this::finish);
        this.virtualFactory.allRequirements().stream().forEach(requirement -> requirement.appendDefinitionTo(this.sqlBuilder));
        this.virtualFactory.allDeliverables().stream().forEach(requirement -> requirement.appendDefinitionTo(this.sqlBuilder));
        this.deliverablesPerMeterActivation
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(each -> each.appendDefinitionTo(this.sqlBuilder));
    }

    private void finish(ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation) {
        readingTypeDeliverableForMeterActivation.finish();
    }

    private List<AggregatedReadingRecord> execute(SqlBuilder sqlBuilder) throws SQLException {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                return this.execute(statement);
            }
        }
    }

    private DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    private List<AggregatedReadingRecord> execute(PreparedStatement statement) throws SQLException {
        List<AggregatedReadingRecord> aggregatedReadingRecords = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                aggregatedReadingRecords.add(AggregatedReadingRecord.from(resultSet));
            }
        }
        return aggregatedReadingRecords;
    }

}