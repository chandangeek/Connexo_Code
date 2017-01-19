package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractDoesNotApplyToUsagePointException;
import com.elster.jupiter.metering.aggregation.VirtualUsagePointsOnlySupportConstantLikeExpressionsException;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
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
    private volatile InstantTruncaterFactory truncaterFactory;
    private volatile SourceChannelSetFactory sourceChannelSetFactory;
    private SqlBuilderFactory sqlBuilderFactory;
    private Provider<VirtualFactory> virtualFactoryProvider;
    private CustomPropertySetService customPropertySetService;
    private ReadingTypeDeliverableForMeterActivationFactory readingTypeDeliverableForMeterActivationFactory;

    public DataAggregationServiceImpl(ServerMeteringService meteringService, InstantTruncaterFactory truncaterFactory, SourceChannelSetFactory sourceChannelSetFactory, CustomPropertySetService customPropertySetService) {
        this(SqlBuilderFactoryImpl::new, VirtualFactoryImpl::new, () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
        this.meteringService = meteringService;
        this.truncaterFactory = truncaterFactory;
        this.sourceChannelSetFactory = sourceChannelSetFactory;
        this.customPropertySetService = customPropertySetService;
    }

    // For testing purposes only
    @Inject
    public DataAggregationServiceImpl(CustomPropertySetService customPropertySetService, ServerMeteringService meteringService, InstantTruncaterFactory truncaterFactory, Provider<SqlBuilderFactory> sqlBuilderFactoryProvider, Provider<VirtualFactory> virtualFactoryProvider, Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        this(sqlBuilderFactoryProvider, virtualFactoryProvider, readingTypeDeliverableForMeterActivationFactoryProvider);
        this.meteringService = meteringService;
        this.truncaterFactory = truncaterFactory;
        this.customPropertySetService = customPropertySetService;
    }

    private DataAggregationServiceImpl(Provider<SqlBuilderFactory> sqlBuilderFactoryProvider, Provider<VirtualFactory> virtualFactoryProvider, Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        super();
        this.sqlBuilderFactory = sqlBuilderFactoryProvider.get();
        this.virtualFactoryProvider = virtualFactoryProvider;
        this.readingTypeDeliverableForMeterActivationFactory = readingTypeDeliverableForMeterActivationFactoryProvider.get();
    }

    @Override
    public CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        Loggers.ANALYSIS.debug(() -> new DataAggregationAnalysisLogger().calculationStarted(usagePoint, contract, period));
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities = this.getEffectiveMetrologyConfigurationForUsagePointInPeriod(usagePoint, period);
        this.validateContractAppliesToUsagePoint(effectivities, usagePoint, contract, period);
        Range<Instant> clippedPeriod = this.clipToContractActivePeriod(effectivities, contract, period);
        Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation = new LinkedHashMap<>();
        VirtualFactory virtualFactory = this.virtualFactoryProvider.get();
        List<MeterActivationSet> meterActivationSets = this.getMeterActivationSets(usagePoint, clippedPeriod);
        if (meterActivationSets.isEmpty()) {
            if (usagePoint.isVirtual()) {
                /* No meter activations is only supported for unmeasured usage points
                 * if all formulas of the contract are using only constants
                 * or expressions that behave as a constant (e.g. custom properties). */
                if (this.onlyConstantLikeExpressions(contract)) {
                    MeterActivationSetImpl meterActivationSet = new MeterActivationSetImpl((UsagePointMetrologyConfiguration) contract.getMetrologyConfiguration(), 1, clippedPeriod, clippedPeriod.lowerEndpoint());
                    this.prepare(usagePoint, meterActivationSet, contract, clippedPeriod, virtualFactory, deliverablesPerMeterActivation);
                } else {
                    throw new VirtualUsagePointsOnlySupportConstantLikeExpressionsException(this.getThesaurus());
                }
            } else {
                return noData(usagePoint, contract, period);
            }
        } else {
            meterActivationSets.forEach(set -> this.prepare(usagePoint, set, contract, clippedPeriod, virtualFactory, deliverablesPerMeterActivation));
        }
        if (deliverablesPerMeterActivation.isEmpty()) {
            return noData(usagePoint, contract, period);
        } else {
            try {
                return this.postProcess(
                        usagePoint,
                        contract,
                        period,
                        this.execute(
                                this.generateSql(
                                        this.sqlBuilderFactory.newClauseAwareSqlBuilder(),
                                        deliverablesPerMeterActivation,
                                        virtualFactory), deliverablesPerMeterActivation));
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private boolean onlyConstantLikeExpressions(MetrologyContract contract) {
        ConstantLikeExpression visitor = new ConstantLikeExpression(contract);
        return contract
                    .getDeliverables()
                    .stream()
                    .map(ReadingTypeDeliverable::getFormula)
                    .map(Formula::getExpressionNode)
                    .allMatch(expressionNode -> expressionNode.accept(visitor));
    }

    private CalculatedMetrologyContractDataImpl noData(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        return new CalculatedMetrologyContractDataImpl(usagePoint, contract, period, Collections.emptyMap(), this.truncaterFactory, this.sourceChannelSetFactory);
    }

    private List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationForUsagePointInPeriod(UsagePoint usagePoint, Range<Instant> period) {
        List<EffectiveMetrologyConfigurationOnUsagePoint> result =
                this.getDataModel()
                    .query(EffectiveMetrologyConfigurationOnUsagePoint.class, MetrologyConfiguration.class, MetrologyContract.class)
                    .select(     where("usagePoint").isEqualTo(usagePoint)
                            .and(where("interval").isEffective(period)));
        Loggers.ANALYSIS.debug(() -> new DataAggregationAnalysisLogger().verboseEffectiveMetrologyConfigurations(result));
        return result;
    }

    private void validateContractAppliesToUsagePoint(List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities, UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        if (effectivities.stream().noneMatch(each -> this.hasContract(each, contract))) {
            throw new MetrologyContractDoesNotApplyToUsagePointException(this.getThesaurus(), contract, usagePoint, period);
        }
    }

    private Range<Instant> clipToContractActivePeriod(List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities, MetrologyContract contract, Range<Instant> period) {
        Range<Instant> clippedPeriod = effectivities
                .stream()
                .filter(each -> this.hasContract(each, contract))
                .findFirst()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getRange)
                .map(period::intersection)
                .orElseThrow(() -> new IllegalStateException("Validation that contract was active on contract failed before"));
        if (!clippedPeriod.equals(period)) {
            Loggers.ANALYSIS.debug(() -> "Requested period clipped to effectivity of the contract: " + clippedPeriod);
        }
        return clippedPeriod;
    }

    private boolean hasContract(EffectiveMetrologyConfigurationOnUsagePoint each, MetrologyContract contract) {
        return each.getMetrologyConfiguration().getContracts().contains(contract);
    }

    @Override
    public List<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Range<Instant> period) {
        return new MeterActivationSetBuilder(usagePoint, period).build();
    }

    @Override
    public List<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Instant when) {
        return new MeterActivationSetBuilder(usagePoint, when).build();
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

    private SqlBuilder generateSql(ClauseAwareSqlBuilder sqlBuilder, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet, VirtualFactory virtualFactory) {
        this.appendAllToSqlBuilder(sqlBuilder, virtualFactory, deliverablesPerMeterActivationSet);
        SqlBuilder fullSqlBuilder = sqlBuilder.finish();
        fullSqlBuilder.append("\n ORDER BY 3 DESC, 1");
        return fullSqlBuilder;
    }

    private void appendAllToSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivationSet) {
        /* Oracle requires that all WITH clauses are generated in the correct order,
         * i.e. one WITH clause can only refer to an already defined with clause.
         * It suffices to append the definition for all requirements first
         * as those are not referring to another requirement. */
        virtualFactory.allRequirements().forEach(requirement -> requirement.appendDefinitionTo(sqlBuilder));
        deliverablesPerMeterActivationSet
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(each -> each.appendDefinitionTo(sqlBuilder));
    }

    private Map<ReadingType, List<CalculatedReadingRecord>> execute(SqlBuilder sqlBuilder, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws SQLException {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                return this.execute(statement, deliverablesPerMeterActivation);
            }
        }
    }

    private CalculatedMetrologyContractData postProcess(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        return new CalculatedMetrologyContractDataImpl(usagePoint, contract, period, calculatedReadingRecords, this.truncaterFactory, this.sourceChannelSetFactory);
    }

    private DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    private Thesaurus getThesaurus() {
        return this.meteringService.getThesaurus();
    }

    private Map<ReadingType, List<CalculatedReadingRecord>> execute(PreparedStatement statement, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws SQLException {
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

    private static class ConstantLikeExpression implements ExpressionNode.Visitor<Boolean> {
        private final MetrologyContract contract;

        private ConstantLikeExpression(MetrologyContract contract) {
            this.contract = contract;
        }

        @Override
        public Boolean visitNull(NullNode nullNode) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitConstant(ConstantNode constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitProperty(CustomPropertyNode property) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            return this.contract.getDeliverables().contains(deliverable.getReadingTypeDeliverable());
        }

        @Override
        public Boolean visitOperation(com.elster.jupiter.metering.config.OperationNode operationNode) {
            return Stream
                    .of(operationNode.getLeftOperand(), operationNode.getRightOperand())
                    .allMatch(each -> each.accept(this));
        }

        @Override
        public Boolean visitFunctionCall(com.elster.jupiter.metering.config.FunctionCallNode functionCall) {
            return functionCall.getChildren()
                    .stream()
                    .allMatch(each -> each.accept(this));
        }

    }

}