package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class ChannelResource {
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final IssueDataValidationService issueDataValidationService;

    @Inject
    public ChannelResource(Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, Thesaurus thesaurus, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, IssueDataValidationService issueDataValidationService) {
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.issueDataValidationService = issueDataValidationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannels(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mRID, (d -> this.getFilteredChannels(d, filter)), queryParameters);
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannel(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        return channelHelper.get().getChannel(() -> channel);
    }

    private List<Channel> getFilteredChannels(Device device, JsonQueryFilter filter){
        Predicate<String> filterByLoadProfileName = getFilterIfAvailable("loadProfileName", filter);
        Predicate<String> filterByChannelName = getFilterIfAvailable("channelName", filter);
        return device.getLoadProfiles().stream()
                .filter(l -> filterByLoadProfileName.test(l.getLoadProfileSpec().getLoadProfileType().getName()))
                .flatMap(l -> l.getChannels().stream())
                .filter(c -> filterByChannelName.test(channelHelper.get().getChannelName(c)))
                .collect(Collectors.toList());
    }

    private Predicate<String> getFilterIfAvailable(String name, JsonQueryFilter filter){
        if (filter.hasProperty(name)){
            Pattern pattern = getFilterPattern(filter.getString(name));
            if (pattern != null){
                return s -> pattern.matcher(s).matches();
            }
        }
        return s -> true;
    }

    private Pattern getFilterPattern(String filter){
        if (filter != null){
            filter = Pattern.quote(filter.replace('%', '*'));
            return Pattern.compile(filter.replaceAll("([*?])", "\\\\E\\.$1\\\\Q"));
        }
        return null;
    }

    @GET
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getChannelData(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive(channel, clock.instant());
        if (intervalStart != null && intervalEnd != null) {
            List<LoadProfileReading> channelData = channel.getChannelData(Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd)));
            List<ChannelDataInfo> infos = channelData.stream().map(loadProfileReading -> deviceDataInfoFactory.createChannelDataInfo(loadProfileReading, isValidationActive, deviceValidation)).collect(Collectors.toList());
            infos = filter(infos, uriInfo.getQueryParameters());
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private List<ChannelDataInfo> filter(List<ChannelDataInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<ChannelDataInfo> fromParams = getFilter(queryParameters);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ChannelDataInfo> getFilter(MultivaluedMap<String, String> queryParameters) {
        ImmutableList.Builder<Predicate<ChannelDataInfo>> list = ImmutableList.builder();
        boolean onlySuspect = filterActive(queryParameters, "onlySuspect");
        boolean onlyNonSuspect = filterActive(queryParameters, "onlyNonSuspect");
        if (onlySuspect ^ onlyNonSuspect) {
            if (onlySuspect) {
                list.add(this::hasSuspects);
            } else {
                list.add(not(this::hasSuspects));
            }
        }
        if (filterActive(queryParameters, "hideMissing")) {
            list.add(this::hasMissingData);
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }

    private boolean filterActive(MultivaluedMap<String, String> queryParameters, String key) {
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }


    private boolean hasSuspects(ChannelDataInfo info) {
        return ValidationStatus.SUSPECT.equals(info.validationInfo.mainValidationInfo.validationResult) ||
                ValidationStatus.SUSPECT.equals(info.validationInfo.bulkValidationInfo.validationResult);
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
    }

    @PUT
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response editChannelData(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId, @BeanParam JsonQueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        List<BaseReading> editedReadings = new ArrayList<>();
        List<Range<Instant>> removeCandidates = new ArrayList<>();
        channelDataInfos.forEach((channelDataInfo) -> {
            if (channelDataInfo.value == null) {
                removeCandidates.add(
                        Range.openClosed(
                                Instant.ofEpochMilli(channelDataInfo.interval.start),
                                Instant.ofEpochMilli(channelDataInfo.interval.end)));
            }
            else {
                editedReadings.add(channelDataInfo.createNew());
            }
        });
        channel.startEditingData().removeChannelData(removeCandidates).editChannelData(editedReadings).complete();

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("{channelid}/validationstatus")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        ValidationStatusInfo deviceValidationStatusInfo = channelHelper.get().determineStatus(channel);
        return Response.ok(deviceValidationStatusInfo).build();
    }

    @PUT
    @Path("{channelid}/validate")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VALIDATE_MANUAL)
    public Response validateDeviceData(TriggerValidationInfo validationInfo, @PathParam("mRID") String mRID, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        DeviceValidation deviceValidation = device.forValidation();
        if (validationInfo.lastChecked != null) {
            deviceValidation.setLastChecked(channel, Instant.ofEpochMilli(validationInfo.lastChecked));
        }
        deviceValidation.validateChannel(channel);
        return Response.ok().build();
    }

    @GET
    @Path("{channelid}/datavalidationissues/{issueid}/validationblocks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getValidationBlocksOfIssue(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId, @PathParam("issueid") long issueId, @BeanParam JsonQueryParameters parameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
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
}
