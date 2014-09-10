package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
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
import javax.ws.rs.core.*;

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

    @Inject
    public LoadProfileResource(ResourceHelper resourceHelper, Thesaurus thesaurus, Provider<ChannelResource> channelResourceProvider, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.channelResourceProvider = channelResourceProvider;
        this.clock = clock;
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
        LoadProfileInfo loadProfileInfo = LoadProfileInfo.from(loadProfile);

        addValidationInfo(loadProfile, loadProfileInfo);

        return Response.ok(loadProfileInfo).build();
    }

    private void addValidationInfo(LoadProfile loadProfile, LoadProfileInfo loadProfileInfo) {
        loadProfileInfo.validationActive = true;
        loadProfileInfo.validationInfo = new DetailedValidationInfo();
        ValidationRuleInfo validationRuleInfo = new ValidationRuleInfo();
        validationRuleInfo.displayName = "rule1";
        PropertyValueInfo<String> stringPropertyValueInfo = new PropertyValueInfo<>("Value", "default");
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        PropertyInfo propKey = new PropertyInfo("propKey", stringPropertyValueInfo, propertyTypeInfo, true);
        validationRuleInfo.properties = Arrays.asList(propKey);
        loadProfileInfo.validationInfo.validationRules = ImmutableMap.of(validationRuleInfo, 5);
        loadProfileInfo.validationInfo.dataValidated = true;
        loadProfileInfo.validationInfo.validationResult = ValidationStatus.SUSPECT;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lpid}/data")
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getLoadProfileData(@PathParam("mRID") String mrid, @PathParam("lpid") long loadProfileId, @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LoadProfile loadProfile = resourceHelper.findLoadProfileOrThrowException(device, loadProfileId, mrid);
        if (intervalStart!=null && intervalEnd!=null) {
            List<LoadProfileReading> loadProfileData = loadProfile.getChannelData(new Interval(new Date(intervalStart), new Date(intervalEnd)));
            List<LoadProfileReading> paginatedLoadProfileData = ListPager.of(loadProfileData).from(queryParameters).find();
            List<LoadProfileDataInfo> infos = LoadProfileDataInfo.from(paginatedLoadProfileData, thesaurus, clock, evaluator);
//            infos = filter(infos, uriInfo.getQueryParameters()); TODO
            PagedInfoList pagedInfoList = PagedInfoList.asJson("data", infos, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private boolean hasSuspects(LoadProfileDataInfo info) {
        return info.channelValidationData.values().stream().anyMatch(v -> ValidationStatus.SUSPECT.equals(v.validationResult));
    }

    private List<LoadProfileDataInfo> filter(List<LoadProfileDataInfo> infos, MultivaluedMap<String, String> queryParameters) {
        List<String> validationResult = queryParameters.get("validationResult");
        if (validationResult != null && !validationResult.isEmpty()) {
            // TODO
        }
        return null;
    }

    @Path("{lpid}/channels")
    public ChannelResource getChannelResource() {
        return channelResourceProvider.get();
    }

}
