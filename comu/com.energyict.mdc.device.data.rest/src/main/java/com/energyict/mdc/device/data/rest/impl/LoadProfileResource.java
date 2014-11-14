package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.isNull;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Created by bvn on 7/28/14.
 */
public class LoadProfileResource {

    private static final Comparator<LoadProfile> LOAD_PROFILE_COMPARATOR_BY_NAME = new LoadProfileComparator();

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final Provider<ChannelResource> channelResourceProvider;
    private final Clock clock;

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus, Provider<ChannelResource> channelResourceProvider, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.channelResourceProvider = channelResourceProvider;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getAllLoadProfiles(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LoadProfile> allLoadProfiles = device.getLoadProfiles();
        List<LoadProfile> loadProfilesOnPage = ListPager.of(allLoadProfiles, LOAD_PROFILE_COMPARATOR_BY_NAME).from(queryParameters).find();
        List<LoadProfileInfo> loadProfileInfos = LoadProfileInfo.from(loadProfilesOnPage);
        return Response.ok(PagedInfoList.asJson("loadProfiles", loadProfileInfos, queryParameters)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}")
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getLoadProfile(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        LoadProfile loadProfile = doGetLoadProfile(mrid, loadProfileId);
        LoadProfileInfo loadProfileInfo = LoadProfileInfo.from(loadProfile);

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

        loadProfileInfo.validationInfo = new DetailedValidationInfo(isValidationActive(loadProfile), states, lastChecked(loadProfile));
        if (states.isEmpty()) {
            loadProfileInfo.validationInfo.dataValidated = loadProfile.getChannels().stream()
                    .allMatch(c -> c.getDevice().forValidation().allDataValidated(c, clock.instant()));
        }
    }

    private boolean isValidationActive(LoadProfile loadProfile) {
        return loadProfile.getChannels().stream()
                .anyMatch(isValidationActive());
    }

    private Date lastChecked(LoadProfile loadProfile) {
        List<Channel> channels = loadProfile.getChannels().stream()
                .filter(isValidationActive()).collect(Collectors.toList());
        List<Date> collect = channels.stream()
                .map(c -> c.getDevice().forValidation().getLastChecked(c))
                .map(o -> o.map(Date::from).orElse(null))
                .collect(Collectors.toList());
        return collect.stream().anyMatch(isNull()) ? null : collect.stream().reduce(this::min).orElse(null);
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private Date min(Date d1, Date d2) {
        Comparator<Date> comparator = nullsFirst(naturalOrder());
        return comparator.compare(d1, d2) <= 0 ? d1 : d2;
    }

    private Predicate<Channel> isValidationActive() {
        return c -> c.getDevice().forValidation().isValidationActive(c, clock.instant());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}/data")
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getLoadProfileData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        if (intervalStart != null && intervalEnd != null) {
            List<LoadProfileReading> loadProfileData = loadProfile.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileDataInfo> infos = LoadProfileDataInfo.from(device, loadProfileData, thesaurus, clock, loadProfile.getChannels());
            infos = filter(infos, uriInfo.getQueryParameters());
            List<LoadProfileDataInfo> paginatedLoadProfileData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", paginatedLoadProfileData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Path("{lpid}/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VALIDATE_MANUAL)
    public Response validateDeviceData(TriggerValidationInfo validationInfo, @PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {

        Instant start = validationInfo.lastChecked == null ? null : Instant.ofEpochMilli(validationInfo.lastChecked);
        validateLoadProfile(doGetLoadProfile(mrid, loadProfileId), start);

        return Response.status(Response.Status.OK).build();
    }

    private void validateLoadProfile(LoadProfile loadProfile, Instant start) {
    	if (start != null) {
    		loadProfile.getChannels().forEach(c -> loadProfile.getDevice().forValidation().setLastChecked(c, start));
    	}
        loadProfile.getDevice().forValidation().validateLoadProfile(loadProfile);        
    }

    private boolean hasSuspects(LoadProfileDataInfo info) {
        return info.channelValidationData.values().stream().anyMatch(v -> ValidationStatus.SUSPECT.equals(v.validationResult));
    }

    private boolean hasMissingData(LoadProfileDataInfo info) {
        return info.channelData.values().stream().anyMatch(isNull());
    }

    private List<LoadProfileDataInfo> filter(List<LoadProfileDataInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<LoadProfileDataInfo> toKeep = getFilter(queryParameters);
        infos.removeIf(not(toKeep));
        return infos;
    }

    private Predicate<LoadProfileDataInfo> getFilter(MultivaluedMap<String, String> queryParameters) {
        ImmutableList.Builder<Predicate<LoadProfileDataInfo>> list = ImmutableList.builder();
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
            list.add(not(this::hasMissingData));
        }
        return lpi -> list.build().stream().allMatch(p -> p.test(lpi));
    }

    private boolean filterActive(MultivaluedMap<String, String> queryParameters, String key) {
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

    @Path("{lpid}/channels")
    public ChannelResource getChannelResource() {
        return channelResourceProvider.get();
    }

    @Path("{lpid}/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId) {
        LoadProfile loadProfile = doGetLoadProfile(mrid, loadProfileId);
        ValidationStatusInfo deviceValidationStatusInfo = determineStatus(loadProfile);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(LoadProfile loadProfile) {
        return new ValidationStatusInfo(isValidationActive(loadProfile), lastChecked(loadProfile), hasData(loadProfile));
    }

    private boolean hasData(LoadProfile loadProfile) {
        return loadProfile.getChannels().stream()
                .anyMatch(hasData());
    }

    private Predicate<Channel> hasData() {
        return c -> c.getDevice().forValidation().hasData(c);
    }

}
