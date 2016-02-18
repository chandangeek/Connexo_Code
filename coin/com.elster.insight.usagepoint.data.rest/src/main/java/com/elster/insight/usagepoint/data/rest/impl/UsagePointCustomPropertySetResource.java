package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.rest.IntervalInfo;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.cps.rest.ValuesRangeConflictInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.google.common.collect.Range;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final ResourceHelper resourceHelper;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public UsagePointCustomPropertySetResource(CustomPropertySetInfoFactory customPropertySetInfoFactory,
                                               ResourceHelper resourceHelper,
                                               CustomPropertySetService customPropertySetService) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.resourceHelper = resourceHelper;
        this.customPropertySetService = customPropertySetService;
    }

    private PagedInfoList getCustomPropertySetValues(UsagePointCustomPropertySetExtension usagePointExtension,
                                                     List<RegisteredCustomPropertySet> customPropertySetValues,
                                                     JsonQueryParameters queryParameters) {
        List<CustomPropertySetInfo> infos = customPropertySetValues
                .stream()
                .map(rcps -> customPropertySetInfoFactory.getFullInfo(rcps, usagePointExtension.getCustomPropertySetValue(rcps)))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCustomPropertySets(@PathParam("mrid") String usagePointMrid,
                                                  @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getAllCustomPropertySets(), queryParameters);
    }

    @GET
    @Path("/metrologyconfiguration")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCustomPropertySetsOnMetrologyConfiguration(@PathParam("mrid") String usagePointMrid,
                                                                       @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration(), queryParameters);
    }

    @GET
    @Path("/servicecategory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCustomPropertySetsOnServiceCategory(@PathParam("mrid") String usagePointMrid,
                                                                @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getCustomPropertySetsOnServiceCategory(), queryParameters);
    }

    @GET
    @Path("/{rcpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CustomPropertySetInfo getCustomPropertySetByRegisteredId(@PathParam("mrid") String usagePointMrid,
                                                                    @PathParam("id") long rcpsId,
                                                                    @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        return customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet,
                usagePointExtension.getCustomPropertySetValue(registeredCustomPropertySet));
    }

    @PUT
    @Path("/{rcpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public CustomPropertySetInfo setCustomPropertySetValuesByRegisteredId(@PathParam("mrid") String usagePointMrid,
                                                                          @PathParam("id") long rcpsId,
                                                                          @BeanParam JsonQueryParameters queryParameters,
                                                                          CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .lockUsagePointCustomPropertySetExtensionOrThrowException(info.parent);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        CustomPropertySetValues values = this.customPropertySetInfoFactory.getCustomPropertySetValues(info, customPropertySet.getPropertySpecs());
        usagePointExtension.setCustomPropertySetValue(customPropertySet, values);
        return customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet,
                usagePointExtension.getCustomPropertySetValue(registeredCustomPropertySet));
    }

    @GET
    @Path("{rcpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IntervalInfo getCurrentTimeSlicedCustomPropertySetInterval(@PathParam("mrid") String usagePointMrid,
                                                                      @PathParam("id") long rcpsId,
                                                                      @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        return IntervalInfo.from(usagePointExtension.getCurrentInterval(registeredCustomPropertySet));
    }

    @GET
    @Path("{rcpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllTimeSlicedCustomPropertySetVersions(@PathParam("mrid") String usagePointMrid,
                                                                   @PathParam("id") long rcpsId,
                                                                   @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        List<CustomPropertySetInfo> versions = customPropertySetService
                .getAllVersionedValuesFor(customPropertySet, usagePointExtension.getUsagePoint())
                .stream()
                .map(value -> customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet, value))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("versions", versions, queryParameters);
    }

    @POST
    @Path("{rcpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response addNewVersionForTimeSlicedCustomAttributeSet(@PathParam("mrid") String usagePointMrid,
                                                                 @PathParam("id") long rcpsId,
                                                                 @BeanParam JsonQueryParameters queryParameters,
                                                                 CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        validateRangeSourceValues(info.startTime, info.endTime);
        Range<Instant> range = getInstantRange(info.startTime, info.endTime);
        List<ValuesRangeConflict> valuesRangeConflicts = customPropertySetService
                .calculateOverlapsFor(customPropertySet, usagePointExtension.getUsagePoint()).whenCreating(range);
        if (!valuesRangeConflicts.isEmpty()) {
            UsagePointAddVersionFailResponse errorInfo = new UsagePointAddVersionFailResponse(valuesRangeConflicts);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorInfo).build();
        }
        CustomPropertySetValues versionValues = customPropertySetInfoFactory.getCustomPropertySetValues(info, customPropertySet.getPropertySpecs());
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), usagePointExtension.getUsagePoint(), versionValues, range);
        return Response.ok().build();
    }

    private void validateRangeSourceValues(Long start, Long end) {
        new RestValidationBuilder()
                .on(end)
                .check(endTime -> endTime == null || start == null || endTime > start)
                .field("endTime")
                .message(MessageSeeds.END_DATE_MUST_BE_AFTER_START_DATE).test().validate();
    }

    private Range<Instant> getInstantRange(Long start, Long end) {
        Range<Instant> range;
        if (start == null && end == null) {
            range = Range.all();
        } else if (start != null && end != null) {
            range = Range.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end));
        } else if (start != null) {
            range = Range.atLeast(Instant.ofEpochMilli(start));
        } else {
            range = Range.lessThan(Instant.ofEpochMilli(end));
        }
        return range;
    }

    @GET
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTimeSlicedCustomAttributeSetVersion(@PathParam("mrid") String usagePointMrid,
                                                                @PathParam("id") long rcpsId,
                                                                @BeanParam JsonQueryParameters queryParameters) {
        return null;
    }

    @PUT
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList updateTimeSlicedCustomAttributeSetVersion(@PathParam("mrid") String usagePointMrid,
                                                                   @PathParam("id") long rcpsId,
                                                                   @BeanParam JsonQueryParameters queryParameters) {
        return null;
    }

    @GET
    @Path("{rcpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTimeSlicedCustomPropertySetVersionsConflicts(@PathParam("mrid") String usagePointMrid,
                                                                         @PathParam("id") long rcpsId,
                                                                         @BeanParam JsonQueryParameters queryParameters,
                                                                         @QueryParam("startTime") Long startTime,
                                                                         @QueryParam("endTime") Long endTime) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        validateRangeSourceValues(startTime, endTime);
        Range<Instant> range = getInstantRange(startTime, endTime);
        List valuesRangeConflicts = customPropertySetService
                .calculateOverlapsFor(customPropertySet, usagePointExtension.getUsagePoint()).whenCreating(range)
                .stream()
                .map(conflict -> {
                    ValuesRangeConflictInfo conflictInfo = customPropertySetInfoFactory.getValuesRangeConflictInfo(conflict);
                    conflictInfo.customPropertySet = customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet, conflict.getValues());
                    return conflictInfo;
                })
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("conflicts", valuesRangeConflicts, queryParameters);
    }

    // WAT???
    @GET
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList some(@PathParam("mrid") String usagePointMrid,
                              @PathParam("id") long rcpsId,
                              @BeanParam JsonQueryParameters queryParameters) {
        return null;
    }

    @GET
    @Path("/{rcps_id}")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getRegisteredCustomPropertySet(@PathParam("mrid") String usagePointMrid,
                                                   @PathParam("rcps_id") long rcpsId,
                                                   @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        for (Map.Entry<RegisteredCustomPropertySet, CustomPropertySetValues> entry : usagePointExtension.getCustomPropertySetValues().entrySet()) {
            RegisteredCustomPropertySet registeredCustomPropertySet = entry.getKey();
            if (!registeredCustomPropertySet.isViewableByCurrentUser()){
                continue;
            }
            if (registeredCustomPropertySet.getId() == rcpsId){
                CustomPropertySet<?,?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
                CustomPropertySetValues customPropertySetValue = entry.getValue();
                CustomPropertySetInfo info = customPropertySetInfoFactory.getGeneralInfo(registeredCustomPropertySet);
                info.properties = customPropertySet.getPropertySpecs()
                        .stream()
                        .map(propertySpec -> customPropertySetInfoFactory.getPropertyInfo(propertySpec,
                                key -> customPropertySetValue != null ? customPropertySetValue.getProperty(key) : null))
                        .collect(Collectors.toList());
                return Response.ok(info).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
