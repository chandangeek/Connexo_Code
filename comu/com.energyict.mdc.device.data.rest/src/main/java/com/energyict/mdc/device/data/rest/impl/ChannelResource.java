package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class ChannelResource {
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public ChannelResource(Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, Thesaurus thesaurus, Clock clock) {
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannels(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mrid, (d -> this.getFilteredChannels(d, filter)), queryParameters);
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mrid, channelId);
        return channelHelper.get().getChannel(() -> channel);
    }

    private List<Channel> getFilteredChannels(Device device, JsonQueryFilter filter){
        Predicate<String> filterByLoadProfileName = getFilterIfAvailable("loadProfileName", filter);
        Predicate<String> filterByChannelName = getFilterIfAvailable("channelName", filter);
        return device.getLoadProfiles().stream()
                .filter(l -> filterByLoadProfileName.test(l.getLoadProfileSpec().getLoadProfileType().getName()))
                .flatMap(l -> l.getChannels().stream())
                .filter(c -> filterByChannelName.test(c.getName()))
                .sorted(Comparator.comparing(Channel::getName))
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getChannelData(@PathParam("mRID") String mrid, @PathParam("channelid") long channelId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mrid, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive(channel, clock.instant());
        if (intervalStart != null && intervalEnd != null) {
            List<LoadProfileReading> channelData = channel.getChannelData(Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd)));
            List<ChannelDataInfo> infos = ChannelDataInfo.from(channelData, isValidationActive, thesaurus, deviceValidation);
            infos = filter(infos, uriInfo.getQueryParameters());
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", paginatedChannelData, queryParameters);
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
        return ValidationStatus.SUSPECT.equals(info.validationResult);
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
    }



    @PUT
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response editChannelData(@PathParam("mRID") String mrid, @PathParam("channelid") long channelId, @BeanParam QueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        com.elster.jupiter.metering.Channel koreChannel = resourceHelper.findLoadProfileChannelOrThrowException(channel, resourceHelper.getMeterFor(device));
        List<BaseReading> editedReadings = new LinkedList<>();
        List<BaseReadingRecord> removedReadings = new LinkedList<>();
        channelDataInfos.forEach((channelDataInfo) -> {
            if (channelDataInfo.value == null) {
                List<BaseReadingRecord> readings =
                        koreChannel.getReadings(
                                Range.openClosed(
                                        Instant.ofEpochMilli(channelDataInfo.interval.start),
                                        Instant.ofEpochMilli(channelDataInfo.interval.end)));
                removedReadings.addAll(readings);
            } else {
                editedReadings.add(channelDataInfo.createNew());
            }
        });
        koreChannel.editReadings(editedReadings);
        koreChannel.removeReadings(removedReadings);

        return Response.status(Response.Status.OK).build();
    }


    @GET
    @Path("{channelid}/validationstatus")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("channelid") long channelId) {
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(mrid, channelId);
        ValidationStatusInfo deviceValidationStatusInfo = channelHelper.get().determineStatus(channel);
        return Response.ok(deviceValidationStatusInfo).build();
    }

    @PUT
    @Path("{channelid}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VALIDATE_MANUAL)
    public Response validateDeviceData(TriggerValidationInfo validationInfo, @PathParam("mRID") String mrid, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        DeviceValidation deviceValidation = device.forValidation();
        if (validationInfo.lastChecked != null) {
            deviceValidation.setLastChecked(channel, Instant.ofEpochMilli(validationInfo.lastChecked));
        }
        deviceValidation.validateChannel(channel);
        return Response.ok().build();
    }
}
