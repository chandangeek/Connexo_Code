package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.comparators.NullSafeOrdering;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.isNull;

/**
 * Created by bvn on 7/28/14.
 */
public class LoadProfileResource {

    private static final Comparator<LoadProfile> LOAD_PROFILE_COMPARATOR_BY_NAME = new LoadProfileComparator();

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Provider<ChannelResource> channelResourceProvider;
    private final Clock clock;
    private final ValidationEvaluator evaluator;
    private final ValidationService validationService;

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, Thesaurus thesaurus, Provider<ChannelResource> channelResourceProvider, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.channelResourceProvider = channelResourceProvider;
        this.clock = clock;
        this.validationService = validationService;
        this.evaluator = validationService.getEvaluator();
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
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        LoadProfileInfo loadProfileInfo = LoadProfileInfo.from(loadProfile);

        addValidationInfo(loadProfile, loadProfileInfo);

        return Response.ok(loadProfileInfo).build();
    }

    private void addValidationInfo(LoadProfile loadProfile, LoadProfileInfo loadProfileInfo) {
        List<DataValidationStatus> states = loadProfile.getChannels().stream()
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, lastMonth()).stream())
                .collect(Collectors.toList());

        loadProfileInfo.validationInfo = new DetailedValidationInfo(isValidationActive(loadProfile), states, lastChecked(loadProfile));
    }

    private boolean isValidationActive(LoadProfile loadProfile) {
        return loadProfile.getChannels().stream()
                    .anyMatch(isValidationActive());
    }

    private Date lastChecked(LoadProfile loadProfile) {
        return (Date) loadProfile.getChannels().stream()
                    .filter(isValidationActive())
                    .map(c -> c.getDevice().forValidation().getLastChecked(c))
                    .map(Optional::orNull)
                    .reduce(this::min)
                    .orElse(null);
    }

    private Interval lastMonth() {
        ZonedDateTime end = clock.now().toInstant().atZone(ZoneId.of("UTC")).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return new Interval(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

    private Date min(Date d1, Date d2) {
        return NullSafeOrdering.NULL_IS_SMALLEST.<Date>get().compare(d1 , d2) <= 0 ? d1 : d2;
    }

    private Predicate<Channel> isValidationActive() {
        return c -> c.getDevice().forValidation().isValidationActive(c, clock.now());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}/data")
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getLoadProfileData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = loadProfile.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<LoadProfileDataInfo> infos = LoadProfileDataInfo.from(paginatedLoadProfileData, thesaurus, clock, evaluator);
            infos = filter(infos, uriInfo.getQueryParameters());
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private boolean hasSuspects(LoadProfileDataInfo info) {
        return info.channelValidationData.values().stream().anyMatch(v -> ValidationStatus.SUSPECT.equals(v.validationResult));
    }

    private boolean hasMissingData(LoadProfileDataInfo info) {
        return info.channelData.values().stream().anyMatch(isNull());
    }

    private List<LoadProfileDataInfo> filter(List<LoadProfileDataInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<LoadProfileDataInfo> fromParams = getFilter(queryParameters);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<LoadProfileDataInfo> getFilter(MultivaluedMap<String, String> queryParameters) {
        ImmutableList.Builder<Predicate<LoadProfileDataInfo>> list = ImmutableList.builder();
        if (filterActive(queryParameters, "onlySuspect")) {
            list.add(this::hasSuspects);
        }
        if (filterActive(queryParameters, "hideMissing")) {
            list.add(this::hasMissingData);
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

}
