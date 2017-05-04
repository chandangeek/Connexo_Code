/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cbo.QualityCodeSystem;
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
import com.elster.jupiter.metering.MeterActivation;
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
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
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
    private final Clock clock;
    private final ValidationStatusFactory validationStatusFactory;
    private final TimeService timeService;
    private final EstimationService estimationService;
    private final MeteringService meteringService;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;
    private final EstimationTaskInfoFactory estimationTaskInfoFactory;
    private final CalendarService calendarService;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final DataAggregationService dataAggregationService;

    private final Provider<UsagePointOutputValidationResource> usagePointOutputValidationResourceProvider;
    private final Provider<UsagePointOutputEstimationResource> usagePointOutputEstimationResourceProvider;

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
            ValidationStatusFactory validationStatusFactory,
            Clock clock,
            TimeService timeService,
            EstimationService estimationService,
            MeteringService meteringService,
            DataValidationTaskInfoFactory dataValidationTaskInfoFactory,
            CalendarService calendarService,
            EstimationTaskInfoFactory estimationTaskInfoFactory,
            EstimationRuleInfoFactory estimationRuleInfoFactory,
            UsagePointConfigurationService usagePointConfigurationService,
            DataAggregationService dataAggregationService,
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
        this.validationStatusFactory = validationStatusFactory;
        this.clock = clock;
        this.timeService = timeService;
        this.estimationService = estimationService;
        this.meteringService = meteringService;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
        this.calendarService = calendarService;
        this.estimationTaskInfoFactory = estimationTaskInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.dataAggregationService = dataAggregationService;
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
        Range<Instant> interval = null;
        if (filter.hasFilters()) {
            Instant now = clock.instant();
            int periodId = filter.getInteger("periodId");
            Range<Instant> relativePeriodInterval = timeService.findRelativePeriod(periodId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_RELATIVEPERIOD_FOR_ID, periodId))
                    .getOpenClosedInterval(ZonedDateTime.ofInstant(now, clock.getZone()));
            Range<Instant> upToNow = Range.atMost(now);
            if (!relativePeriodInterval.isConnected(upToNow)) {
                throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
            } else if (!relativePeriodInterval.intersection(upToNow).isEmpty()) {
                interval = getUsagePointAdjustedDataRange(usagePoint, relativePeriodInterval.intersection(upToNow)).orElse(Range.openClosed(now, now));
            }
        }
        outputInfoList = outputInfoFactory.deliverablesAsOutputInfo(effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, interval)
                .stream()
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
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
                .map(outputChannelDataInfoFactory::createChannelDataInfo)
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,
            com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE,
            com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR})
    public Response editChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                            @BeanParam JsonQueryParameters queryParameters, List<OutputChannelDataInfo> channelDataInfos) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        if (!readingType.isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }
        List<BaseReading> editedReadings = new ArrayList<>();
        List<BaseReading> estimatedReadings = new ArrayList<>();
        List<BaseReading> confirmedReadings = new ArrayList<>();
        List<Instant> removeCandidates = new ArrayList<>();

        channelDataInfos.forEach(channelDataInfo ->
                processInfo(channelDataInfo, removeCandidates, estimatedReadings, editedReadings, confirmedReadings));

        usagePoint.getEffectiveMetrologyConfigurations()
                .forEach(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyContract.getMetrologyPurpose())
                        .flatMap(effectiveMC::getChannelsContainer)
                        .ifPresent(channelsContainer -> {
                            Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                            channelsContainer.getChannel(readingType).ifPresent(channel -> {
                                DataAggregationService.MetrologyContractDataEditor editor = this.dataAggregationService.edit(usagePoint, metrologyContract, readingTypeDeliverable, QualityCodeSystem.MDM);
                                editor.estimateAll(filterInRange(estimatedReadings, containerRange));
                                editor.updateAll(filterInRange(editedReadings, containerRange));
                                editor.confirmAll(filterInRange(confirmedReadings, containerRange));
                                editor.removeTimestamps(removeCandidates.stream().filter(containerRange::contains).collect(Collectors.toSet()));
                                editor.save();
                            });
                        }));
        return Response.status(Response.Status.OK).build();
    }

    private List<BaseReading> filterInRange(Collection<BaseReading> readings, Range<Instant> range) {
        return readings.stream().filter(reading -> range.contains(reading.getTimeStamp())).collect(Collectors.toList());
    }

    private void processInfo(OutputChannelDataInfo channelDataInfo, List<Instant> removeCandidates, List<BaseReading> estimatedReadings, List<BaseReading> editedReadings, List<BaseReading> confirmedReadings) {
        Optional<ReadingQualityComment> readingQualityComment = resourceHelper.getReadingQualityComment(channelDataInfo.commentId);

        if (!isToBeConfirmed(channelDataInfo) && channelDataInfo.value == null) {
            removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
        } else {
            processValue(channelDataInfo, readingQualityComment, estimatedReadings, editedReadings);
            processConfirmedInfo(channelDataInfo, confirmedReadings);
        }
    }

    private void processValue(OutputChannelDataInfo channelDataInfo, Optional<ReadingQualityComment> readingQualityComment, List<BaseReading> estimatedReadings, List<BaseReading> editedReadings) {
        if (channelDataInfo.value != null) {
            BaseReading baseReading = channelDataInfo.createNew();
            if (channelDataInfo.isProjected) {
                ((BaseReadingImpl) baseReading).addQuality("3.12.0", this.extractComment(readingQualityComment));
                editedReadings.add(baseReading);
            } else if (channelDataInfo.ruleId != 0) {
                ((BaseReadingImpl) baseReading).addQuality("3.8." + channelDataInfo.ruleId, this.extractComment(readingQualityComment));
                estimatedReadings.add(baseReading);
            } else {
                ((BaseReadingImpl) baseReading).addQuality("3.7.0", this.extractComment(readingQualityComment));
                editedReadings.add(baseReading);
            }
        }
    }

    private void processConfirmedInfo(OutputChannelDataInfo channelDataInfo, List<BaseReading> confirmedReadings) {
        if (isToBeConfirmed(channelDataInfo)) {
            confirmedReadings.add(channelDataInfo.createConfirm());
        }
    }

    private boolean isToBeConfirmed(OutputChannelDataInfo channelDataInfo) {
    }

    private boolean isToBeConfirmed(OutputChannelDataInfo channelDataInfo) {
        return Boolean.TRUE.equals(channelDataInfo.isConfirmed);
    }
    
    private String extractComment(Optional<ReadingQualityComment> readingQualityComment) {
        if (readingQualityComment.isPresent()) {
            return readingQualityComment.get().getComment();
        }
        return null;
    }

    }

    private Optional<MetrologyContract> findMetrologyContractForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose metrologyPurpose) {
        return effectiveMC.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(metrologyPurpose))
                .findAny();
    }

    static Optional<Range<Instant>> getUsagePointAdjustedDataRange(UsagePoint usagePoint, Range<Instant> sourceRange) {
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration().get();
        if (meterActivations.isEmpty()
                && effectiveMetrologyConfiguration.getMetrologyConfiguration().getMeterRoles().isEmpty()
                && sourceRange.isConnected(effectiveMetrologyConfiguration.getRange())) {
            return Optional.of(effectiveMetrologyConfiguration.getRange().intersection(sourceRange));
        } else if (!meterActivations.isEmpty()) {
            RangeSet<Instant> meterActivationIntervals = meterActivations.stream()
                    .map(MeterActivation::getRange)
                    .collect(TreeRangeSet::<Instant>create, RangeSet::add, RangeSet::addAll);
            Range<Instant> usagePointActivationsRange = !meterActivationIntervals.isEmpty() ? meterActivationIntervals.span() : Range.singleton(Instant.MIN);
            if (usagePointActivationsRange.isConnected(sourceRange)) {
                return Optional.of(usagePointActivationsRange.intersection(sourceRange));
            }
        }
        return Optional.empty();
    }

    @POST
    @Path("/{purposeId}/outputs/{outputId}/channelData/estimate")
    @Transactional
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
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData/copyfromreference")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public List<OutputChannelDataInfo> previewCopyFromReferenceChannelData(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                                           ReferenceChannelDataInfo referenceChannelDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }
        AggregatedChannel channel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType()).get();

        return new UsagePointOutputReferenceCopier(meteringService, resourceHelper, outputChannelDataInfoFactory, channel).get(referenceChannelDataInfo);
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

        if (persistedReading.isPresent() && persistedReading.get().getValue() != null) {
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
                && ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.start > ((NumericalOutputRegisterDataInfo) registerDataInfo).interval.end) {
            throw new LocalizedFieldValidationException(MessageSeeds.INTERVAL_END_BEFORE_START, "interval.end");
        }
        BaseReading reading = registerDataInfo.createNew(readingTypeDeliverable.getReadingType());
        if ((registerDataInfo instanceof NumericalOutputRegisterDataInfo
                && NumericalOutputRegisterDataInfo.class.cast(registerDataInfo).isConfirmed != null
                && NumericalOutputRegisterDataInfo.class.cast(registerDataInfo).isConfirmed)) {
            channel.confirmReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
        } else {
            channel.editReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
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
    @Transactional
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
                            .map(readingRecord -> createCorrectedChannelDataInfo(info, readingRecord))
                            .collect(Collectors.toList()));
                });

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

    private OutputChannelDataInfo createCorrectedChannelDataInfo(ValueCorrectionInfo info, IntervalReadingRecord record) {
        return outputChannelDataInfoFactory.createUpdatedChannelDataInfo(record, info.type.apply(record.getValue(), info.amount), info.projected,
                resourceHelper.getReadingQualityComment(info.commentId));
    }
}
