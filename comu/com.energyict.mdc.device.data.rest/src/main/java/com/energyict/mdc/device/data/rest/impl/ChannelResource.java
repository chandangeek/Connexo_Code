package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * Created by bvn on 9/5/14.
 */
public class ChannelResource {
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public ChannelResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus, Clock clock, Provider<ChannelResourceHelper> channelHelper) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.channelHelper = channelHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE,Privileges.VIEW_DEVICE})
    public Response getChannels(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @BeanParam QueryParameters queryParameters) {
        return channelHelper.get().getChannels(mrid, (d -> resourceHelper.findLoadProfileOrThrowException(d, loadProfileId).getChannels()), queryParameters);
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE,Privileges.VIEW_DEVICE})
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        return channelHelper.get().getChannel(() -> doGetChannel(mrid, loadProfileId, channelId));
    }

    @GET
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE,Privileges.VIEW_DEVICE})
    public Response getChannelData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive(channel, clock.instant());
        if (intervalStart != null && intervalEnd != null) {
            List<LoadProfileReading> channelData = channel.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<ChannelDataInfo> infos = ChannelDataInfo.from(channelData, isValidationActive, thesaurus, deviceValidation);
            infos = filter(infos, uriInfo.getQueryParameters());
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response editChannelData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId, @BeanParam QueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = resourceHelper.getMeterFor(device);
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        com.elster.jupiter.metering.Channel koreChannel =
                resourceHelper
                    .getLoadProfileChannel(channel, meter)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_LOAD_PROFILE, loadProfileId, channelId));
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

    private List<ChannelDataInfo> filter(List<ChannelDataInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<ChannelDataInfo> fromParams = getFilter(queryParameters);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private boolean hasSuspects(ChannelDataInfo info) {
        return ValidationStatus.SUSPECT.equals(info.validationResult);
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
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

    private Channel doGetChannel(String mrid, long loadProfileId, long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        return resourceHelper.findChannelOrThrowException(loadProfile, channelId);
    }

    @Path("{channelid}/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,Privileges.FINE_TUNE_VALIDATION_CONFIGURATION})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        ValidationStatusInfo deviceValidationStatusInfo = channelHelper.get().determineStatus(channel);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    @Path("{channelid}/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VALIDATE_MANUAL)
    public Response validateDeviceData(TriggerValidationInfo validationInfo, @PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {

        Instant start = validationInfo.lastChecked == null ? null : Instant.ofEpochMilli(validationInfo.lastChecked);
        validateChannel(doGetChannel(mrid, loadProfileId, channelId), start);

        return Response.status(Response.Status.OK).build();
    }

    private void validateChannel(Channel channel, Instant start) {
    	DeviceValidation deviceValidation = channel.getDevice().forValidation();
    	if (start != null) {
    		deviceValidation.setLastChecked(channel, start);
    	}
    	deviceValidation.validateChannel(channel);
    }


}
