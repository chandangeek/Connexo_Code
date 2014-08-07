package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
 * Created by bvn on 7/28/14.
 */
public class LoadProfileResource {

    public static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new Comparator<Channel>() {
        @Override
        public int compare(Channel o1, Channel o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLoadProfiles(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LoadProfile> allLoadProfiles = device.getLoadProfiles();
        List<LoadProfile> loadProfilesOnPage = ListPager.of(allLoadProfiles).from(queryParameters).find();
        List<LoadProfileInfo> loadProfileInfos = LoadProfileInfo.from(loadProfilesOnPage);
        return Response.ok(PagedInfoList.asJson("loadProfiles", loadProfileInfos, queryParameters)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}")
    public Response getLoadProfile(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = findLoadProfileOrThrowException(device, loadProfileId, mrid);
        return Response.ok(LoadProfileInfo.from(loadProfile)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}/data")
    public Response getLoadProfileData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = findLoadProfileOrThrowException(device, loadProfileId, mrid);
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = device.getChannelDataFor(loadProfile, new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<LoadProfileDataInfo> infos = LoadProfileDataInfo.from(paginatedLoadProfileData, thesaurus);
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("{lpid}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannels(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = findLoadProfileOrThrowException(device, loadProfileId, mrid);
        List<Channel> channelsPage = ListPager.of(loadProfile.getChannels(), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("channels", ChannelInfo.from(channelsPage), queryParameters)).build();
    }

    @GET
    @Path("{lpid}/channels/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = findLoadProfileOrThrowException(device, loadProfileId, mrid);
        for (Channel channel : loadProfile.getChannels()) {
            if (channel.getChannelSpec().getId()==channelId) {
                return Response.ok(ChannelInfo.from(channel)).build();
            }
        }


        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId, String mrid) {
        for (LoadProfile loadProfile : device.getLoadProfiles()) {
            if (loadProfile.getId()==loadProfileId) {
                return loadProfile;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, mrid, loadProfileId);
    }



}
