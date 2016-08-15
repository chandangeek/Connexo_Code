package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
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

public class UsagePointOutputResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    private final ValidationService validationService;
    private final OutputInfoFactory outputInfoFactory;
    private final OutputChannelDataInfoFactory outputChannelDataInfoFactory;
    private final OutputRegisterDataInfoFactory outputRegisterDataInfoFactory;
    private final PurposeInfoFactory purposeInfoFactory;

    private static final String INTERVAL_START = "intervalStart";
    private static final String INTERVAL_END = "intervalEnd";

    @Inject
    public UsagePointOutputResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory,
                                    ValidationService validationService,
                                    OutputInfoFactory outputInfoFactory,
                                    OutputChannelDataInfoFactory outputChannelDataInfoFactory,
                                    OutputRegisterDataInfoFactory outputRegisterDataInfoFactory,
                                    PurposeInfoFactory purposeInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.validationService = validationService;
        this.outputInfoFactory = outputInfoFactory;
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
        this.outputRegisterDataInfoFactory = outputRegisterDataInfoFactory;
        this.purposeInfoFactory = purposeInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getUsagePointPurposes(@PathParam("mRID") String mRID, @QueryParam("withValidationTasks") boolean withValidationTasks, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration();
        List<PurposeInfo> purposeInfoList;
        if (effectiveMetrologyConfiguration.isPresent()) {
            purposeInfoList = effectiveMetrologyConfiguration.get().getMetrologyConfiguration().getContracts().stream()
                    .map(metrologyContract -> purposeInfoFactory.asInfo(effectiveMetrologyConfiguration.get(), metrologyContract, withValidationTasks))
                    .collect(Collectors.toList());
        } else {
            purposeInfoList = Collections.emptyList();
        }
        return PagedInfoList.fromCompleteList("purposes", purposeInfoList, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getOutputsOfUsagePointPurpose(@PathParam("mRID") String mRID, @PathParam("purposeId") long contractId,@BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        List<OutputInfo> outputInfoList = metrologyContract.getDeliverables()
                .stream()
                .map(deliverable -> outputInfoFactory.asInfo(deliverable, effectiveMC, metrologyContract))
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("outputs", outputInfoList, queryParameters);
    }

    @GET
    @Path("/{purposeId}/outputs/{outputId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public OutputInfo getOutputsOfPurpose(@PathParam("mRID") String mRID, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, mRID);
        return outputInfoFactory.asFullInfo(readingTypeDeliverable, effectiveMC, metrologyContract);
    }

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/channelData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getChannelDataOfOutput(@PathParam("mRID") String mRID, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, mRID);
        if (!readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_IRREGULAR, outputId);
        }
        List<OutputChannelDataInfo> outputChannelDataInfoList = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            ChannelsContainer channelsContainer = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getChannelsContainer(metrologyContract).get();
            if (channelsContainer.getRange().isConnected(requestedInterval)) {
                Range<Instant> effectiveInterval = channelsContainer.getRange().intersection(requestedInterval);
                Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
                TemporalAmount intervalLength = readingTypeDeliverable.getReadingType().getIntervalLength().get();
                Map<Instant, IntervalReadingWithValidationStatus> preFilledChannelDataMap = channel.toList(effectiveInterval).stream()
                        .collect(Collectors.toMap(Function.identity(), timeStamp -> new IntervalReadingWithValidationStatus(timeStamp, intervalLength)));

                // add readings to pre filled channel data map
                List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(effectiveInterval);
                for (IntervalReadingRecord intervalReadingRecord : intervalReadings) {
                    IntervalReadingWithValidationStatus readingWithValidationStatus = preFilledChannelDataMap.get(intervalReadingRecord.getTimeStamp());
                    readingWithValidationStatus.setIntervalReadingRecord(intervalReadingRecord);
                }

                // add validation statuses to pre filled channel data map
                List<DataValidationStatus> dataValidationStatuses = validationService.getEvaluator()
                        .getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, intervalReadings, effectiveInterval);
                for (DataValidationStatus dataValidationStatus : dataValidationStatuses) {
                    IntervalReadingWithValidationStatus readingWithValidationStatus = preFilledChannelDataMap.get(dataValidationStatus.getReadingTimestamp());
                    readingWithValidationStatus.setValidationStatus(dataValidationStatus);
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

    @GET
    @Transactional
    @Path("/{purposeId}/outputs/{outputId}/registerData")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public PagedInfoList getRegisterDataOfOutput(@PathParam("mRID") String mRID, @PathParam("purposeId") long contractId, @PathParam("outputId") long outputId,
                                                 @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        ReadingTypeDeliverable readingTypeDeliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, mRID);
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            throw exceptionFactory.newException(MessageSeeds.THIS_OUTPUT_IS_REGULAR, outputId);
        }
        List<OutputRegisterDataInfo> outputRegisterData = new ArrayList<>();
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            ChannelsContainer channelsContainer = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getChannelsContainer(metrologyContract).get();
            if (channelsContainer.getRange().isConnected(requestedInterval)) {
                Range<Instant> effectiveInterval = channelsContainer.getRange().intersection(requestedInterval);
                Channel channel = channelsContainer.getChannel(readingTypeDeliverable.getReadingType()).get();
                outputRegisterData = channel.getRegisterReadings(effectiveInterval).stream()
                        .sorted(Comparator.comparing(ReadingRecord::getTimeStamp).reversed())
                        .map(outputRegisterDataInfoFactory::createRegisterDataInfo)
                        .collect(Collectors.toList());
                outputRegisterData = ListPager.of(outputRegisterData).from(queryParameters).find();
            }
        }
        return PagedInfoList.fromPagedList("registerData", outputRegisterData, queryParameters);
    }

    @PUT
    @Path("/{purposeId}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response validateMetrologyContract(@PathParam("mRID") String mRID, @PathParam("purposeId") long purposeId, @QueryParam("upVersion") long upVersion, PurposeInfo purposeInfo) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByMrIdOrThrowException(mRID, upVersion);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, purposeId);
        usagePoint.update();
        effectiveMC.getChannelsContainer(metrologyContract)
                .ifPresent(channelsContainer -> validationService.validate(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer)
                        .setMetrologyContract(metrologyContract), purposeInfo.validationInfo.lastChecked));
        return Response.status(Response.Status.OK).build();
    }
}
