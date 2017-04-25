/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.BaseReadingImpl;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
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
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
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
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final UsagePointConfigurationService usagePointConfigurationService;

    private static final String INTERVAL_START = "intervalStart";
    private static final String INTERVAL_END = "intervalEnd";

    @Inject
    UsagePointOutputResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory,
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
                             EstimationTaskInfoFactory estimationTaskInfoFactory,
                             EstimationRuleInfoFactory estimationRuleInfoFactory, UsagePointConfigurationService usagePointConfigurationService) {
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
        this.estimationTaskInfoFactory = estimationTaskInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.usagePointConfigurationService = usagePointConfigurationService;
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
            int periodId = filter.getInteger("periodId");
            Range<Instant> interval = timeService.findRelativePeriod(periodId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_RELATIVEPERIOD_FOR_ID, periodId))
                    .getOpenClosedInterval(ZonedDateTime.ofInstant(now, clock.getZone()));
            Range<Instant> upToNow = Range.atMost(now);
            if (!interval.isConnected(upToNow)) {
                throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
            } else if (!interval.intersection(upToNow).isEmpty()) {
                Range<Instant> adjustedInterval = getUsagePointAdjustedDataRange(usagePoint, interval.intersection(upToNow)).orElse(Range.openClosed(now, now));
                outputInfoList = metrologyContract.getDeliverables()
                        .stream()
                        .map(deliverable -> outputInfoFactory.asInfo(deliverable, effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, adjustedInterval))
                        .sorted(Comparator.comparing(info -> info.name))
                        .collect(Collectors.toList());
            }
            return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
        } else {
            outputInfoList = metrologyContract.getDeliverables()
                    .stream()
                    .map(deliverable -> outputInfoFactory.asInfo(deliverable, effectiveMetrologyConfigurationOnUsagePoint, metrologyContract, null))
                    .sorted(Comparator.comparing(info -> info.name))
                    .collect(Collectors.toList());
            return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
        }
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

        usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                .ifPresent(contract -> putChannelDataFromMetrologyConfiguration(outputChannelDataMap, contract, readingType, filter, effectiveMC)));

        List<OutputChannelDataInfo> infoList = outputChannelDataMap.values().stream()
                .filter(getSuspectsFilter(filter, this::hasSuspects))
                .map(outputChannelDataInfoFactory::createChannelDataInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("channelData", infoList, queryParameters);
    }

    private void putChannelDataFromMetrologyConfiguration(Map<Instant, ChannelReadingWithValidationStatus> outputChannelDataMap,
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
                                clock, channel,
                                validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                                validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)));

                        Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap = channel.toList(effectiveInterval).stream()
                                .collect(Collectors.toMap(Function.identity(), readingWithValidationStatusFactory::createChannelReading, (r1, r2) -> r1, TreeMap::new));

                        // add readings to pre filled channel data map
                        List<IntervalReadingRecord> calculatedReadings = channel.getCalculatedIntervalReadings(effectiveInterval);
                        Map<Instant, IntervalReadingRecord> persistedReadings = toMap(channel.getPersistedIntervalReadings(effectiveInterval));
                        for (Map.Entry<Instant, ChannelReadingWithValidationStatus> entry : preFilledChannelDataMap.entrySet()) {
                            Instant readingTimestamp = entry.getKey();
                            ChannelReadingWithValidationStatus readingWithValidationStatus = entry.getValue();
                            IntervalReadingRecord persistedReading = persistedReadings.get(readingTimestamp);
                            if (persistedReading != null && persistedReading.getValue() != null) {
                                readingWithValidationStatus.setPersistedReadingRecord(persistedReading);
                            }
                            findRecordWithContainingRange(calculatedReadings, readingTimestamp)
                                    .ifPresent(readingWithValidationStatus::setCalculatedReadingRecord);
                        }

                        // add validation statuses to pre filled channel data map
                        List<DataValidationStatus> dataValidationStatuses =
                                evaluator.getValidationStatus(
                                        EnumSet.of(QualityCodeSystem.MDM),
                                        channel,
                                        preFilledChannelDataMap.values().stream()
                                                .map(ChannelReadingWithValidationStatus::getReading)
                                                .flatMap(Functions.asStream())
                                                .collect(Collectors.toList()),
                                        requestedInterval);
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

    private static Optional<IntervalReadingRecord> findRecordWithContainingRange(List<IntervalReadingRecord> records, Instant timestamp) {
        return records
                .stream()
                .filter(record -> record.getTimePeriod().isPresent())
                .filter(record -> record.getTimePeriod().get().contains(timestamp))
                .findFirst();
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
                                channel.estimateReadings(QualityCodeSystem.MDM, filterInRange(estimatedReadings, containerRange, BaseReading::getTimeStamp));
                                channel.editReadings(QualityCodeSystem.MDM, filterInRange(editedReadings, containerRange, BaseReading::getTimeStamp));
                                channel.confirmReadings(QualityCodeSystem.MDM, filterInRange(confirmedReadings, containerRange, BaseReading::getTimeStamp));
                                channel.removeReadings(QualityCodeSystem.MDM, filterInRange(removeCandidates, containerRange, Function.identity()).stream()
                                        .map(channel::getReading)
                                        .flatMap(Functions.asStream())
                                        .collect(Collectors.toList()));
                            });
                        }));

        return Response.status(Response.Status.OK).build();
    }


    private void processInfo(OutputChannelDataInfo channelDataInfo, List<Instant> removeCandidates, List<BaseReading> estimatedReadings, List<BaseReading> editedReadings, List<BaseReading> confirmedReadings) {
        Optional<ReadingQualityComment> readingQualityComment =  resourceHelper.getReadingQualityComment(channelDataInfo.commentId);

        if (!isToBeConfirmed(channelDataInfo) && channelDataInfo.value == null) {
            removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
        }  else {
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
                ((BaseReadingImpl)baseReading).addQuality("3.8." + channelDataInfo.ruleId, this.extractComment(readingQualityComment));
                estimatedReadings.add(baseReading);
            } else {
                ((BaseReadingImpl)baseReading).addQuality("3.7.0", this.extractComment(readingQualityComment));
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
        return Boolean.TRUE.equals(channelDataInfo.isConfirmed);
    }

    private String extractComment(Optional<ReadingQualityComment> readingQualityComment) {
        if (readingQualityComment.isPresent()) {
            return readingQualityComment.get().getComment();
        }
        return null;
    }

    private static <T, C extends Comparable<? super C>> List<T> filterInRange(Collection<T> collection, Range<C> range,
                                                                              Function<? super T, ? extends C> mapping) {
        return collection.stream()
                .filter(item -> range.contains(mapping.apply(item)))
                .collect(Collectors.toList());
    }

    private static Optional<MetrologyContract> findMetrologyContractForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC,
                                                                               MetrologyPurpose metrologyPurpose) {
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
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData/estimate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,
            com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR})
    public List<OutputChannelDataInfo> previewEstimateChannelDataOfOutput(@PathParam("name") String name,
                                                                          @PathParam("purposeId") long contractId,
                                                                          @PathParam("outputId") long outputId,
                                                                          EstimateChannelDataInfo estimateChannelDataInfo) {
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

        return previewCopyFromRefernce(QualityCodeSystem.MDM, channel, referenceChannelDataInfo);
    }

    @GET
    // TODO: 'estimateWithRule' is not a proper path for GET method, maybe 'estimationRules'?
    @Path("/{purposeId}/outputs/{outputId}/channelData/estimateWithRule")
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
                .map(estimationRuleInfoFactory::createEstimationRuleInfo)
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("rules", estimationRuleInfos, queryParameters);
    }

    private List<OutputChannelDataInfo> previewCopyFromRefernce(QualityCodeSystem system, AggregatedChannel channel, ReferenceChannelDataInfo referenceChannelDataInfo) {
        ReadingType readingType = meteringService.getReadingType(referenceChannelDataInfo.readingType)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_READING_TYPE_FOR_MRID, "readingType",referenceChannelDataInfo.readingType));

        UsagePoint usagePoint = resourceHelper.findUsagePointByName(referenceChannelDataInfo.referenceUsagePoint)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_USAGE_POINT_WITH_NAME, "usagePoint", referenceChannelDataInfo.referenceUsagePoint));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyPurpose purpose = resourceHelper.findMetrologyPurpose(referenceChannelDataInfo.referencePurpose)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, "referencePurpose", referenceChannelDataInfo.referencePurpose));
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContract(effectiveMetrologyConfigurationOnUsagePoint, purpose)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.METROLOGYPURPOSE_IS_NOT_FOUND_ON_USAGEPOINT, "referencePurpose", purpose.getName(), usagePoint.getName()));
        ReadingTypeDeliverable readingTypeDeliverable = metrologyContract.getDeliverables().stream().filter(output -> output.getReadingType().equals(readingType))
                .findFirst()
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));
        AggregatedChannel referenceChannel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType())
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));

        if(!matchReadingTypes(referenceChannel.getMainReadingType(), channel.getMainReadingType())){
            throw new LocalizedFieldValidationException(MessageSeeds.READINGTYPES_DONT_MATCH, "readingType");
        }
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw new LocalizedFieldValidationException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, "readingType", readingTypeDeliverable.getName());
        }

        List<OutputChannelDataInfo> resultReadings = new ArrayList<>();
        for (Map.Entry<Range<Instant>, Range<Instant>> range : getCorrectedTimeStampsForReference(referenceChannelDataInfo.startDate, referenceChannelDataInfo.intervals).entrySet()) {
            Optional<IntervalReadingRecord> referenceRecord = referenceChannel.getCalculatedIntervalReadings(range.getValue()).stream().findFirst();
            Optional<IntervalReadingRecord> sourceRecord = channel.getCalculatedIntervalReadings(range.getKey()).stream().findFirst();
            if (sourceRecord.isPresent()) {
                referenceRecord.ifPresent(referenceReading -> {
                    OutputChannelDataInfo channelDataInfo = outputChannelDataInfoFactory.createUpdatedChannelDataInfo(sourceRecord.get(), referenceReading.getValue()
                                    .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - sourceRecord.get()
                                            .getReadingType()
                                            .getMultiplier()
                                            .getMultiplier()),
                                    referenceChannelDataInfo.projectedValue, referenceChannelDataInfo.commentId!=null ? resourceHelper.getReadingQualityComment(referenceChannelDataInfo.commentId) : Optional.empty());
                            channelDataInfo.isProjected = referenceChannelDataInfo.projectedValue;
                            if(referenceChannelDataInfo.allowSuspectData || referenceReading.getReadingQualities().stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                                resultReadings.add(channelDataInfo);
                            }
                        });
            } else {
                referenceRecord.ifPresent(referenceReading -> {
                    OutputChannelDataInfo channelDataInfo = new OutputChannelDataInfo();
                    channelDataInfo.value = referenceReading.getValue()
                            .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - channel.getMainReadingType().getMultiplier().getMultiplier());
                    if (referenceChannelDataInfo.commentId != null) {
                        resourceHelper.getReadingQualityComment(referenceChannelDataInfo.commentId)
                                .ifPresent(comment -> {
                                    channelDataInfo.commentId = comment.getId();
                                    channelDataInfo.commentValue = comment.getComment();
                                });
                    }
                    channelDataInfo.isProjected = referenceChannelDataInfo.projectedValue;
                    channelDataInfo.interval = IntervalInfo.from(range.getKey());
                    if (referenceChannelDataInfo.allowSuspectData || referenceReading.getReadingQualities().stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                        resultReadings.add(channelDataInfo);
                    }
                });
            }
        }
        if (!referenceChannelDataInfo.completePeriod || resultReadings.size() == referenceChannelDataInfo.intervals.size()) {
            return resultReadings;
        } else {
            return Collections.emptyList();
        }
    }

    private Map<Range<Instant>, Range<Instant>> getCorrectedTimeStampsForReference(Instant referenceStartDate,  List<IntervalInfo> intervals){
        Instant startDate = intervals.stream().map(date -> Instant.ofEpochMilli(date.end)).min(Instant::compareTo).get();
        TemporalAmount offset = Duration.between(referenceStartDate, Instant.ofEpochMilli(intervals.get(0).end));
        if(referenceStartDate.isAfter(startDate)){
            return intervals.stream().map(interval -> Range.openClosed(Instant.ofEpochMilli(interval.start),Instant.ofEpochMilli(interval.end)))
                    .collect(Collectors.toMap(Function.identity(),interval -> Range.openClosed(interval.lowerEndpoint().plus(offset), interval.upperEndpoint().plus(offset))));
        } else {
            return intervals.stream().map(interval -> Range.openClosed(Instant.ofEpochMilli(interval.start),Instant.ofEpochMilli(interval.end)))
                    .collect(Collectors.toMap(Function.identity(),interval -> Range.openClosed(interval.lowerEndpoint().minus(offset), interval.upperEndpoint().minus(offset))));
        }
    }

    private boolean matchReadingTypes(ReadingType first, ReadingType second) {
        return first.equals(second)
                || (first.getMacroPeriod().equals(second.getMacroPeriod())
                && first.getAggregate().equals(second.getAggregate())
                && first.getMeasuringPeriod().equals(second.getMeasuringPeriod())
                && first.getAccumulation().equals(second.getAccumulation())
                && first.getFlowDirection().equals(second.getFlowDirection())
                && first.getCommodity().equals(second.getCommodity())
                && first.getMeasurementKind().equals(second.getMeasurementKind())
                && first.getInterharmonic().equals(second.getInterharmonic())
                && first.getArgument().equals(second.getArgument())
                && first.getTou() == second.getTou()
                && first.getCpp() == second.getCpp()
                && first.getPhases().equals(second.getPhases())
                && first.getUnit().equals(second.getUnit())
                && first.getCurrency().equals(second.getCurrency()));
    }

    private List<OutputChannelDataInfo> previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Channel channel, EstimateChannelDataInfo estimateChannelDataInfo) {
        Estimator estimator = estimationHelper.getEstimator(estimateChannelDataInfo);
        ReadingType readingType = channel.getMainReadingType();
        List<Range<Instant>> ranges = estimateChannelDataInfo.intervals.stream()
                .map(info -> Range.openClosed(Instant.ofEpochMilli(info.start), Instant.ofEpochMilli(info.end)))
                .collect(Collectors.toList());
        ImmutableSet<Range<Instant>> blocks = ranges.stream()
                .collect(ImmutableRangeSet::<Instant>builder, ImmutableRangeSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()))
                .build()
                .asRanges();

        List<EstimationResult> results = blocks.stream()
                .map(block -> estimationHelper.previewEstimate(system, channelsContainer, readingType, block, estimator))
                .collect(Collectors.toList());
        return estimationHelper.getChannelDataInfoFromEstimationReports(channel, ranges, results, estimateChannelDataInfo.markAsProjected, resourceHelper.getReadingQualityComment(estimateChannelDataInfo.commentId));
    }

    private Stream<? extends EstimationRule> streamMatchingEstimationRules(ReadingType readingType, MetrologyContract metrologyContract) {
        return usagePointConfigurationService.getEstimationRuleSets(metrologyContract).stream()
                .filter(ruleSet -> QualityCodeSystem.MDM.equals(ruleSet.getQualityCodeSystem()))
                .map(EstimationRuleSet::getRules)
                .flatMap(List::stream)
                .filter(estimationRule -> estimationRule.getReadingTypes().contains(readingType));
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
                .ifPresent(contract -> putRegisterDataFromMetrologyConfiguration(outputRegisterData, contract, readingType, effectiveMC, filter)));
        List<OutputRegisterDataInfo> infoList = outputRegisterData.values().stream().collect(Collectors.toList());

        return PagedInfoList.fromPagedList("registerData", ListPager.of(infoList).from(queryParameters).find(), queryParameters);
    }

    private void putRegisterDataFromMetrologyConfiguration(Map<Instant, OutputRegisterDataInfo> outputRegisterData,
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
                                        clock, channel,
                                        validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                                        validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)));

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
                                    //                    ReadingRecord tempPreviousReadingRecord = null;
                                    readingWithValidationStatus.setPreviousReadingRecord(previousReadingRecord);
                                    if (persistedReading != null && (persistedReading.getValue() != null || persistedReading.getText() != null)) {
                                        readingWithValidationStatus.setPersistedReadingRecord(persistedReading);
                                        // readingWithValidationStatus.setPreviousReadingRecord(previousReadingRecord);
                                        previousReadingRecord = persistedReading;
                                    } else {
                                        ReadingRecord calculatedReading = calculatedReadings.get(readingTimestamp);
                                        if (calculatedReading != null) {
                                            readingWithValidationStatus.setCalculatedReadingRecord(calculatedReading);
                                            // readingWithValidationStatus.setPreviousReadingRecord(previousReadingRecord);
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
                clock,
                channel,
                validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)));
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
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationOnUsagePoint = usagePoint.getEffectiveMetrologyConfiguration(registerDataInfo.timeStamp.minusMillis(1));
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
        MetrologyPurpose metrologyPurpose = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId)
                .getMetrologyPurpose();
        usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                .ifPresent(contract -> effectiveMC.getChannelsContainer(contract)
                        .ifPresent(channelsContainer -> validationService.validate(
                                new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer, contract),
                                purposeInfo.validationInfo.lastChecked))));
        usagePoint.update();
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{purposeId}/estimate")
    @RolesAllowed({com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response estimateMetrologyContract(@PathParam("name") String name, @PathParam("purposeId") long contractId, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, purposeInfo.parent.version);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyPurpose metrologyPurpose = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId)
                .getMetrologyPurpose();
        usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC -> findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                .ifPresent(contract -> effectiveMC.getChannelsContainer(contract)
                        .ifPresent(channelsContainer ->
                                estimationService.estimate(QualityCodeSystem.MDM, channelsContainer, channelsContainer.getRange()))));
        usagePoint.update();
        return Response.status(Response.Status.OK).build();
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
