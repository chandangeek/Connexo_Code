/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
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
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.metering.impl.config.DependencyAnalyzer;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DataAggregationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (12:56)
 */
public class DataAggregationServiceImpl implements ServerDataAggregationService {

    private final Clock clock;
    private final CalendarService calendarService;
    private Optional<Category> touCategory;
    private final ServerMeteringService meteringService;
    private final InstantTruncaterFactory truncaterFactory;
    private final SourceChannelSetFactory sourceChannelSetFactory;
    private SqlBuilderFactory sqlBuilderFactory;
    private Provider<VirtualFactory> virtualFactoryProvider;
    private CustomPropertySetService customPropertySetService;
    private ReadingTypeDeliverableForMeterActivationFactory readingTypeDeliverableForMeterActivationFactory;

    public DataAggregationServiceImpl(
            MeteringDataModelService meteringDataModelService,
            InstantTruncaterFactory truncaterFactory,
            SourceChannelSetFactory sourceChannelSetFactory) {
        this(meteringDataModelService.getClock(),
                meteringDataModelService.getMeteringService(),
                meteringDataModelService.getCalendarService(),
                meteringDataModelService.getCustomPropertySetService(),
                truncaterFactory,
                () -> sourceChannelSetFactory,
                SqlBuilderFactoryImpl::new,
                () -> new VirtualFactoryImpl(meteringDataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringDataModelService.getMeteringService()));
    }

    // For testing purposes only
    @Inject
    public DataAggregationServiceImpl(
            Clock clock,
            ServerMeteringService meteringService,
            CalendarService calendarService,
            CustomPropertySetService customPropertySetService,
            InstantTruncaterFactory truncaterFactory,
            Provider<SqlBuilderFactory> sqlBuilderFactoryProvider,
            Provider<VirtualFactory> virtualFactoryProvider,
            Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        this(clock,
                meteringService,
                calendarService,
                customPropertySetService,
                truncaterFactory,
                () -> new SourceChannelSetFactory(meteringService),
                sqlBuilderFactoryProvider,
                virtualFactoryProvider,
                readingTypeDeliverableForMeterActivationFactoryProvider);
    }

    private DataAggregationServiceImpl(
            Clock clock,
            ServerMeteringService meteringService,
            CalendarService calendarService,
            CustomPropertySetService customPropertySetService,
            InstantTruncaterFactory truncaterFactory,
            Provider<SourceChannelSetFactory> sourceChannelSetProvider,
            Provider<SqlBuilderFactory> sqlBuilderFactoryProvider,
            Provider<VirtualFactory> virtualFactoryProvider,
            Provider<ReadingTypeDeliverableForMeterActivationFactory> readingTypeDeliverableForMeterActivationFactoryProvider) {
        super();
        this.clock = clock;
        this.meteringService = meteringService;
        this.calendarService = calendarService;
        this.customPropertySetService = customPropertySetService;
        this.truncaterFactory = truncaterFactory;
        this.sourceChannelSetFactory = sourceChannelSetProvider.get();
        this.sqlBuilderFactory = sqlBuilderFactoryProvider.get();
        this.virtualFactoryProvider = virtualFactoryProvider;
        this.readingTypeDeliverableForMeterActivationFactory = readingTypeDeliverableForMeterActivationFactoryProvider.get();
    }

