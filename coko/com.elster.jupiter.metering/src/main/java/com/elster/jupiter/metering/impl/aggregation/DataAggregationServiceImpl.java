package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractDoesNotApplyToUsagePointException;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DataAggregationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (12:56)
 */
public class DataAggregationServiceImpl implements ServerDataAggregationService {

    private volatile ServerMeteringService meteringService;
    private SqlBuilderFactory sqlBuilderFactory;
    private VirtualFactory virtualFactory;
    private CustomPropertySetService customPropertySetService;
    private ReadingTypeDeliverableForMeterActivationFactory readingTypeDeliverableForMeterActivationFactory;

    // For OSGi only
    @SuppressWarnings("unused")
    public DataAggregationServiceImpl(ServerMeteringService meteringService, CustomPropertySetService customPropertySetService) {
        this(SqlBuilderFactoryImpl::new, VirtualFactoryImpl::new, () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
    }

    // For testing purposes only
    @Inject
    public DataAggregationServiceImpl(CustomPropertySetService customPropertySetService, ServerMeteringService meteringService, Provider<SqlBuilderFactory> sqlBuilderFactoryProvider, Provider<VirtualFactory> virtualFactoryProvider, Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        this(sqlBuilderFactoryProvider, virtualFactoryProvider, readingTypeDeliverableForMeterActivationFactoryProvider);
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
    }

    private DataAggregationServiceImpl(Provider<SqlBuilderFactory> sqlBuilderFactoryProvider, Provider<VirtualFactory> virtualFactoryProvider, Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        super();
        this.sqlBuilderFactory = sqlBuilderFactoryProvider.get();
        this.virtualFactory = virtualFactoryProvider.get();
        this.readingTypeDeliverableForMeterActivationFactory = readingTypeDeliverableForMeterActivationFactoryProvider.get();
    }

    @Override
    public CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities = this.getEffectiveMetrologyConfigurationForUsagePointInPeriod(usagePoint, period);
        this.validateContractAppliesToUsagePoint(effectivities, usagePoint, contract, period);
        Range<Instant> clippedPeriod = this.clipToContractActivePeriod(effectivities, contract, period);
        Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation = new LinkedHashMap<>();
        this.getMeterActivationSets(usagePoint, clippedPeriod)
                .forEach(set -> this.prepare(usagePoint, set, contract, clippedPeriod, this.virtualFactory, deliverablesPerMeterActivation));
        if (deliverablesPerMeterActivation.isEmpty()) {
            return new CalculatedMetrologyContractDataImpl(usagePoint, contract, period, Collections.emptyMap(), this.meteringService);
        } else {
            try {
                return this.postProcess(
                        usagePoint,
                        contract,
                        period,
                        this.execute(
                                this.generateSql(
                                        this.sqlBuilderFactory.newClauseAwareSqlBuilder(),
                                        deliverablesPerMeterActivation),
                                deliverablesPerMeterActivation));
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationForUsagePointInPeriod(UsagePoint usagePoint, Range<Instant> period) {
        return this.getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class, MetrologyConfiguration.class, MetrologyContract.class)
                .select(where("usagePoint").isEqualTo(usagePoint)
                        .and(where("interval").isEffective(period)));
    }

    private void validateContractAppliesToUsagePoint(List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities, UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        if (effectivities.stream().noneMatch(each -> this.hasContract(each, contract))) {
            throw new MetrologyContractDoesNotApplyToUsagePointException(this.getThesaurus(), contract, usagePoint, period);
        }
    }

    private Range<Instant> clipToContractActivePeriod(List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities, MetrologyContract contract, Range<Instant> period) {
        return effectivities
                .stream()
                .filter(each -> this.hasContract(each, contract))
                .findFirst()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getRange)
                .map(period::intersection)
                .orElseThrow(() -> new IllegalStateException("Validation that contract was active on contract failed before"));
    }

    private boolean hasContract(EffectiveMetrologyConfigurationOnUsagePoint each, MetrologyContract contract) {
        return each.getMetrologyConfiguration().getContracts().contains(contract);
    }

    @Override
    public Stream<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Range<Instant> period) {
        return new MeterActivationSetStreamBuilder(usagePoint, period).build();
    }

