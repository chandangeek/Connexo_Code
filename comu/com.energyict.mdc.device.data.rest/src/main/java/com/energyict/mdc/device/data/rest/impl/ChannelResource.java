/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileJournalReading;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@DeviceStatesRestricted(
        value = {DefaultState.DECOMMISSIONED},
        methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE},
        ignoredUserRoles = {Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
public class ChannelResource {
    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";
    private final ExceptionFactory exceptionFactory;
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final ValidationInfoFactory validationInfoFactory;
    private final IssueDataValidationService issueDataValidationService;
    private final EstimationHelper estimationHelper;
    private final TopologyService topologyService;
    private final MeteringService meteringService;

    @Inject
    public ChannelResource(ExceptionFactory exceptionFactory, Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, ValidationInfoFactory validationInfoFactory, IssueDataValidationService issueDataValidationService, EstimationHelper estimationHelper, TopologyService topologyService, MeteringService meteringService) {
        this.exceptionFactory = exceptionFactory;
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.validationInfoFactory = validationInfoFactory;
        this.issueDataValidationService = issueDataValidationService;
        this.estimationHelper = estimationHelper;
        this.topologyService = topologyService;
        this.meteringService = meteringService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannels(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(name, (d -> this.getFilteredChannels(d, filter)), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannel(@PathParam("name") String name, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        return channelHelper.get().getChannel(() -> channel);
    }

    @PUT
    @Transactional
    @Path("/{channelid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE})
    public Response updateChannel(@PathParam("name") String name, @PathParam("channelid") long channelId, ChannelInfo channelInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        Channel.ChannelUpdater channelUpdater = device.getChannelUpdaterFor(channel);
        channelUpdater.setNumberOfFractionDigits(channelInfo.overruledNbrOfFractionDigits);
        channelUpdater.setOverflowValue(channelInfo.overruledOverflowValue);
        channelUpdater.setObisCode(channelInfo.overruledObisCode);
        channelUpdater.update();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getChannelCustomProperties(@PathParam("name") String name, @PathParam("channelId") long channelId, @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getChannelCustomPropertySetInfo(channel, this.clock.instant());
        return PagedInfoList
                .fromCompleteList(
                        "customproperties",
                        customPropertySetInfo != null ? Collections.singletonList(customPropertySetInfo) : new ArrayList<>(),
                        queryParameters);
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public CustomPropertySetInfo getChannelCustomProperties(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getChannelCustomPropertySetInfo(channel, this.clock.instant());
        if (customPropertySetInfo.id != cpsId) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        return customPropertySetInfo;
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public CustomPropertySetInfo getChannelCustomProperties(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") Long timeStamp) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getChannelCustomPropertySetInfo(channel, Instant.ofEpochMilli(timeStamp));
        if (customPropertySetInfo.id != cpsId) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        return customPropertySetInfo;
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getChannelCustomPropertiesHistory(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        return PagedInfoList.fromCompleteList("versions", resourceHelper.getVersionedCustomPropertySetHistoryInfos(channel, cpsId), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public IntervalInfo getCurrentTimeInterval(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(channel, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(channel, cpsId, resourceHelper.getTimeRange(startTime, endTime));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenUpdate(channel, cpsId, resourceHelper.getTimeRange(startTime, endTime), Instant.ofEpochMilli(timeStamp));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response changeRegisterCustomProperty(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        resourceHelper.lockLoadProfileTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockChannelSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, channel);
        resourceHelper.setChannelCustomPropertySet(channel, customPropertySetInfo);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response addChannelCustomAttributeVersioned(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        resourceHelper.lockLoadProfileTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockChannelSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, channel);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenCreate(channel, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.addChannelCustomPropertySetVersioned(channel, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/{channelId}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editChannelCustomAttributeVersioned(@PathParam("name") String name, @PathParam("channelId") long channelId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        resourceHelper.lockLoadProfileTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockChannelSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, channel);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenUpdate(channel, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime), Instant.ofEpochMilli(timeStamp))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.setChannelCustomPropertySetVersioned(channel, cpsId, customPropertySetInfo, Instant.ofEpochMilli(timeStamp));
        return Response.ok().build();
    }

    private List<Channel> getFilteredChannels(Device device, JsonQueryFilter filter) {
        Predicate<String> filterByLoadProfileName = getStringListFilterIfAvailable("loadProfileName", filter);
        Predicate<String> filterByChannelName = getStringFilterIfAvailable("channelName", filter);
        return device.getLoadProfiles().stream()
                .filter(l -> filterByLoadProfileName.test(l.getLoadProfileSpec().getLoadProfileType().getName()))
                .flatMap(l -> l.getChannels().stream())
                .filter(c -> filterByChannelName.test(c.getReadingType().getFullAliasName()))
                .collect(Collectors.toList());
    }

    private Predicate<String> getStringFilterIfAvailable(String name, JsonQueryFilter filter) {
        if (filter.hasProperty(name)) {
            Pattern pattern = getFilterPattern(filter.getString(name));
            if (pattern != null) {
                return s -> pattern.matcher(s).matches();
            }
        }
        return s -> true;
    }

    private Predicate<String> getStringListFilterIfAvailable(String name, JsonQueryFilter filter) {
        if (filter.hasProperty(name)) {
            List<String> entries = filter.getStringList(name);
            List<Pattern> patterns = new ArrayList<>();
            for (String entry : entries) {
                patterns.add(getFilterPattern(entry));
            }
            if (!patterns.isEmpty()) {
                return s -> {
                    boolean match = false;
                    for (Pattern pattern : patterns) {
                        match = match || pattern.matcher(s).matches();
                        if (match) {
                            break;
                        }
                    }
                    return match;
                };
            }
        }
        return s -> true;
    }

    private Pattern getFilterPattern(String filter) {
        if (filter != null) {
            filter = Pattern.quote(filter.replace('%', '*'));
            return Pattern.compile(filter.replaceAll("([*?])", "\\\\E\\.$1\\\\Q"));
        }
        return null;
    }

    @GET
    @Transactional
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response getChannelData(
            @PathParam("name") String name,
            @PathParam("channelid") long channelId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive();
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> range = Ranges.closedOpen(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));

            // Always do it via the topologyService, if for some reason the performance is slow, check if you can optimize it for
            // devices which are not dataloggers
            List<Pair<Channel, Range<Instant>>> channelTimeLine = topologyService.getDataLoggerChannelTimeLine(channel, range);
            List<ChannelDataInfo> infos = channelTimeLine.stream()
                    .flatMap(channelRangePair -> {
                        Channel channelWithData = channelRangePair.getFirst();
                        List<LoadProfileReading> loadProfileReadings = channelWithData.getChannelData(Interval.of(channelRangePair.getLast()).toOpenClosedRange());
                        return loadProfileReadings.stream()
                                .map(loadProfileReading -> deviceDataInfoFactory.createChannelDataInfo(channelWithData, loadProfileReading, isValidationActive, deviceValidation, channel.equals(channelWithData) ? null : channelWithData
                                        .getDevice()));
                    })
                    .filter(resourceHelper.getSuspectsFilter(filter, this::hasSuspects))
                    .collect(Collectors.toList());
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Transactional
    @Path("/{channelid}/historydata")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response getChannelHistoryData(
            @PathParam("name") String name,
            @PathParam("channelid") long channelId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive();
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> range = Ranges.closedOpen(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            Range<Instant> changed = Ranges.closedOpen(filter.getInstant("changedStart"), filter.getInstant("changedEnd"));

            // Always do it via the topologyService, if for some reason the performance is slow, check if you can optimize it for
            // devices which are not dataloggers
            List<Pair<Channel, Range<Instant>>> channelTimeLine = topologyService.getDataLoggerChannelTimeLine(channel, range);
            List<ChannelHistoryDataInfo> infos = channelTimeLine.stream()
                    .flatMap(channelRangePair -> {
                        Channel channelWithData = channelRangePair.getFirst();
                        List<LoadProfileJournalReading> loadProfileJournalReadings = channelWithData.getChannelWithHistoryData(Interval.of(channelRangePair.getLast()).toOpenClosedRange(), changed);
                        return loadProfileJournalReadings.stream()
                                .map(loadProfileJournalReading -> deviceDataInfoFactory.createChannelHistoryDataInfo(channelWithData, loadProfileJournalReading, isValidationActive, deviceValidation, channel
                                        .equals(channelWithData) ? null : channelWithData
                                        .getDevice()));
                    })
                    .filter(resourceHelper.getSuspectsFilter(filter, this::hasSuspects))
                    .collect(Collectors.toList());
            List<ChannelHistoryDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{channelid}/data/{epochMillis}/validation")
    public Response getValidationData(
            @PathParam("name") String name,
            @PathParam("channelid") long channelId,
            @PathParam("epochMillis") long epochMillis) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        Instant to = Instant.ofEpochMilli(epochMillis);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(to, channel.getDevice().getZone());
        Instant from = zonedDateTime.minus(channel.getInterval().asTemporalAmount()).toInstant();
        Range<Instant> range = Ranges.openClosed(from, to);
        List<Pair<Channel, Range<Instant>>> channelTimeLine = topologyService.getDataLoggerChannelTimeLine(channel, range);
        Optional<VeeReadingInfo> veeReadingInfo = channelTimeLine.stream()
                .map(channelRangePair -> {
                            Channel channelWithData = channelRangePair.getFirst();

                    Optional<LoadProfileReading> loadProfileReading = channelWithData.getChannelData(range).stream().findAny();
                    if (loadProfileReading.isPresent()) {
                        DeviceValidation deviceValidation = channelWithData.getDevice().forValidation();

                        boolean isValidationActive = deviceValidation.isValidationActive();

                        Optional<DataValidationStatus> dataValidationStatus = loadProfileReading.flatMap(lpReading -> lpReading.getChannelValidationStates()
                                .entrySet()
                                .stream()
                                .map(Map.Entry::getValue)
                                .findFirst());

                        if (dataValidationStatus.isPresent()) {
                            IntervalReadingRecord channelReading = loadProfileReading.flatMap(lpReading -> lpReading.getChannelValues()
                                    .entrySet()
                                    .stream()
                                    .map(Map.Entry::getValue)
                                    .findFirst())
                                    .orElse(null);// There can be only one channel (or no channel at all if the channel has no dta for this interval)
                            return validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, dataValidationStatus.get(), deviceValidation, channelReading, isValidationActive);
                        } else {
                            return new VeeReadingInfo();

                        }
                    } else {
                        return new VeeReadingInfo();
                    }
                        }
                ).findAny();
        return Response.ok(veeReadingInfo.orElse(new VeeReadingInfo())).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{channelid}/data/{epochMillis}/{historyEpochMillis}/validation")
    public Response getValidationHistoryData(
            @PathParam("name") String name,
            @PathParam("channelid") long channelId,
            @PathParam("epochMillis") long epochMillis,
            @PathParam("historyEpochMillis") long historyEpochMillis) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);

        Instant to = Instant.ofEpochMilli(epochMillis);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(to, channel.getDevice().getZone());
        Instant from = zonedDateTime.minus(channel.getInterval().asTemporalAmount()).toInstant();
        Range<Instant> range = Ranges.openClosed(from, to);

        Instant changedTo = Instant.ofEpochMilli(historyEpochMillis);
        ZonedDateTime changedZonedDateTime = ZonedDateTime.ofInstant(changedTo, channel.getDevice().getZone());
        Instant changedFrom = changedTo.minus(1L, ChronoUnit.SECONDS);
        Range<Instant> changedRange = Ranges.openClosed(changedFrom, changedTo);

        List<Pair<Channel, Range<Instant>>> channelTimeLine = topologyService.getDataLoggerChannelTimeLine(channel, range);
        Optional<VeeReadingInfo> veeReadingInfo = channelTimeLine.stream()
                .map(channelRangePair -> {
                    Channel channelWithData = channelRangePair.getFirst();
                    Optional<LoadProfileJournalReading> loadProfileJournalReading = channelWithData.getChannelWithHistoryData(range, changedRange).stream().findAny();
                    if (loadProfileJournalReading.isPresent()) {
                        DeviceValidation deviceValidation = channelWithData.getDevice().forValidation();
                        boolean isValidationActive = deviceValidation.isValidationActive();

                        Optional<DataValidationStatus> dataValidationStatus = loadProfileJournalReading.flatMap(lpReading -> lpReading.getChannelValidationStates()
                                .entrySet()
                                .stream()

                                .map(Map.Entry::getValue)
                                .findFirst());

                        if (dataValidationStatus.isPresent()) {
                            IntervalReadingRecord channelReading = loadProfileJournalReading
                                    .flatMap(lpReading -> lpReading.getChannelValues()
                                            .entrySet()
                                            .stream()
                                            .map(Map.Entry::getValue)
                                            .findFirst())
                                    .orElse(null);// There can be only one channel (or no channel at all if the channel has no dta for this interval)
                            List<ReadingQualityRecord> readingQualities = loadProfileJournalReading.get().getReadingQualities().entrySet().stream()
                                    .map(channelListEntry -> channelListEntry.getValue())
                                    .flatMap(List::stream).collect(Collectors.toList());
                            //  return validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, dataValidationStatus.get(), deviceValidation, channelReading, readingQualities, isValidationActive);
                            return validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, dataValidationStatus.get(), deviceValidation, channelReading, readingQualities, isValidationActive);

                        } else {
                            return new VeeReadingInfo();

                        }
                    } else {
                        return new VeeReadingInfo();
                    }
                        }
                ).findAny();
        return Response.ok(veeReadingInfo.orElse(new VeeReadingInfo())).build();
    }

    private boolean hasSuspects(ChannelDataInfo info) {
        return ValidationStatus.SUSPECT.equals(info.mainValidationInfo.validationResult) ||
                (info.bulkValidationInfo != null && ValidationStatus.SUSPECT.equals(info.bulkValidationInfo.validationResult));
    }

    @PUT
    @Transactional
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response editChannelData(@PathParam("name") String name, @PathParam("channelid") long channelId, @BeanParam JsonQueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        List<BaseReading> editedReadings = new ArrayList<>();
        List<BaseReading> editedBulkReadings = new ArrayList<>();
        List<BaseReading> confirmedReadings = new ArrayList<>();
        List<Instant> removeCandidates = new ArrayList<>();
        channelDataInfos.forEach((channelDataInfo) -> {
            validateLinkedToSlave(channel, Range.closedOpen(Instant.ofEpochMilli(channelDataInfo.interval.start), Instant.ofEpochMilli(channelDataInfo.interval.end)));
            if (!(isToBeConfirmed(channelDataInfo)) && channelDataInfo.value == null && channelDataInfo.collectedValue == null) {
                removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
            } else {
                if (channelDataInfo.value != null) {
                    editedReadings.add(channelDataInfo.createNew());
                }
                if (channelDataInfo.collectedValue != null) {
                    editedBulkReadings.add(channelDataInfo.createNewBulk());
                }
                if (isToBeConfirmed(channelDataInfo)) {
                    confirmedReadings.add(channelDataInfo.createConfirm());
                }
            }
        });
        channel.startEditingData()
                .removeChannelData(removeCandidates)
                .editChannelData(editedReadings)
                .editBulkChannelData(editedBulkReadings)
                .confirmChannelData(confirmedReadings)
                .complete();

        return Response.status(Response.Status.OK).build();
    }

    private void validateLinkedToSlave(Channel channel, Range readingTimeStamp) {
        List<DataLoggerChannelUsage> dataLoggerChannelUsagesForChannels = topologyService.findDataLoggerChannelUsagesForChannels(channel, readingTimeStamp);
        if (!dataLoggerChannelUsagesForChannels.isEmpty()) {
            throw this.exceptionFactory.newException(MessageSeeds.CANNOT_ADDEDITREMOVE_CHANNEL_VALUE_WHEN_LINKED_TO_SLAVE);
        }
    }

    private boolean isToBeConfirmed(ChannelDataInfo channelDataInfo) {
        return (channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.isConfirmed ||
                channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.isConfirmed);
    }

    @POST
    @Transactional
    @Path("/{channelid}/data/estimate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public List<ChannelDataInfo> previewEstimateChannelData(@PathParam("name") String name, @PathParam("channelid") long channelId,
                                                            @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                                            EstimateChannelDataInfo estimateChannelDataInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        return previewEstimate(QualityCodeSystem.MDC, device, channel, estimateChannelDataInfo);
    }

    private List<ChannelDataInfo> previewEstimate(QualityCodeSystem system, Device device, Channel channel, EstimateChannelDataInfo estimateChannelDataInfo) {
        Estimator estimator = estimationHelper.getEstimator(estimateChannelDataInfo);
        ReadingType readingType = channel.getReadingType();
        List<Range<Instant>> ranges = estimateChannelDataInfo.intervals.stream()
                .map(info -> Range.openClosed(Instant.ofEpochMilli(info.start), Instant.ofEpochMilli(info.end)))
                .collect(Collectors.toList());
        ImmutableSet<Range<Instant>> blocks = ranges.stream()
                .collect(ImmutableRangeSet::<Instant>builder, ImmutableRangeSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()))
                .build()
                .asRanges();

        Instant calculatedReadingTypeTimeStampForEstimationPreview = getCalculatedReadingTypeTimeStampForEstimationPreview(estimateChannelDataInfo);
        if (!estimateChannelDataInfo.estimateBulk && channel.getReadingType().isCumulative() && channel.getCalculatedReadingType(calculatedReadingTypeTimeStampForEstimationPreview).isPresent()) {
            readingType = channel.getCalculatedReadingType(calculatedReadingTypeTimeStampForEstimationPreview).get();
        }

        List<EstimationResult> results = new ArrayList<>();
        for (Range<Instant> block : blocks) {
            results.add(estimationHelper.previewEstimate(system, device, readingType, block, estimator));
        }
        return estimationHelper.getChannelDataInfoFromEstimationReports(channel, ranges, results);
    }

    private Instant getCalculatedReadingTypeTimeStampForEstimationPreview(EstimateChannelDataInfo estimateChannelDataInfo) {
        Optional<IntervalInfo> min = estimateChannelDataInfo.intervals.stream().min((o1, o2) -> (o1.start < o2.start ? -1 : 1));
        if (min.isPresent()) {
            return Instant.ofEpochMilli(min.get().start);
        } else {
            return clock.instant();
        }
    }

    @POST
    @Transactional
    @Path("/{channelid}/data/issue/estimate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response saveEstimatesChannelData(@PathParam("name") String name, @PathParam("channelid") long channelId, EstimateChannelDataInfo estimateChannelDataInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        return saveEstimates(device, channel, estimateChannelDataInfo);
    }

    private Response saveEstimates(Device device, Channel channel, EstimateChannelDataInfo estimateChannelDataInfo) {
        Estimator estimator = estimationHelper.getEstimator(estimateChannelDataInfo);
        ReadingType readingType = channel.getReadingType();
        List<BaseReading> editedBulkReadings = new ArrayList<>();
        List<Range<Instant>> ranges = estimateChannelDataInfo.intervals.stream()
                .map(info -> Range.openClosed(Instant.ofEpochMilli(info.start), Instant.ofEpochMilli(info.end)))
                .collect(Collectors.toList());
        ImmutableSet<Range<Instant>> blocks = ranges.stream()
                .collect(ImmutableRangeSet::<Instant>builder, ImmutableRangeSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()))
                .build()
                .asRanges();

        Instant calculatedReadingTypeTimeStampForEstimationPreview = getCalculatedReadingTypeTimeStampForEstimationPreview(estimateChannelDataInfo);
        if (!estimateChannelDataInfo.estimateBulk && channel.getReadingType().isCumulative() && channel.getCalculatedReadingType(calculatedReadingTypeTimeStampForEstimationPreview).isPresent()) {
            readingType = channel.getCalculatedReadingType(calculatedReadingTypeTimeStampForEstimationPreview).get();
        }

        for (Range<Instant> block : blocks) {
            EstimationResult estimationResult = estimationHelper.previewEstimate(QualityCodeSystem.MDC, device, readingType, block, estimator);
            if (!estimationResult.remainingToBeEstimated().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            ChannelDataInfo channelDataInfo = new ChannelDataInfo();
            for (EstimationBlock estimated : estimationResult.estimated()) {
                for (Estimatable estimatable : estimated.estimatables()) {
                    channelDataInfo.interval = new IntervalInfo();
                    channelDataInfo.interval.end = estimatable.getTimestamp().toEpochMilli();
                    channelDataInfo.collectedValue = estimatable.getEstimation();
                    editedBulkReadings.add(channelDataInfo.createNewBulk());
                }
            }
        }

        Optional<ReadingType> readingTypeToUpdate = Optional.ofNullable(estimateChannelDataInfo.readingType).flatMap(rt -> meteringService.getReadingType(rt.mRID));
        Optional<ReadingType> calculatedReadingType = channel.getCalculatedReadingType(calculatedReadingTypeTimeStampForEstimationPreview);

        if (readingTypeToUpdate
                .flatMap(rt -> calculatedReadingType.filter(crt -> crt.equals(rt))).isPresent()) {
            channel.startEditingData()
                    .editChannelData(editedBulkReadings)
                    .complete();
        } else if (calculatedReadingType.isPresent() && readingTypeToUpdate.filter(rt -> rt.equals(channel.getReadingType())).isPresent()) {
            channel.startEditingData()
                    .editBulkChannelData(editedBulkReadings)
                    .complete();
        } else if (!calculatedReadingType.isPresent() && readingTypeToUpdate.filter(rt -> rt.equals(channel.getReadingType())).isPresent()) {
            channel.startEditingData()
                    .editChannelData(editedBulkReadings)
                    .complete();
        } else {
            channel.startEditingData()
                    .editBulkChannelData(editedBulkReadings)
                    .complete();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Transactional
    @Path("{channelid}/validationstatus")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("name") String name, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        ValidationStatusInfo deviceValidationStatusInfo = channelHelper.get().determineStatus(channel);
        return Response.ok(deviceValidationStatusInfo).build();
    }

    @GET
    @Transactional
    @Path("{channelid}/validationpreview")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationStatusPreview(@PathParam("name") String name, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        return channelHelper.get().getChannelValidationInfo(() -> channel);
    }

    @PUT
    @Transactional
    @Path("{channelid}/validate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response validateDeviceData(LoadProfileTriggerValidationInfo info, @PathParam("name") String name, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        info.id = channel.getLoadProfile().getId();
        LoadProfile loadProfile = resourceHelper.lockLoadProfileOrThrowException(info);
        DeviceValidation deviceValidation = loadProfile.getDevice().forValidation();
        if (info.lastChecked != null) {
            deviceValidation.setLastChecked(channel, Instant.ofEpochMilli(info.lastChecked));
        }
        loadProfile.getDevice().getLoadProfileUpdaterFor(loadProfile).update();
        deviceValidation.validateChannel(channel);
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("{channelid}/datavalidationissues/{issueid}/validationblocks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getValidationBlocksOfIssue(@PathParam("name") String name, @PathParam("channelid") long channelId, @PathParam("issueid") long issueId, @BeanParam JsonQueryParameters parameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        IssueDataValidation issue = issueDataValidationService.findIssue(issueId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        Map<ReadingType, List<NotEstimatedBlock>> groupedBlocks = issue.getNotEstimatedBlocks().stream().collect(Collectors.groupingBy(NotEstimatedBlock::getReadingType));
        ReadingType mainReadingType = channel.getReadingType();
        List<NotEstimatedBlock> allNotEstimatedBlocks = new ArrayList<>();
        if (groupedBlocks.containsKey(mainReadingType)) {
            allNotEstimatedBlocks.addAll(groupedBlocks.get(mainReadingType));
        }
        Optional<ReadingType> calculatedReadingType = mainReadingType.getCalculatedReadingType();
        if (calculatedReadingType.isPresent() && groupedBlocks.containsKey(calculatedReadingType.get())) {
            allNotEstimatedBlocks.addAll(groupedBlocks.get(calculatedReadingType.get()));
        }
        List<Range<Instant>> result = new ArrayList<>();
        allNotEstimatedBlocks.stream()
                .map(block -> Range.closedOpen(block.getStartTime(), block.getEndTime()))
                .sorted((range1, range2) -> range1.lowerEndpoint().compareTo(range2.lowerEndpoint()))
                .reduce((range1, range2) -> {
                    if (range1.isConnected(range2)) {
                        return range1.span(range2);
                    }
                    result.add(range1);
                    return range2;
                }).ifPresent(result::add);
        List<Map<?, ?>> validationBlocksInfo = result.stream().map(block -> {
            Map<String, Object> info = new HashMap<>();
            info.put("startTime", block.lowerEndpoint());
            info.put("endTime", block.upperEndpoint());
            return info;
        }).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validationBlocks", validationBlocksInfo, parameters);
    }

    @GET
    @Transactional
    @Path("/{channelid}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public ChannelHistoryInfos getDataLoggerSlaveChannelHistory(@PathParam("name") String name, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(name, channelId);
        return ChannelHistoryInfos.from(topologyService.findDataLoggerChannelUsagesForChannels(channel, Range.atMost(clock.instant())));
    }
}
