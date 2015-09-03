package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@DeviceStatesRestricted(
        value = {DefaultState.DECOMMISSIONED},
        methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE},
        ignoredUserRoles = {Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
public class ChannelResource {
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final ValidationInfoFactory validationInfoFactory;
    private final IssueDataValidationService issueDataValidationService;
    private final EstimationHelper estimationHelper;

    @Inject
    public ChannelResource(Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, Thesaurus thesaurus, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, ValidationInfoFactory validationInfoFactory, IssueDataValidationService issueDataValidationService, EstimationHelper estimationHelper) {
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.validationInfoFactory = validationInfoFactory;
        this.issueDataValidationService = issueDataValidationService;
        this.estimationHelper = estimationHelper;
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
        Predicate<String> filterByLoadProfileName = getStringListFilterIfAvailable("loadProfileName", filter);
        Predicate<String> filterByChannelName = getStringFilterIfAvailable("channelName", filter);
        return device.getLoadProfiles().stream()
                .filter(l -> filterByLoadProfileName.test(l.getLoadProfileSpec().getLoadProfileType().getName()))
                .flatMap(l -> l.getChannels().stream())
                .filter(c -> filterByChannelName.test(channelHelper.get().getChannelName(c)))
                .collect(Collectors.toList());
    }

    private Predicate<String> getStringFilterIfAvailable(String name, JsonQueryFilter filter){
        if (filter.hasProperty(name)){
            Pattern pattern = getFilterPattern(filter.getString(name));
            if (pattern != null){
                return s -> pattern.matcher(s).matches();
            }
        }
        return s -> true;
    }

    private Predicate<String> getStringListFilterIfAvailable(String name, JsonQueryFilter filter){
        if (filter.hasProperty(name)){
            List<String> entries = filter.getStringList(name);
            List<Pattern> patterns = new ArrayList<>();
            for (String entry : entries) {
                patterns.add(getFilterPattern(entry));
            }
            if (!patterns.isEmpty()){
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
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response getChannelData(
            @PathParam("mRID") String mRID,
            @PathParam("channelid") long channelId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive();
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            List<LoadProfileReading> channelData = channel.getChannelData(range);
            List<ChannelDataInfo> infos = channelData.stream().map(loadProfileReading -> deviceDataInfoFactory.createChannelDataInfo(channel, loadProfileReading, isValidationActive, deviceValidation)).collect(Collectors.toList());
            infos = filter(infos, filter);
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{channelid}/data/{epochMillis}/validation")
    public Response getValidationData(
            @PathParam("mRID") String mRID,
            @PathParam("channelid") long channelId,
            @PathParam("epochMillis") long epochMillis) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        Instant to = Instant.ofEpochMilli(epochMillis);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(to, channel.getDevice().getZone());
        Instant from = zonedDateTime.minus(channel.getInterval().asTemporalAmount()).toInstant();
        Optional<LoadProfileReading> loadProfileReading = channel.getChannelData(Range.openClosed(from, to)).stream().findAny();
        Optional<DataValidationStatus> dataValidationStatus = loadProfileReading.flatMap(lpReading -> lpReading.getChannelValidationStates().entrySet().stream().map(Map.Entry::getValue).findFirst());

        Optional<VeeReadingInfo> veeReadingInfo = dataValidationStatus.map(status -> {
            IntervalReadingRecord channelReading = loadProfileReading.flatMap(lpReading -> lpReading.getChannelValues().entrySet().stream().map(Map.Entry::getValue).findFirst()).orElse(null);// There can be only one channel (or no channel at all if the channel has no dta for this interval)
            return validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, status, deviceValidation, channelReading);
        });
        List<VeeReadingInfo> list = new ArrayList<>();
        list.add(veeReadingInfo.orElseGet(VeeReadingInfo::new));
        return Response.ok(veeReadingInfo.orElseGet(VeeReadingInfo::new)).build();
    }

    private List<ChannelDataInfo> filter(List<ChannelDataInfo> infos, JsonQueryFilter filter) {
        Predicate<ChannelDataInfo> fromParams = getFilter(filter);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ChannelDataInfo> getFilter(JsonQueryFilter filter) {
        ImmutableList.Builder<Predicate<ChannelDataInfo>> list = ImmutableList.builder();
        if (filter.hasProperty("suspect")){
            List<String> suspectFilters = filter.getStringList("suspect");
            if (suspectFilters.size() == 0) {
                if ("suspect".equals(filter.getString("suspect"))) {
                    list.add(this::hasSuspects);
                } else {
                    list.add(not(this::hasSuspects));
                }
            }
        }
        if (filterActive(filter, "hideMissing")) {
            list.add(this::hasMissingData);
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }

    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }


    private boolean hasSuspects(ChannelDataInfo info) {
        return ValidationStatus.SUSPECT.equals(info.mainValidationInfo.validationResult) ||
                ValidationStatus.SUSPECT.equals(info.bulkValidationInfo.validationResult);
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
    }

    @PUT
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response editChannelData(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId, @BeanParam JsonQueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        List<BaseReading> editedReadings = new ArrayList<>();
        List<BaseReading> editedBulkReadings = new ArrayList<>();
        List<BaseReading> confirmedReadings = new ArrayList<>();
        List<Instant> removeCandidates = new ArrayList<>();
        channelDataInfos.forEach((channelDataInfo) -> {
            if (!(isToBeConfirmed(channelDataInfo)) && channelDataInfo.value == null && channelDataInfo.collectedValue == null) {
                removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
            }
            else {
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

    private boolean isToBeConfirmed(ChannelDataInfo channelDataInfo) {
        return ((channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.isConfirmed) ||
                (channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.isConfirmed));
    }

    @POST
    @Path("/{channelid}/data/estimate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_DATA})
    public List<ChannelDataInfo> previewEstimateChannelData(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId, EstimateChannelDataInfo estimateChannelDataInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        return previewEstimate(device, channel, estimateChannelDataInfo);
    }

    private List<ChannelDataInfo> previewEstimate(Device device, Channel channel, EstimateChannelDataInfo estimateChannelDataInfo) {
        Estimator estimator = estimationHelper.getEstimator(estimateChannelDataInfo);
        ReadingType readingType = channel.getReadingType();
        List<EstimationResult> results = new ArrayList<>();
        List<Range<Instant>> ranges = estimateChannelDataInfo.intervals.stream()
                .map(info -> Range.openClosed(Instant.ofEpochMilli(info.start), Instant.ofEpochMilli(info.end)))
                .collect(Collectors.toList());
        ImmutableSet<Range<Instant>> blocks = ranges.stream()
                .collect(ImmutableRangeSet::<Instant>builder, ImmutableRangeSet.Builder::add, (b1, b2) -> b1.addAll(b2.build()))
                .build()
                .asRanges();

        if (!estimateChannelDataInfo.estimateBulk && channel.getReadingType().isCumulative() && channel.getReadingType().getCalculatedReadingType().isPresent()) {
            readingType = channel.getReadingType().getCalculatedReadingType().get();
        }

        for (Range<Instant> block : blocks) {
            results.add(estimationHelper.previewEstimate(device, readingType, block, estimator));
        }
        return estimationHelper.getChannelDataInfoFromEstimationReports(channel, ranges, results);
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

    @GET
    @Path("{channelid}/validationpreview")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationStatusPreview(@PathParam("mRID") String mRID, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mRID, channelId);
        return channelHelper.get().getChannelValidationInfo(() -> channel);
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