    @Override
    public Stream<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Instant when) {
        return new MeterActivationSetStreamBuilder(usagePoint, when).build();
    }

    private void prepare(UsagePoint usagePoint, MeterActivationSet meterActivationSet, MetrologyContract contract, Range<Instant> period, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        virtualFactory.nextMeterActivationSet(meterActivationSet, period);
        deliverablesPerMeterActivation.put(meterActivationSet, new ArrayList<>());
        contract
            .getDeliverables()
            .forEach(deliverable -> this.prepare(usagePoint, meterActivationSet, deliverable, period, virtualFactory, deliverablesPerMeterActivation));
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
     *
     * @param usagePoint The UsagePoint
     * @param meterActivationSet The MeterActivationSet
     * @param deliverable The ReadingTypeDeliverable
     * @param period The requested period in time
     */
    private void prepare(UsagePoint usagePoint, MeterActivationSet meterActivationSet, ReadingTypeDeliverable deliverable, Range<Instant> period, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        ServerExpressionNode copied = this.copy(deliverable, usagePoint, meterActivationSet, virtualFactory, deliverablesPerMeterActivation, deliverable.getFormula().getMode());
        copied.accept(new FinishRequirementAndDeliverableNodes());
        ServerExpressionNode withUnitConversion;
        VirtualReadingType readingType;
        if (Formula.Mode.AUTO.equals(deliverable.getFormula().getMode())) {
            withUnitConversion = copied.accept(new ApplyUnitConversion(deliverable.getFormula().getMode(), VirtualReadingType.from(deliverable.getReadingType())));
            readingType = this.inferReadingType(deliverable, withUnitConversion);
        } else {
            withUnitConversion = copied;
            readingType = VirtualReadingType.from(deliverable.getReadingType());
        }
        ServerExpressionNode withMultipliers = withUnitConversion.accept(new ApplyCurrentAndOrVoltageTransformer(this.meteringService, meterActivationSet));
        deliverablesPerMeterActivation
                .get(meterActivationSet)
                .add(this.readingTypeDeliverableForMeterActivationFactory
                        .from(
                                deliverable.getFormula().getMode(),
                                deliverable,
                                meterActivationSet,
                                period,
                                virtualFactory.sequenceNumber(),
                                withMultipliers,
                                readingType));
    }

    /**
     * Copies the formula of the {@link ReadingTypeDeliverable} as described by {@link Copy}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param usagePoint The UsagePoint
     * @param meterActivationSet The MeterActivationSet
     * @param deliverablesPerMeterActivationSet The Map that provides a {@link ReadingTypeDeliverableForMeterActivationSet} for each {@link MeterActivation}
     * @param mode The mode  @return The copied formula with virtual requirements and deliverables
     */
    private ServerExpressionNode copy(ReadingTypeDeliverable deliverable, UsagePoint usagePoint, MeterActivationSet meterActivationSet, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet, Formula.Mode mode) {
        ServerFormula formula = (ServerFormula) deliverable.getFormula();
        Copy visitor =
                new Copy(
                        mode,
                        virtualFactory,
                        this.customPropertySetService,
                        new ReadingTypeDeliverablePerMeterActivationSetProviderImpl(deliverablesPerMeterActivationSet),
                        deliverable,
                        usagePoint,
                        meterActivationSet);
        return formula.getExpressionNode().accept(visitor);
    }

    /**
     * Infers the most appropriate {@link VirtualReadingType} for the expressions in the tree.
     * Uses information provided by
     * <ul>
     * <li>the {@link ReadingTypeDeliverable} (for which the expression tree defines the calculation)
     * that specifies the requested or desired reading type</li>
     * <li>other ReadingTypeDeliverable that are found in the expression tree</li>
     * <li>ReadingTypeRequirements that are not using wildcards in the interval or unit multiplier</li>
     * </ul>
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param expressionTree The expression tree that defines how the ReadingTypeDeliverable should be calculated
     * @return The most appropriate aggregation interval for all expressions in the tree
     */
    private VirtualReadingType inferReadingType(ReadingTypeDeliverable deliverable, ServerExpressionNode expressionTree) {
        return expressionTree.accept(new InferReadingType(VirtualReadingType.from(deliverable.getReadingType())));
    }

    private SqlBuilder generateSql(ClauseAwareSqlBuilder sqlBuilder, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet) {
        this.appendAllToSqlBuilder(sqlBuilder, this.virtualFactory, deliverablesPerMeterActivationSet);
        SqlBuilder fullSqlBuilder = sqlBuilder.finish();
        fullSqlBuilder.append("\n ORDER BY 3 DESC, 1");
        return fullSqlBuilder;
    }

    private void appendAllToSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet) {
        /* Oracle requires that all WITH clauses are generated in the correct order,
         * i.e. one WITH clause can only refer to an already defined with clause.
         * It suffices to append the definition for all requirements first
         * as those are not referring to another requirement. */
        virtualFactory.allRequirements().stream().forEach(requirement -> requirement.appendDefinitionTo(sqlBuilder));
        deliverablesPerMeterActivationSet
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(each -> each.appendDefinitionTo(sqlBuilder));
    }

    private Map<ReadingType, List<CalculatedReadingRecord>> execute(SqlBuilder sqlBuilder, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws
            SQLException {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                return this.execute(statement, deliverablesPerMeterActivation);
            }
        }
    }

    private CalculatedMetrologyContractData postProcess(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        return new CalculatedMetrologyContractDataImpl(usagePoint, contract, period, calculatedReadingRecords, this.meteringService);
    }

    private DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    private Thesaurus getThesaurus() {
        return this.meteringService.getThesaurus();
    }

    private Map<ReadingType, List<CalculatedReadingRecord>> execute(PreparedStatement statement, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws
            SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return this.getDataModel().getInstance(CalculatedReadingRecordFactory.class).consume(resultSet, deliverablesPerMeterActivation);
        }
    }

    private static class ReadingTypeDeliverablePerMeterActivationSetProviderImpl implements ReadingTypeDeliverableForMeterActivationSetProvider {
        private final Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet;

        private ReadingTypeDeliverablePerMeterActivationSetProviderImpl(Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet) {
            this.deliverablesPerMeterActivationSet = deliverablesPerMeterActivationSet;
        }

        @Override
        public ReadingTypeDeliverableForMeterActivationSet from(ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet) {
            List<ReadingTypeDeliverableForMeterActivationSet> candidates = this.deliverablesPerMeterActivationSet.get(meterActivationSet);
            return candidates
                    .stream()
                    .filter(candidate -> candidate.getDeliverable().equals(deliverable))
                    .findAny()
                    .orElseThrow(() -> new UnsupportedOperationException("Forward references to other deliverables is not supported yet"));
        }
    }

}