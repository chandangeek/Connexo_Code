package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.util.ArrayList;
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
        List<LoadProfileInfo> loadProfileInfos = new ArrayList<>(loadProfilesOnPage.size());
        for (LoadProfile loadProfile : loadProfilesOnPage) {
            loadProfileInfos.add(LoadProfileInfo.from(loadProfile));
        }

        return Response.ok(PagedInfoList.asJson("loadProfiles", loadProfileInfos, queryParameters)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}")
    public Response getLoadProfile(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        for (LoadProfile loadProfile : device.getLoadProfiles()) {
            if (loadProfile.getId()==loadProfileId) {
                if (intervalStart!=null && intervalEnd!=null) {
                    List<LoadProfileReading> loadProfileData = device.getChannelDataFor(loadProfile, new Interval(new Date(intervalStart), new Date(intervalEnd)));
                    return Response.ok(LoadProfileInfo.from(loadProfile, loadProfileData, thesaurus)).build();
                } else {
                    return Response.ok(LoadProfileInfo.from(loadProfile)).build();
                }
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, mrid, loadProfileId);
    }

}
