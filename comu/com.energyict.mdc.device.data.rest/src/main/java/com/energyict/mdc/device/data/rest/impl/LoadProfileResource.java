package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
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

    private static final Comparator<LoadProfile> LOAD_PROFILE_COMPARATOR_BY_NAME = new LoadProfileComparator();

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Provider<ChannelResource> channelResourceProvider;
    private final ValidationEvaluator evaluator;

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, Thesaurus thesaurus, Provider<ChannelResource> channelResourceProvider, ValidationService validationService) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.channelResourceProvider = channelResourceProvider;
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
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        return Response.ok(LoadProfileInfo.from(loadProfile)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}/data")
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getLoadProfileData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = loadProfile.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<LoadProfileDataInfo> infos = LoadProfileDataInfo.from(paginatedLoadProfileData, thesaurus, evaluator);
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Path("{lpid}/channels")
    public ChannelResource getChannelResource() {
        return channelResourceProvider.get();
    }




}
