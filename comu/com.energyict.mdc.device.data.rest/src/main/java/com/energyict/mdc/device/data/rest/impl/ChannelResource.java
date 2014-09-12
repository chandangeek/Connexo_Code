package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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

/**
 * Created by bvn on 9/5/14.
 */
public class ChannelResource {
    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final ValidationEvaluator evaluator;
    private final ValidationService validationService;

    @Inject
    public ChannelResource(ResourceHelper resourceHelper, Thesaurus thesaurus, ValidationService validationService) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.evaluator = validationService.getEvaluator();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannels(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        List<Channel> channelsPage = ListPager.of(loadProfile.getChannels(), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("channels", ChannelInfo.from(channelsPage), queryParameters)).build();
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        Channel channel = resourceHelper.findChannelOrThrowException(loadProfile, channelId);
        return Response.ok(ChannelInfo.from(channel)).build();
    }

    @GET
    @Path("/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannelData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        Channel channel = resourceHelper.findChannelOrThrowException(loadProfile, channelId);
        boolean isValidationActive = channel.isValidationActive();
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = channel.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<ChannelDataInfo> infos = ChannelDataInfo.from(paginatedLoadProfileData, isValidationActive, thesaurus, evaluator);
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
