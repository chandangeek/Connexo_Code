package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Created by bvn on 7/28/14.
 */
public class LoadProfileResource {

    private static final Comparator<LoadProfile> LOAD_PROFILE_COMPARATOR_BY_NAME = new LoadProfileComparator();

    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, ValidationInfoFactory validationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.validationInfoFactory = validationInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getAllLoadProfiles(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LoadProfile> allLoadProfiles = device.getLoadProfiles();
        List<LoadProfile> loadProfilesOnPage = ListPager.of(allLoadProfiles, LOAD_PROFILE_COMPARATOR_BY_NAME).from(queryParameters).find();
        List<LoadProfileInfo> loadProfileInfos = LoadProfileInfo.from(loadProfilesOnPage);
        return Response.ok(PagedInfoList.fromPagedList("loadProfiles", loadProfileInfos.stream().sorted((o1, o2) -> o1.name.compareToIgnoreCase(o2.name)).collect(Collectors.toList()), queryParameters)).build();
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("{lpid}")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getLoadProfile(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        LoadProfile loadProfile = doGetLoadProfile(mrid, loadProfileId);
        LoadProfileInfo loadProfileInfo = LoadProfileInfo.from(loadProfile, clock);

        addValidationInfo(loadProfile, loadProfileInfo);

        return Response.ok(loadProfileInfo).build();
    }

    private LoadProfile doGetLoadProfile(String mrid, long loadProfileId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        return resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
    }

    private void addValidationInfo(LoadProfile loadProfile, LoadProfileInfo loadProfileInfo) {
        List<DataValidationStatus> states = loadProfile.getChannels().stream()
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), lastMonth()).stream())
                .collect(Collectors.toList());

        loadProfileInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(loadProfile), states, lastChecked(loadProfile));
        Range<Instant> checkInterval = lastMonth();
        loadProfileInfo.validationInfo.dataValidated = loadProfile.getChannels().stream()
                .allMatch(c -> allDataValidatedOnChannel(c, checkInterval));
    }

    @Path("{lpid}")
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response updateLoadProfile(LoadProfileInfo info, @PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        LoadProfile loadProfile = doGetLoadProfile(mrid, loadProfileId);
        Optional<Instant> lastReading = loadProfile.getLastReading();
        if (!lastReading.isPresent() || lastReading.get().compareTo(info.lastReading) != 0) {
            loadProfile.getDevice().getLoadProfileUpdaterFor(loadProfile).setLastReading(info.lastReading).update();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * This method should return the same result as {@link ChannelResourceHelper#addValidationInfo(Channel, ChannelInfo)}
     */
    private boolean allDataValidatedOnChannel(Channel channel, Range<Instant> checkInterval) {
        List<DataValidationStatus> validationStatuses =
                channel.getDevice().forValidation().getValidationStatus(channel, Collections.emptyList(), checkInterval);
        if (!validationStatuses.isEmpty()) {
            return  validationStatuses.stream().allMatch(DataValidationStatus::completelyValidated);
        }
        return channel.getDevice().forValidation().allDataValidated(channel, clock.instant());
    }

    private boolean isValidationActive(LoadProfile loadProfile) {
        return loadProfile.getChannels().stream()
                .anyMatch(isValidationActive());
    }

    private Optional<Instant> lastChecked(LoadProfile loadProfile) {
        return loadProfile
                    .getChannels()
                    .stream()
                    .filter(isValidationActive())
                    .map(this::getLastChecked)
                    .flatMap(Functions.asStream())
                    .reduce(this::min);
    }

    private Optional<Instant> getLastChecked(Channel channel) {
        Optional<Instant> lastChecked = Optional.empty();
        if(channel.getDevice().forValidation().getLastChecked(channel).isPresent()){
            lastChecked = channel.getDevice().forValidation().getLastChecked(channel).equals(Optional.of(channel.getDevice().getMeterActivationsMostRecentFirst().get(0).getStart())) ? Optional.empty() : channel.getDevice().forValidation().getLastChecked(channel);
        }
        return lastChecked;
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private Instant min(Instant i1, Instant i2) {
        Comparator<Instant> comparator = nullsFirst(naturalOrder());
        return comparator.compare(i1, i2) <= 0 ? i1 : i2;
    }

    private Predicate<Channel> isValidationActive() {
        return c -> c.getDevice().forValidation().isValidationActive(c, clock.instant());
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("{lpid}/data")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getLoadProfileData(
            @PathParam("mRID") String mrid,
            @PathParam("lpid") long loadProfileId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        Boolean isValidationActive = isValidationActive(loadProfile);
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            List<LoadProfileReading> loadProfileData = loadProfile.getChannelData(Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd")));
            List<LoadProfileDataInfo> infos = loadProfileData.stream().map(loadProfileReading -> deviceDataInfoFactory.createLoadProfileDataInfo(loadProfileReading, device.forValidation(), loadProfile.getChannels(), isValidationActive)).collect(Collectors.toList());
            infos = filter(infos, filter);
            List<LoadProfileDataInfo> paginatedLoadProfileData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedLoadProfileData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Path("{lpid}/validate")
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response validateDeviceData(LoadProfileTriggerValidationInfo info, @PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        info.id = loadProfileId;
        LoadProfile loadProfile = resourceHelper.lockLoadProfileOrThrowException(info);
        Instant start = info.lastChecked == null ? null : Instant.ofEpochMilli(info.lastChecked);
        validateLoadProfile(loadProfile, start);
        loadProfile.getDevice().getLoadProfileUpdaterFor(loadProfile).update();

        return Response.status(Response.Status.OK).build();
    }

    private void validateLoadProfile(LoadProfile loadProfile, Instant start) {
    	if (start != null) {
    		loadProfile.getChannels().forEach(c -> loadProfile.getDevice().forValidation().setLastChecked(c, start));
    	}
        loadProfile.getDevice().forValidation().validateLoadProfile(loadProfile);
    }

    private boolean hasSuspects(LoadProfileDataInfo info) {
        return info.channelValidationData.values().stream()
                .anyMatch(v -> ValidationStatus.SUSPECT.equals(v.mainValidationInfo.validationResult) ||
                    (v.bulkValidationInfo != null && ValidationStatus.SUSPECT.equals(v.bulkValidationInfo.validationResult)));
    }

    private List<LoadProfileDataInfo> filter(List<LoadProfileDataInfo> infos, JsonQueryFilter filter) {
        Predicate<LoadProfileDataInfo> toKeep = resourceHelper.getSuspectsFilter(filter, this::hasSuspects);
        infos.removeIf(not(toKeep));
        return infos;
    }

    @Path("{lpid}/validationstatus")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        LoadProfile loadProfile = doGetLoadProfile(mrid, loadProfileId);
        ValidationStatusInfo deviceValidationStatusInfo = determineStatus(loadProfile);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(LoadProfile loadProfile) {
        return new ValidationStatusInfo(isValidationActive(loadProfile), lastChecked(loadProfile), hasData(loadProfile));
    }

    private boolean hasData(LoadProfile loadProfile) {
        return loadProfile.getChannels().stream().anyMatch(Channel::hasData);
    }
}