    @Override
    public CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        return this.calculate((ServerUsagePoint) usagePoint, contract, period);
    }

    private CalculatedMetrologyContractData calculate(ServerUsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        VirtualFactory virtualFactory = this.virtualFactoryProvider.get();
        Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation =
                this.prepareCalculation(
                        usagePoint, contract, period,
                        () -> new DataAggregationAnalysisLogger().calculationStarted(usagePoint, contract, period),
                        virtualFactory);
        if (deliverablesPerMeterActivation.isEmpty()) {
            return noData(usagePoint, contract, period);
        } else {
            try {
                return this.postProcess(
                        usagePoint,
                        contract,
                        period,
                        generateSQL(virtualFactory, deliverablesPerMeterActivation));
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private Map<ReadingType, List<CalculatedReadingRecordImpl>> generateSQL(VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws
            SQLException {
        Map<ReadingType, List<CalculatedReadingRecordImpl>> readingTypesAndRecords = new HashMap<>();
        Map<ReadingType, List<CalculatedReadingRecordImpl>> readingTypeListMap = this.execute(
                this.generateSql(
                        this.sqlBuilderFactory.newClauseAwareSqlBuilder(),
                        deliverablesPerMeterActivation,
                        virtualFactory),
                deliverablesPerMeterActivation);
        deliverablesPerMeterActivation.forEach((meterActivationSet, readingTypeDeliverableForMeterActivationSets) -> {
            readingTypeDeliverableForMeterActivationSets.forEach(readingTypeDeliverableForMeterActivationSet -> {
                ReadingType readingType = readingTypeDeliverableForMeterActivationSet.getDeliverable().getReadingType();
                if (!readingTypeListMap.containsKey(readingType)) {
                    readingTypesAndRecords.put(readingType, new ArrayList<>());
                }
            });
        });
        readingTypesAndRecords.putAll(readingTypeListMap);
        return readingTypesAndRecords;
    }

    @Override
    public List<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Instant instant) {
        Range<Instant> period = Range.atLeast(instant);
        return usagePoint
                .getEffectiveMetrologyConfigurations(period)
                .stream()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .map(metrologyConfiguration -> this.introspect(usagePoint, period, metrologyConfiguration))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Range<Instant> period, MetrologyConfiguration metrologyConfiguration) {
        return metrologyConfiguration
                .getContracts()
                .stream()
                .map(contract -> this.introspect(usagePoint, period, contract))
                .flatMap(java.util.function.Function.identity())
                .collect(Collectors.toList());
    }

    private Stream<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Range<Instant> period, MetrologyContract contract) {
        VirtualFactory virtualFactory = this.virtualFactoryProvider.get();
        try {
            Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation =
                    this.prepareCalculation(
                            usagePoint, contract, period,
                            () -> new DataAggregationAnalysisLogger().calendarIntrospectionStarted(usagePoint, contract, period),
                            virtualFactory);
            return deliverablesPerMeterActivation
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .flatMap(ReadingTypeDeliverableForMeterActivationSet::getDetailedCalendarUsages);
        } catch (RequirementNotBackedByMeter e) {
            return Stream.empty();
        }
    }

    @Override
    public MetrologyContractCalculationIntrospector introspect(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        return this.introspect((ServerUsagePoint) usagePoint, contract, period);
    }

    private MetrologyContractCalculationIntrospector introspect(ServerUsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
        VirtualFactory virtualFactory = this.virtualFactoryProvider.get();
        Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation =
                this.prepareCalculation(
                        usagePoint, contract, period,
                        () -> new DataAggregationAnalysisLogger().introspectionStarted(usagePoint, contract, period),
                        virtualFactory);
        return new MetrologyContractCalculationIntrospectorImpl(usagePoint, contract, deliverablesPerMeterActivation);
    }

    private Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> prepareCalculation(ServerUsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Supplier<String> startLoggingSupplier, VirtualFactory virtualFactory) {
        Loggers.ANALYSIS.debug(startLoggingSupplier);
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectivities = this.getEffectiveMetrologyConfigurationForUsagePointInPeriod(usagePoint, period);
        this.validateContractAppliesToUsagePoint(effectivities, usagePoint, contract, period);
        Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation = new LinkedHashMap<>();
        Range<Instant> clippedPeriod = this.clipToContractActivePeriod(effectivities, contract, period);
        List<MeterActivationSet> meterActivationSets = this.getMeterActivationSets(usagePoint, clippedPeriod);
        if (meterActivationSets.isEmpty()) {
            if (usagePoint.isVirtual()) {
                /* No meter activations is only supported for unmeasured usage points
                 * if all formulas of the contract are using only constants
                 * or expressions that behave as a constant (e.g. custom properties). */
                if (this.onlyConstantLikeExpressions(contract)) {
                    MeterActivationSetImpl meterActivationSet =
                            new MeterActivationSetImpl(
                                    usagePoint,
                                    (UsagePointMetrologyConfiguration) contract.getMetrologyConfiguration(),
                                    1,
                                    period,
                                    period.lowerEndpoint());
                    this.prepare(usagePoint, meterActivationSet, contract, clippedPeriod, virtualFactory, deliverablesPerMeterActivation);
                } else {
                    throw new VirtualUsagePointsOnlySupportConstantLikeExpressionsException(this.getThesaurus());
                }
            }
        } else {
            meterActivationSets.forEach(set -> {
                if (!set.getMeterActivations().isEmpty()) {
                    this.prepare(usagePoint, set, contract, clippedPeriod, virtualFactory, deliverablesPerMeterActivation);
                }
            });
        }

        return deliverablesPerMeterActivation;
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
        List<EffectiveMetrologyConfigurationOnUsagePoint> result = usagePoint.getEffectiveMetrologyConfigurations(period);
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
                .filter(each -> !each.getRange().isEmpty())
                .filter(each -> this.hasContract(each, contract))
                .filter(each -> each.getRange().isConnected(period))
                .findFirst()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getRange)
                .map(period::intersection)
                .orElseThrow(() -> new IllegalStateException("Validation that contract was active on contract failed before"));
        if (!clippedPeriod.equals(period)) {
            Loggers.ANALYSIS.debug(() -> "Requested period clipped to effectivity of the contract: " + clippedPeriod);
        }
        return clippedPeriod;
    }

    @Override
    public boolean hasContract(EffectiveMetrologyConfigurationOnUsagePoint mistery, MetrologyContract contract) {
        return mistery.getMetrologyConfiguration().getContracts().contains(contract);
    }

    @Override
    public List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Range<Instant> period) {
        return new MeterActivationSetBuilder(this.customPropertySetService, usagePoint, period).build();
    }

    @Override
    public List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Instant when) {
        return new MeterActivationSetBuilder(this.customPropertySetService, usagePoint, when).build();
    }

    private void prepare(ServerUsagePoint usagePoint, MeterActivationSet meterActivationSet, MetrologyContract contract, Range<Instant> period, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        virtualFactory.nextMeterActivationSet(meterActivationSet, period);
        deliverablesPerMeterActivation.put(meterActivationSet, new ArrayList<>());
        DependencyAnalyzer
                .forAnalysisOf(contract)
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
    private void prepare(ServerUsagePoint usagePoint, MeterActivationSet meterActivationSet, ReadingTypeDeliverable deliverable, Range<Instant> period, VirtualFactory virtualFactory, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
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

    private Map<ReadingType, List<CalculatedReadingRecordImpl>> execute(SqlBuilder sqlBuilder, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws
            SQLException {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                return this.execute(statement, deliverablesPerMeterActivation);
            }
        }
    }

    private CalculatedMetrologyContractData postProcess(ServerUsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Map<ReadingType, List<CalculatedReadingRecordImpl>> calculatedReadingRecords) {
        //MetrologyContractCalculationIntrospector introspector = this.introspect(usagePoint, contract, period);
        //Map<ReadingType, List<CalculatedReadingRecordImpl>> withMissings = this.addMissings(calculatedReadingRecords, introspector, period);
        return new CalculatedMetrologyContractDataImpl(usagePoint, contract, period, calculatedReadingRecords, this.truncaterFactory, this.sourceChannelSetFactory);
    }

    private Map<ReadingType, List<CalculatedReadingRecordImpl>> addMissings(Map<ReadingType, List<CalculatedReadingRecordImpl>> readingRecords, MetrologyContractCalculationIntrospector introspector, Range<Instant> period) {
        Map<ReadingType, List<CalculatedReadingRecordImpl>> withMissings = new HashMap<>();
        readingRecords
                .entrySet()
                .forEach(readingTypeAndRecords ->
                        withMissings
                                .put(
                                        readingTypeAndRecords.getKey(),
                                        this.addMissings(
                                                readingTypeAndRecords,
                                                introspector,
                                                this.ensureBoundsOn(period, introspector.getUsagePoint().getZoneId()))));
        return withMissings;
    }

    /**
     * Ensures that the specified period has a lower and upper bound
     * to avoid the IllegalArgumentException being thrown by IntervalLength
     * when generating a timeseries.
     * If the period does not have a lower bound, the start of this year is used.
     * If the period does not have an upper bound, the end of this year is used.
     *
     * @param period The period with or without lower and upper bound
     * @return The period with lower and upper bound depending on which one was missing
     */
    private Range<Instant> ensureBoundsOn(Range<Instant> period, ZoneId zoneId) {
        if (!period.hasLowerBound()) {
            Loggers.POST_PROCESS.info(() -> "Cannot generate timeseries when start is not known, defaulting to start of this year");
            return this.ensureBoundsOn(Ranges.copy(period).withOpenLowerBound(Year.now(this.clock).atDay(1).atStartOfDay(zoneId).toInstant()), zoneId);
        }
        if (!period.hasUpperBound()) {
            Loggers.POST_PROCESS.info(() -> "Cannot generate timeseries when end is not known, defaulting to end of this year");
            ZonedDateTime startOfThisYear = Year.now(this.clock).atDay(1).atStartOfDay(zoneId);
            return this.ensureBoundsOn(Ranges.copy(period).withClosedUpperBound(startOfThisYear.plusYears(1).toInstant()), zoneId);
        }
        return period;
    }


    private List<CalculatedReadingRecordImpl> addMissings(Map.Entry<ReadingType, List<CalculatedReadingRecordImpl>> readingTypeAndRecords, MetrologyContractCalculationIntrospector introspector, Range<Instant> period) {
        List<CalculatedReadingRecordImpl> withMissings = new ArrayList<>(readingTypeAndRecords.getValue());
        if (!IntervalLength.NOT_SUPPORTED.equals(IntervalLength.from(readingTypeAndRecords.getKey()))) {
            ZoneId zoneId = introspector.getUsagePoint().getZoneId();
            Year startYear = this.getStartYear(period, zoneId);
            Year endYear = this.getEndYear(period, zoneId);
            Range<Instant> extendedPeriod = this.extendToEndOfInterval(period, readingTypeAndRecords.getKey(), zoneId);
            List<ZonedCalendarUsage> calendarUsages =
                    introspector
                            .getMetrologyContract()
                            .getDeliverables()
                            .stream()
                            .filter(deliverable -> deliverable.getReadingType().equals(readingTypeAndRecords.getKey()))
                            .findAny()
                            .map(introspector::getCalendarUsagesFor)
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .map(calendarUsage -> new ZonedCalendarUsage(introspector.getUsagePoint(), zoneId, startYear, endYear, calendarUsage))
                            .collect(Collectors.toList());
            IntervalLength
                    .from(readingTypeAndRecords.getKey())
                    .toTimeSeries(extendedPeriod, zoneId)
                    .forEach(timestamp ->
                            this.findCalendarUsage(calendarUsages, timestamp)
                                    .ifPresent(calendarUsage ->
                                            this.addMissingIfDifferentTimeOfUse(
                                                    calendarUsage,
                                                    readingTypeAndRecords.getKey(),
                                                    timestamp,
                                                    zoneId,
                                                    withMissings)));
            IntervalLength
                    .from(readingTypeAndRecords.getKey())
                    .toTimeSeries(extendedPeriod, zoneId)
                    .forEach(timestamp -> {
                        if (!this.findInWithMissings(withMissings, timestamp).isPresent()) {
                            this.addMissingRecord(
                                    readingTypeAndRecords.getKey(),
                                    timestamp,
                                    withMissings,
                                    introspector.getUsagePoint());
                        }
                    });
        }
        return withMissings;
    }

    private Year getStartYear(Range<Instant> period, ZoneId zoneId) {
        if (period.hasLowerBound()) {
            return Year.from(period.lowerEndpoint().atZone(zoneId));
        } else {
            return Year.now(this.clock);
        }
    }

    private Year getEndYear(Range<Instant> period, ZoneId zoneId) {
        if (period.hasUpperBound()) {
            return Year.from(period.upperEndpoint().atZone(zoneId));
        } else {
            return Year.now(this.clock).plusYears(1);
        }
    }

    private Range<Instant> extendToEndOfInterval(Range<Instant> period, ReadingType readingType, ZoneId zoneId) {
        return IntervalLength.from(readingType).extend(period, zoneId);
    }

    private Optional<ZonedCalendarUsage> findCalendarUsage(List<ZonedCalendarUsage> calendarUsages, Instant timestamp) {
        return calendarUsages.stream().filter(each -> each.contains(timestamp)).findAny();
    }

    private Optional<CalculatedReadingRecordImpl> findInWithMissings(List<CalculatedReadingRecordImpl> withMissings, Instant timestamp) {
        return withMissings.stream().filter(cr -> cr.getTimeStamp().equals(timestamp)).findAny();
    }

    private void addMissingIfDifferentTimeOfUse(ZonedCalendarUsage calendarUsage, ReadingType readingType, Instant timestamp, ZoneId zoneId, List<CalculatedReadingRecordImpl> readingRecords) {
        Instant calendarTimestamp = IntervalLength.from(readingType).subtractFrom(timestamp, zoneId);
        int tou = readingType.getTou();
        Event event = calendarUsage.eventFor(calendarTimestamp);
        if (event.getCode() != tou) {
            readingRecords.add(
                    this.addMissing(
                            calendarUsage.getUsagePoint(),
                            (IReadingType) readingType,
                            timestamp,
                            event));
        }
    }

    private void addMissingRecord(ReadingType readingType, Instant timestamp, List<CalculatedReadingRecordImpl> readingRecords, UsagePoint usagePoint) {
        CalculatedReadingRecordImpl crr = new CalculatedReadingRecordImpl(this.truncaterFactory, this.sourceChannelSetFactory);
        crr.initMissing(usagePoint, (IReadingType) readingType, timestamp);
        readingRecords.add(crr);
    }

    private CalculatedReadingRecordImpl addMissing(UsagePoint usagePoint, IReadingType readingType, Instant timeStamp, Event event) {
        return this.getDataModel().getInstance(CalculatedReadingRecordImpl.class).initAsPartOfGapAt(usagePoint, readingType, timeStamp, event);
    }

    private DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    @Override
    public Clock getClock() {
        return this.clock;
    }

    @Override
    public Thesaurus getThesaurus() {
        return this.meteringService.getThesaurus();
    }

    private Map<ReadingType, List<CalculatedReadingRecordImpl>> execute(PreparedStatement statement, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) throws
            SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return this.getDataModel().getInstance(CalculatedReadingRecordFactory.class).consume(resultSet, deliverablesPerMeterActivation);
        }
    }

    @Override
    public Category getTimeOfUseCategory() {
        if (touCategory == null) {
            touCategory = this.calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name());
        }
        return touCategory.orElseThrow(() -> new IllegalStateException("Calendar service installer failure, time of use category is missing"));
    }

    @Override
    public MetrologyContractDataEditor edit(UsagePoint usagePoint, MetrologyContract contract, ReadingTypeDeliverable deliverable, QualityCodeSystem qualityCodeSystem) {
        if (!contract.getDeliverables().contains(deliverable)) {
            throw new IllegalArgumentException("Deliverable is not part of the contract");
        }
        return new MetrologyContractDataEditorImpl(usagePoint, contract, deliverable, qualityCodeSystem, this);
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
