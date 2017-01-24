package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

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
    private final Thesaurus thesaurus;
    private final MetrologyConfigurationService metrologyConfigurationService;

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
                             Thesaurus thesaurus,
                             MetrologyConfigurationService metrologyConfigurationService) {
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
        this.thesaurus = thesaurus;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getUsagePointPurposes(@PathParam("name") String name, @QueryParam("withValidationTasks") boolean withValidationTasks, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration();
        List<PurposeInfo> purposeInfoList;
        if (effectiveMetrologyConfiguration.isPresent()) {
            purposeInfoList = effectiveMetrologyConfiguration.get().getMetrologyConfiguration().getContracts().stream()
                    .map(metrologyContract -> purposeInfoFactory.asInfo(effectiveMetrologyConfiguration.get(), metrologyContract, withValidationTasks))
                    .sorted(Comparator.comparing(info -> info.name))
                    .collect(Collectors.toList());
        } else {
            purposeInfoList = Collections.emptyList();
        }
        return PagedInfoList.fromCompleteList("purposes", purposeInfoList, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getOutputsOfUsagePointPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryFilter filter,
                                                       @BeanParam JsonQueryParameters queryParameters) {
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
            if (interval.isConnected(upToNow)) {
                if (!interval.intersection(upToNow).isEmpty()) {
                    List<OutputInfo> outputInfoList = metrologyContract.getDeliverables()
                            .stream()
                            .map(deliverable -> outputInfoFactory.asInfo(deliverable, effectiveMetrologyConfigurationOnUsagePoint, metrologyContract,
                                    getUsagePointAdjustedDataRange(usagePoint, interval.intersection(upToNow)).orElse(Range.openClosed(now, now))))
                            .sorted(Comparator.comparing(info -> info.name))
                            .collect(Collectors.toList());
                    return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
                }
            }
            throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
        } else {
            List<OutputInfo> outputInfoList = metrologyContract.getDeliverables()
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
    public OutputInfo getOutputsOfPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId) {
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
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }
        List<OutputChannelDataInfo> outputChannelDataInfoList = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = getRequestedInterval(usagePoint, filter);
            if (requestedInterval != null) {
                EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration().get();
                ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get();
                Optional<Range<Instant>> optionalIntervalWithData = Optional.of(channelsContainer)
                        .map(Effectivity::getInterval)
                        .map(Interval::toOpenClosedRange)
                        .filter(requestedInterval::isConnected)
                        .map(requestedInterval::intersection)
                        .filter(not(Range::isEmpty));
                Range<Instant> modifiedInterval = optionalIntervalWithData.orElse(requestedInterval);
                AggregatedChannel channel = effectiveMetrologyConfiguration.getAggregatedChannel(metrologyContract, readingTypeDeliverable
                        .getReadingType()).get();
                TemporalAmount intervalLength = channel.getIntervalLength().get();
                ValidationEvaluator evaluator = validationService.getEvaluator();
                ReadingWithValidationStatus.Builder builder = ReadingWithValidationStatus.builder(
                        channel,
                        validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                        validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)));
                Map<Instant, ReadingWithValidationStatus<IntervalReadingRecord>> preFilledChannelDataMap = channel.toList(modifiedInterval)
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), timeStamp -> builder.from(ZonedDateTime.ofInstant(timeStamp, clock.getZone()), intervalLength)));

                // add readings to pre filled channel data map
                Map<Instant, IntervalReadingRecord> intervalReadings = channel.getCalculatedIntervalReadings(modifiedInterval)
                        .stream()
                        .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                                .identity()));
                Map<Instant, IntervalReadingRecord> persistedIntervalReadings = channel.getPersistedIntervalReadings(modifiedInterval)
                        .stream()
                        .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                                .identity()));
                for (Instant intervalReadingRecordTimestamp : preFilledChannelDataMap.keySet()) {
                    ReadingWithValidationStatus<IntervalReadingRecord> readingWithValidationStatus = preFilledChannelDataMap
                            .get(intervalReadingRecordTimestamp);
                    if (readingWithValidationStatus != null) {
                        if (persistedIntervalReadings.containsKey(intervalReadingRecordTimestamp) && persistedIntervalReadings
                                .get(intervalReadingRecordTimestamp)
                                .getValue() != null) {
                            readingWithValidationStatus.setReadingRecord(persistedIntervalReadings.get(intervalReadingRecordTimestamp));
                            readingWithValidationStatus.setCalculatedReadingRecord(intervalReadings.get(intervalReadingRecordTimestamp));
                            readingWithValidationStatus.setPersistedReadingRecord(persistedIntervalReadings.get(intervalReadingRecordTimestamp));
                        } else if (intervalReadings.containsKey(intervalReadingRecordTimestamp)) {
                            readingWithValidationStatus.setReadingRecord(intervalReadings.get(intervalReadingRecordTimestamp));
                        }
                    }
                }

                // add validation statuses to pre filled channel data map
                List<DataValidationStatus> dataValidationStatuses = evaluator
                        .getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, Stream
                                .concat(persistedIntervalReadings.values().stream(), intervalReadings.values().stream())
                                .collect(Collectors.toList()), modifiedInterval);
                for (DataValidationStatus dataValidationStatus : dataValidationStatuses) {
                    ReadingWithValidationStatus<IntervalReadingRecord> readingWithValidationStatus = preFilledChannelDataMap
                            .get(dataValidationStatus.getReadingTimestamp());
                    if (readingWithValidationStatus != null) {
                        readingWithValidationStatus.setValidationStatus(dataValidationStatus);
                    }
                }

                outputChannelDataInfoList = preFilledChannelDataMap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getKey)))
                        .map(Map.Entry::getValue)
                        .map(outputChannelDataInfoFactory::createChannelDataInfo)
                        .collect(Collectors.toList());
            }
        }
        return PagedInfoList.fromCompleteList("channelData", outputChannelDataInfoList, queryParameters);
    }

    @PUT
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public Response editChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                            @BeanParam JsonQueryParameters queryParameters, List<OutputChannelDataInfo> channelDataInfos) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }

        List<BaseReading> editedReadings = new ArrayList<>();
        List<BaseReading> confirmedReadings = new ArrayList<>();
        List<Instant> removeCandidates = new ArrayList<>();

        channelDataInfos.forEach((channelDataInfo) -> {
            if (!isToBeConfirmed(channelDataInfo) && channelDataInfo.value == null) {
                removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
            } else {
                if (channelDataInfo.value != null) {
                    editedReadings.add(channelDataInfo.createNew());
                }
                if (isToBeConfirmed(channelDataInfo)) {
                    confirmedReadings.add(channelDataInfo.createConfirm());
                }
            }
        });

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .get();
        ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();

        Optional<Instant> currentLastChecked = validationService.getLastChecked(channel);
        channel.editReadings(QualityCodeSystem.MDM, editedReadings);
        channel.confirmReadings(QualityCodeSystem.MDM, confirmedReadings);
        channel.removeReadings(QualityCodeSystem.MDM, removeCandidates.stream()
                .map(channel::getReading)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        if (!editedReadings.isEmpty() || !removeCandidates.isEmpty()) {
            Instant lastChecked = Stream.concat(editedReadings.stream().map(BaseReading::getTimeStamp), removeCandidates
                    .stream())
                    .min(Instant::compareTo)
                    .map(r -> r.minusSeconds(1L))
                    .get();
            if (currentLastChecked.filter(instant -> lastChecked.isBefore(instant.plus(channel.getIntervalLength()
                    .get()))).isPresent()) {
                validationService.updateLastChecked(channel, lastChecked);
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    private boolean isToBeConfirmed(OutputChannelDataInfo channelDataInfo) {
        return Boolean.TRUE.equals(channelDataInfo.isConfirmed);
    }

    private Range<Instant> getRequestedInterval(UsagePoint usagePoint, JsonQueryFilter filter) {
        Range<Instant> sourceRange = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
        return getUsagePointAdjustedDataRange(usagePoint, sourceRange).orElse(null);
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public List<OutputChannelDataInfo> previewEstimateChannelDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                                          EstimateChannelDataInfo estimateChannelDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .get();
        ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();

        return previewEstimate(QualityCodeSystem.MDM, channelsContainer, channel, estimateChannelDataInfo);
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
        return estimationHelper.getChannelDataInfoFromEstimationReports(channel, ranges, results);
    }


    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                 @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_REGULAR, outputId);
        }
        List<OutputRegisterDataInfo> outputRegisterData = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            ChannelsContainer channelsContainer = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                    .get();
            if (channelsContainer.getRange().isConnected(requestedInterval)) {
                Range<Instant> effectiveInterval = channelsContainer.getRange().intersection(requestedInterval);
                AggregatedChannel channel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable
                        .getReadingType())
                        .get();
                ValidationEvaluator evaluator = validationService.getEvaluator();

                ReadingWithValidationStatus.Builder builder = ReadingWithValidationStatus.builder(
                        channel,
                        validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                        validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)));


                // add readings to pre filled register data map
                Map<Instant, ReadingRecord> readings = channel.getCalculatedRegisterReadings(effectiveInterval)
                        .stream()
                        .collect(Collectors.toMap((Function<ReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                                .identity()));
                Map<Instant, ReadingRecord> persistedReadings = channel.getPersistedRegisterReadings(effectiveInterval)
                        .stream()
                        .collect(Collectors.toMap((Function<ReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                                .identity()));
                Map<Instant, ReadingWithValidationStatus<ReadingRecord>> preFilledChannelDataMap = Stream
                        .concat(persistedReadings.keySet().stream(), readings.keySet().stream())
                        .distinct()
                        .collect(Collectors.toMap(Function.identity(), timeStamp -> builder.from(ZonedDateTime.ofInstant(timeStamp, clock
                                .getZone())), (a, b) -> a));

                for (Instant timeStamp : preFilledChannelDataMap.keySet()) {
                    ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus = preFilledChannelDataMap.get(timeStamp);
                    if (readingWithValidationStatus != null) {
                        if (persistedReadings.containsKey(timeStamp) && (persistedReadings.get(timeStamp)
                                .getValue() != null || persistedReadings.get(timeStamp).getText() != null)) {
                            readingWithValidationStatus.setReadingRecord(persistedReadings.get(timeStamp));
                            readingWithValidationStatus.setCalculatedReadingRecord(readings.get(timeStamp));
                            readingWithValidationStatus.setPersistedReadingRecord(persistedReadings.get(timeStamp));
                        } else {
                            readingWithValidationStatus.setReadingRecord(readings.get(timeStamp));
                        }
                    }
                }

                // add validation statuses to pre filled register data map
                List<DataValidationStatus> dataValidationStatuses = evaluator
                        .getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, Stream
                                .concat(persistedReadings.values().stream(), readings.values().stream())
                                .collect(Collectors.toList()), effectiveInterval);
                for (DataValidationStatus dataValidationStatus : dataValidationStatuses) {
                    ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus = preFilledChannelDataMap.get(dataValidationStatus
                            .getReadingTimestamp());
                    if (readingWithValidationStatus != null) {
                        readingWithValidationStatus.setValidationStatus(dataValidationStatus);
                    }
                }

                outputRegisterData = preFilledChannelDataMap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getKey)))
                        .map(Map.Entry::getValue)
                        .map(reading -> outputRegisterDataInfoFactory.createRegisterDataInfo(reading, readingTypeDeliverable))
                        .collect(Collectors.toList());
            }
        }
        return PagedInfoList.fromPagedList("registerData", outputRegisterData, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData/{requestedTimeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public Response getSingleRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                  @PathParam("requestedTimeStamp") long requestedTimeStamp, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_REGULAR, outputId);
        }
        ChannelsContainer channelsContainer = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                .get();
        AggregatedChannel channel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable
                .getReadingType())
                .get();
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Instant requestedTime = Instant.ofEpochMilli(requestedTimeStamp);

        ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus = ReadingWithValidationStatus.builder(
                channel,
                validationStatusFactory.isValidationActive(metrologyContract, Collections.singletonList(channel)),
                validationStatusFactory.getLastCheckedForChannels(evaluator, channelsContainer, Collections.singletonList(channel)))
                .from(ZonedDateTime.ofInstant(requestedTime, clock.getZone()));

        Optional<ReadingRecord> reading = channel.getCalculatedRegisterReadings(Range.openClosed(requestedTime.minusMillis(1L), requestedTime))
                .stream()
                .findFirst();
        Optional<ReadingRecord> persistedReading = channel.getPersistedRegisterReadings(Range.openClosed(requestedTime.minusMillis(1L), requestedTime))
                .stream()
                .findFirst();

        if (persistedReading.isPresent() && persistedReading.get().getValue() != null) {
            readingWithValidationStatus.setReadingRecord(persistedReading.orElse(null));
            readingWithValidationStatus.setCalculatedReadingRecord(reading.orElse(null));
            readingWithValidationStatus.setPersistedReadingRecord(persistedReading.orElse(null));
        } else if (reading.isPresent()) {
            readingWithValidationStatus.setReadingRecord(reading.get());
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        evaluator
                .getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel,
                        Stream.of(persistedReading, reading)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()))
                .stream().findFirst().ifPresent(readingWithValidationStatus::setValidationStatus);

        return Response.ok(outputRegisterDataInfoFactory.createRegisterDataInfo(readingWithValidationStatus, readingTypeDeliverable))
                .build();
    }

    @PUT
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public OutputRegisterDataInfo editRegisterDataOfOutput(@PathParam("name") String name, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                           @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryFilter filter, OutputRegisterDataInfo registerDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ChannelsContainer channelsContainer = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .get()
                .getChannelsContainer(metrologyContract)
                .get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
        if(registerDataInfo instanceof BillingOutputRegisterDataInfo && ((BillingOutputRegisterDataInfo) registerDataInfo).interval.start > ((BillingOutputRegisterDataInfo) registerDataInfo).interval.end){
            throw new LocalizedFieldValidationException(MessageSeeds.INTERVAL_END_BEFORE_START, "interval.end");
        }
        BaseReading reading = registerDataInfo.createNew(readingTypeDeliverable.getReadingType());
        if ((registerDataInfo instanceof NumericalOutputRegisterDataInfo && NumericalOutputRegisterDataInfo.class.cast(registerDataInfo).isConfirmed != null && NumericalOutputRegisterDataInfo.class
                .cast(registerDataInfo).isConfirmed)) {
            channel.confirmReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
        } else {
            Optional<Instant> currentLastChecked = validationService.getLastChecked(channel);
            channel.editReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
            Instant lastChecked = reading.getTimeStamp().minusSeconds(1L);
            if (currentLastChecked.filter(lastChecked::isBefore).isPresent()) {
                validationService.updateLastChecked(channel, lastChecked);
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
                                                             @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryFilter filter, OutputRegisterDataInfo registerDataInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, name);
        ChannelsContainer channelsContainer = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .get()
                .getChannelsContainer(metrologyContract)
                .get();
        Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
        Optional<Instant> currentLastChecked = validationService.getLastChecked(channel);
        channel.getReading(Instant.ofEpochMilli(timeStamp))
                .ifPresent(reading -> channel.removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading)));
        Instant lastChecked = Instant.ofEpochMilli(timeStamp).minusSeconds(1L);
        if (currentLastChecked.filter(lastChecked::isBefore).isPresent()) {
            validationService.updateLastChecked(channel, lastChecked);
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{purposeId}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response validateOrEstimateMetrologyContract(@PathParam("name") String name, @PathParam("purposeId") long contractId, @QueryParam("upVersion") long upVersion,
                                                        @QueryParam("action") @DefaultValue("") String action, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, upVersion);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, contractId);
        usagePoint.update();
        switch (action) {
            case "validate":
                effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                        .ifPresent(channelsContainer ->
                                validationService.validate(
                                        new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer, metrologyContract),
                                        purposeInfo.validationInfo.lastChecked));

                break;
            case "estimate":
                effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                        .ifPresent(channelsContainer ->
                                estimationService.estimate(QualityCodeSystem.MDM, channelsContainer, channelsContainer.getRange()));
                break;
            default:
                throw exceptionFactory.newException(MessageSeeds.WRONG_ACTION_SPECIFIED);
        }

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

        MetrologyContract metrologyContract = effectiveMC.getMetrologyConfiguration().getContracts()
                .stream()
                .filter(mc -> !effectiveMC.getChannelsContainer(mc, clock.instant()).isPresent())
                .filter(mc -> !mc.getDeliverables().isEmpty())
                .filter(mc -> mc.getId() == contractId)
                .filter(mc -> !mc.isMandatory())
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.CANNOT_ACTIVATE_METROLOGY_PURPOSE));

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
        if (effectiveMC.getChannelsContainer(metrologyContract, clock.instant()).isPresent()) {
            effectiveMC.deactivateOptionalMetrologyContract(metrologyContract, clock.instant());
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
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(usagePoint, contractId);
        List<DataValidationTask> validationTasks = validationService.findValidationTasks()
                .stream()
                .filter(task -> task.getQualityCodeSystem().equals(QualityCodeSystem.MDM) && task.getMetrologyPurpose().get().getId() == metrologyContract.getMetrologyPurpose().getId())
                .collect(Collectors.toList());

        List<DataValidationTaskInfo> dataValidationTasks = validationTasks
                .stream()
                .map(DataValidationTask::getUsagePointGroup)
                .filter(Optional::isPresent)
                .map(Optional::get)
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
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(usagePoint, contractId);
        List<EstimationTask> estimationTasks = estimationService.findEstimationTasks(QualityCodeSystem.MDM)
                .stream()
                .filter(task -> task.getMetrologyPurpose().get().getId() == metrologyContract.getMetrologyPurpose().getId())
                .collect(Collectors.toList());

        List<EstimationTaskInfo> dataEstimationTasks = estimationTasks
                .stream()
                .map(EstimationTask::getUsagePointGroup)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .filter(usagePointGroup -> isMember(usagePoint, usagePointGroup))
                .flatMap(usagePointGroup -> estimationTasks.stream()
                        .filter(estimationTask -> estimationTask.getUsagePointGroup()
                                .filter(usagePointGroup::equals)
                                .isPresent()))
                .map(estimationTask -> new EstimationTaskInfo(estimationTask, thesaurus, timeService))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("dataEstimationTasks", dataEstimationTasks, queryParameters);
    }

    private boolean isMember(UsagePoint usagePoint, UsagePointGroup usagePointGroup) {
        return !meteringService.getUsagePointQuery()
                .select(Where.where("id").isEqualTo(usagePoint.getId())
                        .and(ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "id")), 1, 1)
                .isEmpty();
    }
}