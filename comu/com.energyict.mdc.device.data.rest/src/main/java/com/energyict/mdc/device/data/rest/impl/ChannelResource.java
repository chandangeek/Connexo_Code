package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
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

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn on 9/5/14.
 */
public class ChannelResource {
    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final ValidationEvaluator evaluator;
    private final ValidationService validationService;
    private final Clock clock;

    @Inject
    public ChannelResource(ResourceHelper resourceHelper, Thesaurus thesaurus, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.clock = clock;
        this.evaluator = validationService.getEvaluator();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannels(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        List<Channel> channelsPage = ListPager.of(loadProfile.getChannels(), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("channels", ChannelInfo.from(channelsPage), queryParameters)).build();
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo).build();
    }

    private void addValidationInfo(Channel channel, ChannelInfo channelInfo) {
        List<DataValidationStatus> states =
                channel.getDevice().forValidation().getValidationStatus(channel, lastMonth());
        channelInfo.validationInfo = new DetailedValidationInfo(isValidationActive(channel), states, lastChecked(channel));
    }

    private boolean isValidationActive(Channel channel) {
        return channel.getDevice().forValidation().isValidationActive(channel, clock.now());
    }

    private Date lastChecked(Channel channel) {
        Optional<Date> optional =  channel.getDevice().forValidation().getLastChecked(channel);
        return (optional.isPresent()) ? optional.get() : null;
    }

    private Interval lastMonth() {
        ZonedDateTime end = clock.now().toInstant().atZone(ZoneId.of("UTC")).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return new Interval(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

    @GET
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannelData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters) {
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        boolean isValidationActive = channel.getDevice().forValidation().isValidationActive(channel, clock.now());
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = channel.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<ChannelDataInfo> infos = ChannelDataInfo.from(paginatedLoadProfileData, isValidationActive, thesaurus, evaluator);
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private Channel doGetChannel(String mrid, long loadProfileId, long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId);
        return resourceHelper.findChannelOrThrowException(loadProfile, channelId);
    }

    @Path("{channelid}/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        Channel channel = doGetChannel(mrid, loadProfileId, channelId);
        ValidationStatusInfo deviceValidationStatusInfo = determineStatus(channel);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(Channel channel) {
        return new ValidationStatusInfo(isValidationActive(channel), lastChecked(channel), hasData(channel));
    }

    private boolean hasData(Channel channel) {
        return channel.getDevice().forValidation().hasData(channel);
    }

}
