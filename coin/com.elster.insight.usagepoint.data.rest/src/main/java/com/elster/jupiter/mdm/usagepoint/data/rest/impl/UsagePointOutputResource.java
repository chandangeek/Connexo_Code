/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointOutputResource {

    private final ResourceHelper resourceHelper;
    private final EstimationHelper estimationHelper;
    private final ExceptionFactory exceptionFactory;

    private final ValidationService validationService;
    private final OutputInfoFactory outputInfoFactory;
    private final OutputChannelDataInfoFactory outputChannelDataInfoFactory;
    private final OutputRegisterDataInfoFactory outputRegisterDataInfoFactory;
    private final PurposeInfoFactory purposeInfoFactory;
    private final PurposeOutputsDataInfoFactory purposeOutputsDataInfoFactory;
    private final Clock clock;
    private final TimeService timeService;
    private final EstimationService estimationService;
    private final MeteringService meteringService;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;
    private final EstimationTaskInfoFactory estimationTaskInfoFactory;
    private final CalendarService calendarService;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final DataAggregationService dataAggregationService;
    private final UsagePointOutputReferenceCopier usagePointOutputReferenceCopier;
    private final TransactionService transactionService;
    private final UsagePointOutputsHistoryHelper usagePointOutputsHistoryHelper;

    private final Provider<UsagePointOutputValidationResource> usagePointOutputValidationResourceProvider;
    private final Provider<UsagePointOutputEstimationResource> usagePointOutputEstimationResourceProvider;
    private final Thesaurus thesaurus;


    private static final String INTERVAL_START = "intervalStart";
    private static final String INTERVAL_END = "intervalEnd";

    @Inject
    UsagePointOutputResource(
            ResourceHelper resourceHelper, ExceptionFactory exceptionFactory,
            EstimationHelper estimationHelper,
            ValidationService validationService,
            OutputInfoFactory outputInfoFactory,
            OutputChannelDataInfoFactory outputChannelDataInfoFactory,
            OutputRegisterDataInfoFactory outputRegisterDataInfoFactory,
            PurposeInfoFactory purposeInfoFactory,
            Clock clock,
            TimeService timeService,
            EstimationService estimationService,
            MeteringService meteringService,
            DataValidationTaskInfoFactory dataValidationTaskInfoFactory,
            CalendarService calendarService,
            PurposeOutputsDataInfoFactory purposeOutputsDataInfoFactory,
            Thesaurus thesaurus,
            EstimationTaskInfoFactory estimationTaskInfoFactory,
            EstimationRuleInfoFactory estimationRuleInfoFactory,
            UsagePointConfigurationService usagePointConfigurationService,
            DataAggregationService dataAggregationService,
            UsagePointOutputReferenceCopier usagePointOutputReferenceCopier,
            TransactionService transactionService,
            UsagePointOutputsHistoryHelper usagePointOutputsHistoryHelper,
            Provider<UsagePointOutputValidationResource> usagePointOutputValidationResourceProvider,
            Provider<UsagePointOutputEstimationResource> usagePointOutputEstimationResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.estimationHelper = estimationHelper;
        this.validationService = validationService;
        this.outputInfoFactory = outputInfoFactory;
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
        this.outputRegisterDataInfoFactory = outputRegisterDataInfoFactory;
        this.purposeInfoFactory = purposeInfoFactory;
        this.purposeOutputsDataInfoFactory = purposeOutputsDataInfoFactory;
        this.clock = clock;
        this.timeService = timeService;
        this.estimationService = estimationService;
        this.meteringService = meteringService;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
        this.calendarService = calendarService;
        this.estimationTaskInfoFactory = estimationTaskInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.thesaurus = thesaurus;
        this.dataAggregationService = dataAggregationService;
        this.usagePointOutputReferenceCopier = usagePointOutputReferenceCopier;
        this.transactionService = transactionService;
        this.usagePointOutputsHistoryHelper = usagePointOutputsHistoryHelper;
        this.usagePointOutputValidationResourceProvider = usagePointOutputValidationResourceProvider;
        this.usagePointOutputEstimationResourceProvider = usagePointOutputEstimationResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getUsagePointPurposes(@PathParam("name") String name, @QueryParam("withValidationTasks") boolean withValidationTasks, @BeanParam JsonQueryParameters queryParameters) {
        List<PurposeInfo> purposeInfoList = resourceHelper.findUsagePointByNameOrThrowException(name)
                .getCurrentEffectiveMetrologyConfiguration()
                .map(effectiveMetrologyConfigurationOnUsagePoint -> effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                        .getContracts()
                        .stream()
                        .map(metrologyContract -> purposeInfoFactory.asInfo(effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, withValidationTasks))
                        .sorted(Comparator.comparing(info -> info.name))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
        return PagedInfoList.fromCompleteList("purposes", purposeInfoList, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getOutputsOfUsagePointPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryFilter filter,
                                                       @BeanParam JsonQueryParameters queryParameters) {
        List<OutputInfo> outputInfoList = new ArrayList<>();
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        if (filter.hasFilters()) {
            Instant now = clock.instant();
            if (filter.hasProperty("periodId")) {
                int periodId = filter.getInteger("periodId");
                Range<Instant> interval = timeService.findRelativePeriod(periodId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_RELATIVEPERIOD_FOR_ID, periodId))
                        .getOpenClosedInterval(ZonedDateTime.ofInstant(now, clock.getZone()));
                Range<Instant> upToNow = Range.atMost(now);
                if (!interval.isConnected(upToNow)) {
                    throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
                } else if (!interval.intersection(upToNow).isEmpty()) {
                    outputInfoList = outputInfoFactory.deliverablesAsOutputInfo(effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, interval.intersection(upToNow))
                            .stream()
                            .sorted(Comparator.comparing(info -> info.name))
                            .collect(Collectors.toList());
                }
                return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
            } else if (filter.hasProperty("timeInterval")) {
                List<OutputInfo> outputInfos = new ArrayList<>();
                MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();
                usagePoint
                        .getEffectiveMetrologyConfigurations()
                        .forEach(effectiveMC ->
                                findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                                        .ifPresent(contract -> {
                                            Stream<ReadingTypeDeliverable> deliverables = contract.getDeliverables().stream();
                                            deliverables = filterDeliverables(filter, deliverables);
                                            outputInfos.addAll(deliverables
                                                    .map(deliverable -> outputInfoFactory.asFullInfo(deliverable, effectiveMC, contract))
                                                    .collect(Collectors.toList()));
                                        }));
                outputInfos.sort(Comparator.comparing(info -> info.name));
                outputInfoList = outputInfos;
            }
        } else {
            outputInfoList = outputInfoFactory.deliverablesAsOutputInfo(effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, null)
                    .stream()
                    .sorted(Comparator.comparing(info -> info.name))
                    .collect(Collectors.toList());
        }
        return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs/units")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getUnitsOfUsagePointPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryFilter filter,
                                                     @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos;
        Map<String, String> units = new HashMap<>();
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();
        units = usagePoint.getEffectiveMetrologyConfigurations()
                .stream()
                .map(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose))
                .filter(optionalContract -> optionalContract.isPresent())
                .map(Optional::get)
                .map(MetrologyContract::getDeliverables)
                .flatMap(deliverables -> deliverables
                        .stream()
                        .map(deliverable -> deliverable.getReadingType())
                        .filter(ReadingType::isRegular))
                .collect(Collectors.toMap(readingType -> readingType.getMultiplier().getMultiplier() + ":" + readingType.getUnit().getId(),
                        readingType -> getFullAliasNameElement(readingType),
                        (a, b) -> a));
        infos = units.entrySet().stream()
                .map(mapVal -> new IdWithNameInfo(mapVal.getKey(), mapVal.getValue()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("units", infos, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs/intervals")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getIntervalsOfUsagePointPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryFilter filter,
                                                         @BeanParam JsonQueryParameters queryParameters) {
        Set<OutputIntervalInfo> infoList;
        OutputIntervalInfoFactory outputIntervalInfoFactory = new OutputIntervalInfoFactory();
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();

        infoList = usagePoint.getEffectiveMetrologyConfigurations()
                .stream()
                .map(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose))
                .filter(optionalContract -> optionalContract.isPresent())
                .map(Optional::get)
                .map(MetrologyContract::getDeliverables)
                .flatMap(deliverables -> deliverables
                        .stream()
                        .map(ReadingTypeDeliverable::getReadingType)
                        .filter(ReadingType::isRegular)
                        .map(readingType -> outputIntervalInfoFactory.asIntervalInfo(readingType)))
                .collect(Collectors.toSet());
        return PagedInfoList.fromCompleteList("intervals", infoList.stream().collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs/{outputId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public OutputInfo getOutputOfPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        return outputInfoFactory.asFullInfo(readingTypeDeliverable, effectiveMetrologyConfigurationOnUsagePoint, metrologyContract);
    }

    @Path("/{purposeId}/outputs/{outputId}/validation")
    public UsagePointOutputValidationResource getUsagePointOutputValidationResource() {
        return usagePointOutputValidationResourceProvider.get();
    }

    @Path("/{purposeId}/outputs/{outputId}/estimation")
    public UsagePointOutputEstimationResource getUsagePointOutputEstimationResource() {
        return usagePointOutputEstimationResourceProvider.get();
    }

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (!readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }
        Map<Instant, ChannelReadingWithValidationStatus> outputChannelDataMap = new TreeMap<>(Collections.reverseOrder());

        usagePoint
                .getEffectiveMetrologyConfigurations()
                .forEach(effectiveMC ->
                        findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                                .ifPresent(contract ->
                                        putChannelDataFromMetrologyConfiguration(
                                                usagePoint,
                                                outputChannelDataMap,
                                                contract,
                                                readingType,
                                                filter,
                                                effectiveMC)));

        List<OutputChannelDataInfo> infoList = outputChannelDataMap.values().stream()
                .filter(getSuspectsFilter(filter, this::hasSuspects))
                .map(p -> outputChannelDataInfoFactory.createChannelDataInfo(p, ChannelPeriodType.of(readingType)))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("channelData", infoList, queryParameters);
    }

    private void putChannelDataFromMetrologyConfiguration(UsagePoint usagePoint,
                                                          Map<Instant, ChannelReadingWithValidationStatus> outputChannelDataMap,
                                                          MetrologyContract metrologyContract,
                                                          ReadingType readingType,
                                                          JsonQueryFilter filter,
                                                          EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint) {
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract).ifPresent(channelsContainer -> {
                Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                if (containerRange.isConnected(requestedInterval)) {
                    Range<Instant> effectiveInterval = containerRange.intersection(requestedInterval);
                    effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingType).ifPresent(channel -> {
                        ValidationEvaluator evaluator = validationService.getEvaluator();
                        ReadingWithValidationStatusFactory readingWithValidationStatusFactory = new ReadingWithValidationStatusFactory(
                                channel,
                                evaluator.isValidationEnabled(channel),
                                evaluator.getLastChecked(channelsContainer, channel.getMainReadingType()).orElse(null),
                                usagePoint,
                                this.calendarService);

                        Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap =
                                channel.toList(effectiveInterval)
                                        .stream()
                                        .collect(Collectors.toMap(
                                                Function.identity(),
                                                readingWithValidationStatusFactory::createChannelReading, (r1, r2) -> r1, TreeMap::new));

                        // add readings to pre filled channel data map
                        List<AggregatedChannel.AggregatedIntervalReadingRecord> calculatedReadings = channel.getAggregatedIntervalReadings(effectiveInterval);
                        for (Map.Entry<Instant, ChannelReadingWithValidationStatus> entry : preFilledChannelDataMap.entrySet()) {
                            Instant readingTimestamp = entry.getKey();
                            ChannelReadingWithValidationStatus readingWithValidationStatus = entry.getValue();
                            this.findRecordWithContainingRange(calculatedReadings, readingTimestamp)
                                    .ifPresent(readingWithValidationStatus::setReadingRecord);
                        }

                        // add validation statuses to pre filled channel data map
                        List<DataValidationStatus> dataValidationStatuses =
                                evaluator.getValidationStatus(
                                        EnumSet.of(QualityCodeSystem.MDM),
                                        channel,
                                        calculatedReadings,
                                        effectiveInterval);
                        for (DataValidationStatus dataValidationStatus : dataValidationStatuses) {
                            ChannelReadingWithValidationStatus readingWithValidationStatus = preFilledChannelDataMap.get(dataValidationStatus.getReadingTimestamp());
                            if (readingWithValidationStatus != null) {
                                readingWithValidationStatus.setValidationStatus(dataValidationStatus);
                            }
                        }
                        outputChannelDataMap.putAll(preFilledChannelDataMap);
                    });
                }
            });
        }
    }


    @GET
    @Transactional
    @Path("/{purposeId}/outputs/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public Response getChannelDataOfAllOutputs(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                               @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        Map<Instant, Map<Long, OutputChannelDataInfo>> map = new HashMap<>();
        List<PurposeOutputsDataInfo> outputsDataInfos = new ArrayList<>();
        List<Pair<ReadingTypeDeliverable, AggregatedChannel>> deliverablesWithOutputId = new ArrayList<>();
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();

        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            if (requestedInterval != null) {
                List<Pair<Long, OutputChannelDataInfo>> readingsMap = new ArrayList<>();
                usagePoint
                        .getEffectiveMetrologyConfigurations()
                        .forEach(effectiveMC ->
                                findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                                        .ifPresent(contract -> {
                                            Stream<ReadingTypeDeliverable> deliverables = contract.getDeliverables().stream();
                                            deliverables = filterDeliverables(filter, deliverables);
                                            deliverablesWithOutputId.addAll(deliverables
                                                    .map(deliverable -> Pair.of(deliverable, effectiveMC.getAggregatedChannel(contract, deliverable.getReadingType()).orElse(null)))
                                                    .collect(Collectors.toList()));

                                            Map<Instant, ChannelReadingWithValidationStatus> outputChannelDataMap = new TreeMap<>(Collections.reverseOrder());
                                            for (Pair<ReadingTypeDeliverable, AggregatedChannel> pair : deliverablesWithOutputId) {
                                                ReadingTypeDeliverable deliverable = pair.getFirst();
                                                putChannelDataFromMetrologyConfiguration(usagePoint, outputChannelDataMap, contract, deliverable.getReadingType(), filter, effectiveMC);
                                                List<OutputChannelDataInfo> outputChannelDataInfoList = outputChannelDataMap.values().stream()
                                                        .filter(getSuspectsFilter(filter, this::hasSuspects))
                                                        .filter(reading -> reading.getValue() != null)
                                                        .map(p -> outputChannelDataInfoFactory.createChannelDataInfo(p, ChannelPeriodType.of(deliverable.getReadingType())))
                                                        .collect(Collectors.toList());
                                                readingsMap.addAll(outputChannelDataInfoList.stream()
                                                        .map(info -> Pair.of(deliverable.getId(), info))
                                                        .collect(Collectors.toList()));
                                            }
                                        }));
                Map<Long, PurposeOutputsDataInfo> tempMap = new HashMap<>();
                readingsMap.forEach(pair -> {
                    if (tempMap.containsKey(pair.getLast().interval.end)) {
                        purposeOutputsDataInfoFactory.addValues(tempMap.get(pair.getLast().interval.end), pair.getFirst(), pair.getLast().value);
                    } else {
                        tempMap.put(pair.getLast().interval.end, purposeOutputsDataInfoFactory.createPurposeOutputsDataInfo(pair.getFirst(),
                                pair.getLast().value, pair.getLast().interval));
                    }
                });
                outputsDataInfos = tempMap.entrySet().stream()
                        .sorted(Comparator.comparing(entry -> entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                List<PurposeOutputsDataInfo> paginatedOutputsData = ListPager.of(outputsDataInfos).from(queryParameters).find();
                PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedOutputsData, queryParameters);
                return Response.ok(pagedInfoList).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();

    }

    private Optional<AggregatedChannel.AggregatedIntervalReadingRecord> findRecordWithContainingRange(List<AggregatedChannel.AggregatedIntervalReadingRecord> records, Instant timestamp) {
        return records
                .stream()
                .filter(record -> this.equalTimestamp(record, timestamp))
                .findFirst();
    }

    private boolean equalTimestamp(AggregatedChannel.AggregatedIntervalReadingRecord record, Instant timestamp) {
        return record.getTimeStamp().equals(timestamp);
    }

    private boolean hasSuspects(ChannelReadingWithValidationStatus channelReadingWithValidationStatus) {
        return channelReadingWithValidationStatus.getValidationStatus()
                .map(dataValidationStatus -> ValidationStatus.SUSPECT.equals(ValidationStatus.forResult(dataValidationStatus.getValidationResult())))
                .orElse(false);
    }

    private <T extends BaseReadingRecord> Map<Instant, T> toMap(List<T> readings) {
        return readings.stream().collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, Function.identity()));
    }

    @PUT
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
            com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE,
            com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR
    })

    public Response editChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                            List<OutputChannelDataInfo> channelDataInfos) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (!readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }

        EditedChannelReadingSet editedReadings = new EditedChannelReadingSet(resourceHelper).init(channelDataInfos);

        this.dataAggregationService.edit(usagePoint, metrologyContract, readingTypeDeliverable, QualityCodeSystem.MDM)
                .estimateAll(editedReadings.getEstimatedReadings())
                .updateAll(editedReadings.getEditedReadings())
                .confirmAll(editedReadings.getConfirmedReadings())
                .removeTimestamps(editedReadings.getRemoveCandidates())
                .save();

        return Response.status(Response.Status.OK).build();
    }

    private Optional<MetrologyContract> findMetrologyContractForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose metrologyPurpose) {
        return effectiveMC.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(metrologyPurpose))
                .findAny();
    }

    @PUT
    @Path("/{purposeId}/outputs/{outputId}/channelData/prevalidate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response prevalidateEditedChannelData(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                 PrevalidateChannelDataRequestInfo info, @BeanParam JsonQueryParameters queryParameters) {
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
            EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
            MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
            ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
            ReadingType readingType = readingTypeDeliverable.getReadingType();
            if (!readingType.isRegular()) {
                throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
            }

            EditedChannelReadingSet editedReadings = new EditedChannelReadingSet(resourceHelper).init(info.editedReadings);

            // save edited readings
            this.dataAggregationService.edit(usagePoint, metrologyContract, readingTypeDeliverable, QualityCodeSystem.MDM)
                    .estimateAll(editedReadings.getEstimatedReadings())
                    .updateAll(editedReadings.getEditedReadings())
                    .confirmAll(editedReadings.getConfirmedReadings())
                    .removeTimestamps(editedReadings.getRemoveCandidates())
                    .save();

            // validate edited interval
            Optional<Range<Instant>> validationRange = determineValidationRange(editedReadings, info.validateUntil);
            if (!validationRange.isPresent()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            List<PrevalidatedChannelDataInfo> infos = usagePoint.getEffectiveMetrologyConfigurations().stream()
                    .flatMap(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyContract.getMetrologyPurpose())
                            .flatMap(effectiveMC::getChannelsContainer).map(Stream::of).orElse(Stream.empty()))
                    .flatMap(channelsContainer -> validateChannel(channelsContainer, readingType, validationRange.get()))
                    .filter(dataValidationStatus -> ValidationResult.SUSPECT == dataValidationStatus.getValidationResult())
                    .map(outputChannelDataInfoFactory::createPrevalidatedChannelDataInfo)
                    .sorted(Comparator.comparing(prevalidatedChannelDataInfo -> prevalidatedChannelDataInfo.readingTime))
                    .collect(Collectors.toList());

            // do NOT commit intentionally
            return Response.ok(PagedInfoList.fromCompleteList("potentialSuspects", infos, queryParameters)).build();
        }
    }

    private Optional<Range<Instant>> determineValidationRange(EditedChannelReadingSet editedReadings, Instant validateUntil) {
        if (validateUntil == null) {
            return Optional.empty();
        }
        return editedReadings.getFirstEditedReadingTime()
                .filter(firstEditedReadingTime -> firstEditedReadingTime.compareTo(validateUntil) <= 0)
                .map(firstEditedReadingTime -> Range.closed(firstEditedReadingTime, validateUntil));
    }

    private Stream<? extends DataValidationStatus> validateChannel(ChannelsContainer channelsContainer, ReadingType readingType, Range<Instant> validationRange) {
        Optional<Channel> channel = channelsContainer.getChannel(readingType);
        Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
        if (!channel.isPresent() || !containerRange.isConnected(validationRange)) {
            return Stream.empty();
        }
        Range<Instant> rangeToPrevalidate = containerRange.intersection(validationRange);
        validationService.validate(
                new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDM), channelsContainer, readingType),
                rangeToPrevalidate);
        return validationService.getEvaluator()
                .getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDM), channel.get(), Collections.emptyList(), rangeToPrevalidate)
                .stream();
    }

    @POST
    @Path("/{purposeId}/outputs/{outputId}/channelData/estimate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR,
            com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE
    })
    public List<OutputChannelDataInfo> previewEstimateChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                          @PathParam("outputId") long outputId, EstimateChannelDataInfo estimateChannelDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (!readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }

        try (TransactionContext context = transactionService.getContext()) {
            if (estimateChannelDataInfo.editedReadings != null && !estimateChannelDataInfo.editedReadings.isEmpty()) {
                this.editChannelDataOfOutput(name, contractId, outputId, estimateChannelDataInfo.editedReadings);
            }
            Estimator estimator = estimationHelper.getEstimator(estimateChannelDataInfo);
            List<Range<Instant>> ranges = estimateChannelDataInfo.intervals.stream()
                    .map(info -> Range.openClosed(Instant.ofEpochMilli(info.start), Instant.ofEpochMilli(info.end)))
                    .collect(Collectors.toList());
            ImmutableRangeSet<Instant> blocks = ranges.stream()
                    .collect(ImmutableRangeSet::<Instant>builder, ImmutableRangeSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()))
                    .build();
            return usagePoint.getEffectiveMetrologyConfigurations().stream()
                    .map(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyContract.getMetrologyPurpose())
                            .flatMap(effectiveMC::getChannelsContainer))
                    .flatMap(Functions.asStream())
                    .flatMap(container -> {
                        Optional<ReadingQualityComment> readingQualityComment = Optional.empty();
                        readingQualityComment = resourceHelper.getReadingQualityComment(estimateChannelDataInfo.commentId);
                        return estimateInChannelsContainer(container, readingType, blocks, estimator, estimateChannelDataInfo.markAsProjected, readingQualityComment);
                    })
                    .collect(Collectors.toList());
        }
    }

    private Stream<OutputChannelDataInfo> estimateInChannelsContainer(ChannelsContainer container, ReadingType readingType, ImmutableRangeSet<Instant> blocks, Estimator estimator,
                                                                      boolean marksAsProjected, Optional<ReadingQualityComment> readingQualityComment) {
        Range<Instant> containerRange = container.getInterval().toOpenClosedRange();
        return container.getChannel(readingType)
                .map(channel -> {
                    Set<Range<Instant>> subRanges = blocks.subRangeSet(containerRange).asRanges();
                    List<EstimationResult> results = subRanges.stream()
                            .map(block -> estimationHelper.previewEstimate(QualityCodeSystem.MDM, container, readingType, block, estimator))
                            .collect(Collectors.toList());
                    return estimationHelper.getChannelDataInfoFromEstimationReports(channel, subRanges, results, marksAsProjected, readingQualityComment);
                })
                .map(List::stream)
                .orElse(Stream.empty());
    }

    @POST
    @Path("/{purposeId}/outputs/{outputId}/channelData/copyfromreference")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public List<OutputChannelDataInfo> previewCopyFromReferenceChannelData(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                                           ReferenceChannelDataInfo referenceChannelDataInfo) {
        try (TransactionContext context = transactionService.getContext()) {
            if (referenceChannelDataInfo.editedReadings != null && !referenceChannelDataInfo.editedReadings.isEmpty()) {
                this.editChannelDataOfOutput(name, contractId, outputId, referenceChannelDataInfo.editedReadings);
            }
            UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
            MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
            ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
            if (!readingTypeDeliverable.getReadingType().isRegular()) {
                throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
            }
            AggregatedChannel channel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType()).get();

            return usagePointOutputReferenceCopier.copy(channel, referenceChannelDataInfo);
        }
    }

    @GET
    @Path("/{purposeId}/outputs/{outputId}/channelData/applicableEstimationRules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE})
    public PagedInfoList getEstimationRulesForChannel(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                      @PathParam("outputId") long outputId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (!readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }

        List<EstimationRuleInfo> estimationRuleInfos = usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .map(MetrologyConfiguration::getContracts)
                .flatMap(List::stream)
                .filter(contract -> contract.getMetrologyPurpose().equals(metrologyPurpose))
                .distinct()
                .flatMap(contract -> streamMatchingEstimationRules(readingType, contract))
                .distinct()
                .map(estimationRule -> estimationRuleInfoFactory.createEstimationRuleInfo(estimationRule, usagePoint, readingType))
                .sorted(Comparator.comparing(info -> info.name.toLowerCase()))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("rules", estimationRuleInfos, queryParameters);
    }

    private Stream<? extends EstimationRule> streamMatchingEstimationRules(ReadingType readingType, MetrologyContract metrologyContract) {
        return usagePointConfigurationService.getEstimationRuleSets(metrologyContract).stream()
                .map(estimationRuleSet -> estimationRuleSet.getRules(Collections.singleton(readingType)))
                .flatMap(Collection::stream);
    }

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                 @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        MetrologyPurpose metrologyPurpose = metrologyContract.getMetrologyPurpose();
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_REGULAR, outputId);
        }

        Map<Instant, OutputRegisterDataInfo> outputRegisterData = new TreeMap<>(Collections.reverseOrder());
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations();
        effectiveMetrologyConfigurations.forEach(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                .ifPresent(contract -> putRegisterDataFromMetrologyConfiguration(usagePoint, outputRegisterData, contract, readingType, effectiveMC, filter)));
        List<OutputRegisterDataInfo> infoList = outputRegisterData.values().stream().collect(Collectors.toList());

        return PagedInfoList.fromPagedList("registerData", ListPager.of(infoList).from(queryParameters).find(), queryParameters);
    }

    private void putRegisterDataFromMetrologyConfiguration(UsagePoint usagePoint,
                                                           Map<Instant, OutputRegisterDataInfo> outputRegisterData,
                                                           MetrologyContract metrologyContract,
                                                           ReadingType readingType,
                                                           EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                           JsonQueryFilter filter) {
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer -> {
                Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                if (containerRange.isConnected(requestedInterval)) {
                    metrologyContract.getDeliverables().stream()
                            .filter(deliverable -> deliverable.getReadingType().equals(readingType))
                            .findAny()
                            .ifPresent(deliverable -> {
                                Range<Instant> effectiveInterval = containerRange.intersection(requestedInterval);
                                AggregatedChannel channel = effectiveMetrologyConfiguration.getAggregatedChannel(metrologyContract, readingType).get();
                                ValidationEvaluator evaluator = validationService.getEvaluator();

                                ReadingWithValidationStatusFactory readingWithValidationStatusFactory = new ReadingWithValidationStatusFactory(
                                        channel,
                                        evaluator.isValidationEnabled(channel),
                                        evaluator.getLastChecked(channelsContainer, channel.getMainReadingType()).orElse(null),
                                        usagePoint,
                                        calendarService);

                                // add readings to pre filled register data map
                                Map<Instant, ReadingRecord> calculatedReadings = toMap(channel.getCalculatedRegisterReadings(effectiveInterval));
                                Map<Instant, ReadingRecord> persistedReadings = toMap(channel.getPersistedRegisterReadings(effectiveInterval));
                                Map<Instant, RegisterReadingWithValidationStatus> sortedPreFilledRegisterDataMap =
                                        Stream.concat(persistedReadings.keySet().stream(), calculatedReadings.keySet().stream())
                                                .distinct()
                                                .collect(Collectors.toMap(Function.identity(), readingWithValidationStatusFactory::createRegisterReading, (a, b) -> a, TreeMap::new));
                                ReadingRecord previousReadingRecord = null;
                                for (Map.Entry<Instant, RegisterReadingWithValidationStatus> entry : sortedPreFilledRegisterDataMap.entrySet()) {
                                    Instant readingTimestamp = entry.getKey();
                                    RegisterReadingWithValidationStatus readingWithValidationStatus = entry.getValue();
                                    ReadingRecord persistedReading = persistedReadings.get(readingTimestamp);
                                    readingWithValidationStatus.setPreviousReadingRecord(previousReadingRecord);
                                    if (persistedReading != null && (persistedReading.getValue() != null || persistedReading.getText() != null)) {
                                        readingWithValidationStatus.setPersistedReadingRecord(persistedReading);
                                        ReadingRecord calculatedReading = calculatedReadings.get(readingTimestamp);
                                        if (calculatedReading != null) {
                                            readingWithValidationStatus.setCalculatedReadingRecord(calculatedReading);
                                        }
                                        previousReadingRecord = persistedReading;
                                    } else {
                                        ReadingRecord calculatedReading = calculatedReadings.get(readingTimestamp);
                                        if (calculatedReading != null) {
                                            readingWithValidationStatus.setCalculatedReadingRecord(calculatedReading);
                                            previousReadingRecord = calculatedReading;
                                        }
                                    }
                                }

                                // add validation statuses to pre filled register data map
                                List<DataValidationStatus> dataValidationStatuses = evaluator.getValidationStatus(
                                        EnumSet.of(QualityCodeSystem.MDM),
                                        channel,
                                        sortedPreFilledRegisterDataMap.values().stream()
                                                .map(RegisterReadingWithValidationStatus::getReading)
                                                .flatMap(Functions.asStream())
                                                .collect(Collectors.toList()),
                                        effectiveInterval);
                                for (DataValidationStatus dataValidationStatus : dataValidationStatuses) {
                                    ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus = sortedPreFilledRegisterDataMap.get(dataValidationStatus.getReadingTimestamp());
                                    if (readingWithValidationStatus != null) {
                                        readingWithValidationStatus.setValidationStatus(dataValidationStatus);
                                    }
                                }

                                outputRegisterData.putAll(sortedPreFilledRegisterDataMap.entrySet().stream()
                                        .filter(getSuspectsFilter(filter, this::hasSuspects))
                                        .map(entry -> Pair.of(entry.getKey(), outputRegisterDataInfoFactory.createRegisterDataInfo(entry.getValue(), deliverable)))
                                        .collect(Collectors.toMap(Pair::getFirst, Pair::getLast)));
                            });
                }
            });
        }
    }

    private boolean hasSuspects(Map.Entry<?, RegisterReadingWithValidationStatus> registerReadingWithValidationStatus) {
        return registerReadingWithValidationStatus.getValue().getValidationStatus()
                .map(dataValidationStatus -> ValidationStatus.SUSPECT.equals(ValidationStatus.forResult(dataValidationStatus.getValidationResult())))
                .orElse(false);
    }

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData/{requestedTimeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public Response getSingleRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                  @PathParam("requestedTimeStamp") long requestedTimeStamp, @BeanParam JsonQueryParameters queryParameters) {
        Instant requestedTime = Instant.ofEpochMilli(requestedTimeStamp);
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        // need to consider that effective metrology configuration has closed-open range, but contains data in open-closed range,
        // so one time quantum (millisecond) is subtracted
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(requestedTime.minusMillis(1))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT_AT_THE_MOMENT));
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfiguration, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_REGULAR, outputId);
        }
        ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get();
        AggregatedChannel channel = effectiveMetrologyConfiguration.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType()).get();
        ValidationEvaluator evaluator = validationService.getEvaluator();
        ReadingWithValidationStatusFactory readingWithValidationStatusFactory = new ReadingWithValidationStatusFactory(
                channel,
                evaluator.isValidationEnabled(channel),
                evaluator.getLastChecked(channelsContainer, channel.getMainReadingType()).orElse(null),
                usagePoint, calendarService);
        RegisterReadingWithValidationStatus readingWithValidationStatus = readingWithValidationStatusFactory.createRegisterReading(requestedTime);

        Range<Instant> dataAggregationInterval = Range.openClosed(requestedTime.minusMillis(1L), requestedTime);
        Optional<ReadingRecord> calculatedReading = channel.getCalculatedRegisterReadings(dataAggregationInterval).stream().findFirst();
        Optional<ReadingRecord> persistedReading = channel.getPersistedRegisterReadings(dataAggregationInterval).stream().findFirst();

        if (persistedReading.isPresent() && (persistedReading.get().getValue() != null || persistedReading.get().getText() != null)) {
            readingWithValidationStatus.setPersistedReadingRecord(persistedReading.get());
            calculatedReading.ifPresent(readingWithValidationStatus::setCalculatedReadingRecord);
        } else if (calculatedReading.isPresent()) {
            readingWithValidationStatus.setCalculatedReadingRecord(calculatedReading.get());
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<DataValidationStatus> dataValidationStatus = evaluator.getValidationStatus(
                EnumSet.of(QualityCodeSystem.MDM),
                channel,
                Stream.of(persistedReading, calculatedReading).flatMap(Functions.asStream()).collect(Collectors.toList()))
                .stream().findFirst();
        dataValidationStatus.ifPresent(readingWithValidationStatus::setValidationStatus);

        return Response.ok(outputRegisterDataInfoFactory.createRegisterDataInfo(readingWithValidationStatus, readingTypeDeliverable)).build();
    }

    @PUT
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public OutputRegisterDataInfo editRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                           @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryFilter filter, OutputRegisterDataInfo registerDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        registerDataInfo.timeStamp = Instant.ofEpochMilli(timeStamp);
        // need to consider that effective metrology configuration has closed-open range, but contains data in open-closed range,
        // so one time quantum (millisecond) is subtracted
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationOnUsagePoint = usagePoint.getEffectiveMetrologyConfiguration(registerDataInfo.timeStamp
                .minusMillis(1));
        if (!effectiveMetrologyConfigurationOnUsagePoint.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT_AT_THE_MOMENT, "timeStamp");
        }
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint.get(), contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ChannelsContainer channelsContainer = effectiveMetrologyConfigurationOnUsagePoint
                .get()
                .getChannelsContainer(metrologyContract).get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
        if (registerDataInfo instanceof BillingOutputRegisterDataInfo && ((BillingOutputRegisterDataInfo) registerDataInfo).interval != null
                && ((BillingOutputRegisterDataInfo) registerDataInfo).interval.start > ((BillingOutputRegisterDataInfo) registerDataInfo).interval.end) {
            throw new LocalizedFieldValidationException(MessageSeeds.INTERVAL_END_BEFORE_START, "interval.end");
        }
        if (registerDataInfo instanceof NumericalOutputRegisterDataInfo && ((NumericalOutputRegisterDataInfo) registerDataInfo).interval != null
                && ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.start != null
                && ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.end != null
                && ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.start > ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.end) {
            throw new LocalizedFieldValidationException(MessageSeeds.INTERVAL_END_BEFORE_START, "interval.end");
        }
        BaseReading reading = registerDataInfo.createNew(readingTypeDeliverable.getReadingType());
        if ((registerDataInfo instanceof NumericalOutputRegisterDataInfo
                && NumericalOutputRegisterDataInfo.class.cast(registerDataInfo).isConfirmed != null
                && NumericalOutputRegisterDataInfo.class.cast(registerDataInfo).isConfirmed)) {
            channel.confirmReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
        } else {
            try {
                channel.editReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
            } catch (UnderlyingSQLFailedException ex) {
                throw new LocalizedFieldValidationException(MessageSeeds.FIELD_VALUE_EXCEEDED, "value");
            }
        }

        return registerDataInfo;
    }

    @DELETE
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public Response removeRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                               @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryFilter filter) {
        Instant requestedTime = Instant.ofEpochMilli(timeStamp);
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        // need to consider that effective metrology configuration has closed-open range, but contains data in open-closed range,
        // so one time quantum (millisecond) is subtracted
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.getEffectiveMetrologyConfiguration(requestedTime.minusMillis(1))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT_AT_THE_MOMENT));
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ChannelsContainer channelsContainer = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract).get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
        channel.getReading(requestedTime)
                .ifPresent(reading -> channel.removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading)));
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{purposeId}/validate")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response validateMetrologyContract(@PathParam("name") String name, @PathParam("purposeId") long contractId, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, purposeInfo.parent.version);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyPurpose metrologyPurpose = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId).getMetrologyPurpose();
        Boolean somethingValidated = usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                        .flatMap(contract -> effectiveMC.getChannelsContainer(contract)
                                .map(channelsContainer -> validateIfPossible(channelsContainer, contract, purposeInfo.validationInfo.lastChecked))))
                .flatMap(Functions.asStream())
                .filter(validated -> validated)
                // If at least one 'true' is found, somethingValidated = true,
                // but short-circuit terminal operation is not acceptable here,
                // because we need to go through all data and validate it
                .reduce((validated1, validated2) -> Boolean.TRUE)
                .orElse(Boolean.FALSE);
        new RestValidationBuilder()
                .on(somethingValidated)
                .check(Boolean::booleanValue)
                .field("validationInfo.lastChecked")
                .message(MessageSeeds.NOTHING_TO_VALIDATE)
                .test()
                .validate();
        usagePoint.update();
        return Response.status(Response.Status.OK).build();
    }

    /**
     * @return {@code true} if validated, {@code false} otherwise.
     */
    private boolean validateIfPossible(ChannelsContainer channelsContainer, MetrologyContract contract, Instant lastCheckedCandidate) {
        Instant actuallyValidateFrom = resolveTimestampToValidateFrom(channelsContainer, lastCheckedCandidate);
        return Ranges.nonEmptyIntersection(channelsContainer.getInterval().toOpenClosedRange(), Range.atLeast(actuallyValidateFrom))
                .filter(rangeToValidate -> canValidateSomething(contract, rangeToValidate))
                .map(rangeToValidate -> {
                    validationService.validate(
                            new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer, contract),
                            actuallyValidateFrom);
                    return true;
                })
                .orElse(false);
    }

    private Instant resolveTimestampToValidateFrom(ChannelsContainer channelsContainer, Instant candidate) {
        Instant lastChecked = validationService.getLastChecked(channelsContainer)
                .orElseGet(channelsContainer::getStart);
        return (lastChecked.isBefore(candidate) ? lastChecked : candidate)
                .plusMillis(1); // need to exclude lastChecked timestamp itself from validation
    }

    private boolean canValidateSomething(MetrologyContract contract, Range<Instant> rangeToValidate) {
        Set<ReadingType> readingTypes = contract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .collect(Collectors.toSet());
        return usagePointConfigurationService.getValidationRuleSets(contract).stream()
                .map(ValidationRuleSet::getRuleSetVersions)
                .flatMap(List::stream)
                .filter(version -> Ranges.nonEmptyIntersection(version.getRange(), rangeToValidate).isPresent())
                .map(version -> version.getRules(readingTypes))
                .flatMap(List::stream)
                .anyMatch(ValidationRule::isActive);
    }

    @GET
    @Path("/{purposeId}/outputs/{outputId}/historicalregisterdata")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    @Transactional
    public PagedInfoList getOutputRegisterHistoryData(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                      @PathParam("outputId") long outputId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfig = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract contract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfig, contractId);
        MetrologyPurpose purpose = contract.getMetrologyPurpose();
        ReadingType readingType = resourceHelper.findReadingTypeDeliverableOrThrowException(contract, outputId, usagePoint.getName()).getReadingType();

        List<OutputRegisterHistoryDataInfo> data = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            String changedDataOnlyFlag = filter.getString("changedDataOnly");
            boolean changedDataOnly = changedDataOnlyFlag != null && (changedDataOnlyFlag.equalsIgnoreCase("yes"));
            Range<Instant> requestedInterval = Range.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            usagePoint.getEffectiveMetrologyConfigurations(requestedInterval).forEach(effectiveMC ->
                findMetrologyContractForPurpose(effectiveMC, purpose).ifPresent(metrologyContract ->
                    effectiveMC.getChannelsContainer(metrologyContract).ifPresent(channelsContainer -> {
                        Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                        if (containerRange.isConnected(requestedInterval)) {
                            Range<Instant> effectiveInterval = containerRange.intersection(requestedInterval);
                            effectiveMC.getAggregatedChannel(metrologyContract, readingType).ifPresent(aggregatedChannel -> {
                                Set<JournaledReadingRecord> collectedData = usagePointOutputsHistoryHelper.collectHistoricalRegisterData(usagePoint, aggregatedChannel,
                                        effectiveInterval, readingType, changedDataOnly);
                                data.addAll(outputRegisterDataInfoFactory.createHistoricalRegisterInfo(collectedData));
                            });
                        }
                    })));
        }

        data.sort(Comparator.comparing(info -> ((OutputRegisterHistoryDataInfo) info).interval.end)
                .thenComparing(Comparator.comparing(info -> ((OutputRegisterHistoryDataInfo) info).reportedDateTime).reversed()));
        return PagedInfoList.fromCompleteList("data", data, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs/{outputId}/historicalchanneldata")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    @Transactional
    public PagedInfoList getOutputChannelHistoryData(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                     @PathParam("outputId") long outputId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfig = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract contract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfig, contractId);
        MetrologyPurpose purpose = contract.getMetrologyPurpose();

        List<OutputChannelHistoryDataInfo> data = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            data.addAll(usagePoint.getEffectiveMetrologyConfigurations(requestedInterval).stream()
                    .map(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, purpose).map(metrologyContract -> {
                        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
                        boolean changedDataOnly = filter.getString("changedDataOnly") != null && (filter.getString("changedDataOnly")
                                .equalsIgnoreCase("yes"));
                        return outputChannelDataInfoFactory.createOutputChannelHistoryDataInfo(usagePointOutputsHistoryHelper
                                .collectHistoricalChannelData(requestedInterval, usagePoint, metrologyContract,
                                        effectiveMC, readingTypeDeliverable.getReadingType(), changedDataOnly))
                                .stream();
                    })).flatMap(Functions.asStream())
                    .flatMap(Function.identity())
                    .filter(record -> ((OutputChannelHistoryDataInfo) record).reportedDateTime != null)
                    .sorted(Comparator.comparing(info -> ((OutputChannelHistoryDataInfo) info).interval.end)
                            .thenComparing(Comparator.comparing(info -> ((OutputChannelHistoryDataInfo) info).reportedDateTime).reversed()))
                    .collect(Collectors.toList()));
        }

        return PagedInfoList.fromCompleteList("data", data, queryParameters);
    }

    @PUT
    @Path("/{purposeId}/estimate")
    @RolesAllowed({com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response estimateMetrologyContract(@PathParam("name") String name, @PathParam("purposeId") long contractId, EstimatePurposeRequestInfo info) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, info.parent.version);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyPurpose metrologyPurpose = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId).getMetrologyPurpose();
        usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC ->
                findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                        .flatMap(effectiveMC::getChannelsContainer)
                        .map(this::estimate)
                        .filter(estimationReport -> info.revalidate)
                        .ifPresent(this::revalidate));
        usagePoint.update();
        return Response.status(Response.Status.OK).build();
    }

    private EstimationReport estimate(ChannelsContainer channelsContainer) {
        return estimationService.estimate(QualityCodeSystem.MDM, channelsContainer, channelsContainer.getRange());
    }

    private void revalidate(EstimationReport estimationReport) {
        estimationReport.getResults().values().stream()
                .filter(estimationResult -> !estimationResult.estimated().isEmpty())
                .forEach(this::revalidateNonEmpty);
    }

    private void revalidateNonEmpty(EstimationResult estimationResult) {
        Channel channel = estimationResult.estimated().get(0).getChannel();
        Instant firstEstimatedOnChannel = estimationResult.estimated().stream()
                .map(EstimationBlock::estimatables)
                .flatMap(Collection::stream)
                .map(Estimatable::getTimestamp)
                .min(Comparator.naturalOrder())
                .get();
        Instant lastChecked = validationService.getLastChecked(channel).orElse(null);
        validationService.validate(
                new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDM), channel.getChannelsContainer(), channel.getMainReadingType()),
                Ranges.closed(firstEstimatedOnChannel, lastChecked));
    }

    @PUT
    @Path("/{contractId}/activate")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response activateMetrologyContract(@PathParam("name") String name, @PathParam("contractId") long contractId, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, purposeInfo.parent.version);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);

        MetrologyContract metrologyContract = resourceHelper.findInactiveMetrologyContractOrThrowException(effectiveMC, contractId);

        resourceHelper.checkMeterRequirements(usagePoint, metrologyContract);

        effectiveMC.activateOptionalMetrologyContract(metrologyContract, clock.instant());
        return Response.status(Response.Status.OK).entity(purposeInfoFactory.asInfo(effectiveMC, metrologyContract, false)).build();
    }

    @PUT
    @Path("/{contractId}/deactivate")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response deactivateMetrologyContract(@PathParam("name") String name, @PathParam("contractId") long contractId, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, purposeInfo.parent.version);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        Instant now = clock.instant();
        if (effectiveMC.getChannelsContainer(metrologyContract, now).isPresent()) {
            effectiveMC.deactivateOptionalMetrologyContract(metrologyContract, now);
        }
        return Response.status(Response.Status.OK).entity(purposeInfoFactory.asInfo(effectiveMC, metrologyContract, false)).build();
    }

    @GET
    @Path("/{purposeId}/validationtasks")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getValidationTasksOnUsagePoint(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        List<DataValidationTask> validationTasks = validationService.findValidationTasks()
                .stream()
                .filter(task -> !task.getMetrologyPurpose().isPresent()
                        || task.getQualityCodeSystem().equals(QualityCodeSystem.MDM)
                        && task.getMetrologyPurpose().get().equals(metrologyContract.getMetrologyPurpose()))
                .collect(Collectors.toList());

        List<DataValidationTaskInfo> dataValidationTasks = validationTasks
                .stream()
                .map(DataValidationTask::getUsagePointGroup)
                .filter(Optional::isPresent)
                .flatMap(Functions.asStream())
                .distinct()
                .filter(usagePointGroup -> isMember(usagePoint, usagePointGroup))
                .flatMap(usagePointGroup -> validationTasks.stream()
                        .filter(dataValidationTask -> dataValidationTask.getUsagePointGroup()
                                .filter(usagePointGroup::equals)
                                .isPresent()))
                .map(dataValidationTaskInfoFactory::asInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("dataValidationTasks", dataValidationTasks, queryParameters);
    }

    @GET
    @Path("/{purposeId}/estimationtasks")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getEstimationTasksOnUsagePoint(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        List<EstimationTask> estimationTasks = estimationService.findEstimationTasks(QualityCodeSystem.MDM)
                .stream()
                .filter(task -> !task.getMetrologyPurpose().isPresent() || task.getMetrologyPurpose().get().equals(metrologyContract.getMetrologyPurpose()))
                .collect(Collectors.toList());

        List<EstimationTaskShortInfo> dataEstimationTasks = estimationTasks
                .stream()
                .map(EstimationTask::getUsagePointGroup)
                .filter(Optional::isPresent)
                .flatMap(Functions.asStream())
                .distinct()
                .filter(usagePointGroup -> isMember(usagePoint, usagePointGroup))
                .flatMap(usagePointGroup -> estimationTasks.stream()
                        .filter(estimationTask -> estimationTask.getUsagePointGroup()
                                .filter(usagePointGroup::equals)
                                .isPresent()))
                .map(estimationTaskInfoFactory::asInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("dataEstimationTasks", dataEstimationTasks, queryParameters);
    }

    @PUT
    @Path("/{purposeId}/outputs/{outputId}/channelData/correctValues")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public List<OutputChannelDataInfo> correctValues(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                     ValueCorrectionInfo info, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        AggregatedChannel channel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType()).get();

        List<OutputChannelDataInfo> result = new ArrayList<>();
        try (TransactionContext context = transactionService.getContext()) {
            if (info.editedReadings != null && !info.editedReadings.isEmpty()) {
                this.editChannelDataOfOutput(name, contractId, outputId, info.editedReadings);
            }
            Set<Instant> timestamps = info.intervals.stream()
                    .map(intervalInfo -> Instant.ofEpochMilli(intervalInfo.end))
                    .collect(Collectors.toSet());

            info.intervals.stream()
                    .map(interval -> Range.openClosed(Instant.ofEpochMilli(interval.start), Instant.ofEpochMilli(interval.end)))
                    .reduce(Range::span)
                    .ifPresent(intervals -> {
                        List<IntervalReadingRecord> intervalReadingRecords = channel.getIntervalReadings(intervals);
                        result.addAll(intervalReadingRecords.stream()
                                .filter(record -> timestamps.contains(record.getTimeStamp()))
                                .map(readingRecord -> createCorrectedChannelDataInfo(info, readingRecord, usagePoint.getZoneId()))
                                .collect(Collectors.toList()));
                    });
        }
        return result;
    }

    private boolean isMember(UsagePoint usagePoint, UsagePointGroup usagePointGroup) {
        return !meteringService.getUsagePointQuery()
                .select(Where.where("id").isEqualTo(usagePoint.getId())
                        .and(ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "id")), 1, 1)
                .isEmpty();
    }


    private <T> Predicate<T> getSuspectsFilter(JsonQueryFilter filter, Predicate<T> hasSuspects) {
        return filter.hasProperty("suspect") ? hasSuspects : info -> true;
    }

    private OutputChannelDataInfo createCorrectedChannelDataInfo(ValueCorrectionInfo info, IntervalReadingRecord record, ZoneId zoneId) {
        return outputChannelDataInfoFactory.createUpdatedChannelDataInfo(record, info.type.apply(record.getValue(), info.amount), info.projected,
                resourceHelper.getReadingQualityComment(info.commentId), zoneId);
    }

    private String getFullAliasNameElement(ReadingType readingType) {
        ReadingTypeUnit unit = readingType.getUnit();
        MetricMultiplier multiplier = readingType.getMultiplier();
        return thesaurus.getString("readingType.multiplier." + multiplier.name(), multiplier.getSymbol()) + thesaurus.getString("readingType.unit." + unit.getSymbol() + ".symbol", unit
                .getSymbol());
    }

    private Stream<ReadingTypeDeliverable> filterDeliverables(JsonQueryFilter filter, Stream<ReadingTypeDeliverable> deliverables) {
        Stream<ReadingTypeDeliverable> resultDeliverablesList;
        if (filter.hasProperty("timeInterval")) {
            Long timeIntervalId = filter.getLong("timeInterval");
            if (filter.hasProperty("bulk")) {
                Boolean includeBulk = filter.getBoolean("bulk");
                if (includeBulk == true) {
                    resultDeliverablesList = deliverables
                            .filter(deliverable -> deliverable.getReadingType().isRegular());
                } else {
                    resultDeliverablesList = deliverables
                            .filter(deliverable -> deliverable.getReadingType().isRegular())
                            .filter(deliverable -> !deliverable.getReadingType().isCumulative());
                }
            } else {
                resultDeliverablesList = deliverables
                        .filter(deliverable -> deliverable.getReadingType().isRegular())
                        .filter(deliverable -> !deliverable.getReadingType().isCumulative());
            }
            if (filter.hasProperty("unit")) {
                String separator = ":";
                String unit = filter.getString("unit");
                String[] unitWithMultiplier = unit.split(separator);
                if (unitWithMultiplier.length == 2) {
                    Long unitId = Long.parseLong(unitWithMultiplier[1]);
                    Long unitMultiplier = Long.parseLong(unitWithMultiplier[0]);
                    resultDeliverablesList = resultDeliverablesList
                            .filter(deliverable -> deliverable.getReadingType().getMeasuringPeriod().getId() == timeIntervalId ||
                                    deliverable.getReadingType().getMacroPeriod().getId() == timeIntervalId)
                            .filter(deliverable -> deliverable.getReadingType().getUnit().getId() == unitId)
                            .filter(deliverable -> deliverable.getReadingType().getMultiplier().getMultiplier() == unitMultiplier);
                }
            } else {
                resultDeliverablesList = resultDeliverablesList
                        .filter(deliverable -> deliverable.getReadingType().getMeasuringPeriod().getId() == timeIntervalId ||
                                deliverable.getReadingType().getMacroPeriod().getId() == timeIntervalId);
            }
        } else {
            resultDeliverablesList = deliverables
                    .filter(deliverable -> deliverable.getReadingType().isRegular());
        }
        return resultDeliverablesList;
    }
}
