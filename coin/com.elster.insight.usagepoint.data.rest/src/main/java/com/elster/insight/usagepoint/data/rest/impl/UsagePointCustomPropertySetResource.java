package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.cps.rest.ValuesRangeConflictInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.time.RangeInstantBuilder;
import com.elster.jupiter.util.time.RangeInstantComparator;
import com.google.common.collect.Range;

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
import java.util.List;
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

    private void validateRangeSourceValues(Long start, Long end) {
        new RestValidationBuilder()
                .on(end)
                .check(endTime -> endTime == null || start == null || endTime > start)
                .field("endTime")
                .message(MessageSeeds.END_DATE_MUST_BE_AFTER_START_DATE).test().validate();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCustomPropertySets(@PathParam("mrid") String usagePointMrid,
                                                  @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getAllCustomPropertySets(), queryParameters);
    }

    @GET
    @Path("/metrologyconfiguration")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCustomPropertySetsOnMetrologyConfiguration(@PathParam("mrid") String usagePointMrid,
                                                                       @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration(), queryParameters);
    }

    @GET
    @Path("/servicecategory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCustomPropertySetsOnServiceCategory(@PathParam("mrid") String usagePointMrid,
                                                                @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension, usagePointExtension.getCustomPropertySetsOnServiceCategory(), queryParameters);
    }

    @GET
    @Path("/{rcpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CustomPropertySetInfo getCustomPropertySetByRegisteredId(@PathParam("mrid") String usagePointMrid,
                                                                    @PathParam("rcpsId") long rcpsId,
                                                                    @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
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
                                                                          @PathParam("rcpsId") long rcpsId,
                                                                          @BeanParam JsonQueryParameters queryParameters,
                                                                          CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .lockUsagePointOrThrowException(info.parent).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        usagePointExtension.setCustomPropertySetValue(customPropertySet, this.customPropertySetInfoFactory
                .getCustomPropertySetValues(info, customPropertySet.getPropertySpecs()));
        return customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet,
                usagePointExtension.getCustomPropertySetValue(registeredCustomPropertySet));
    }

    @GET
    @Path("{rcpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IntervalInfo getCurrentTimeSlicedCustomPropertySetInterval(@PathParam("mrid") String usagePointMrid,
                                                                      @PathParam("rcpsId") long rcpsId,
                                                                      @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        return IntervalInfo.from(usagePointExtension.getCurrentInterval(registeredCustomPropertySet));
    }

    @GET
    @Path("{rcpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllTimeSlicedCustomPropertySetVersions(@PathParam("mrid") String usagePointMrid,
                                                                   @PathParam("rcpsId") long rcpsId,
                                                                   @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        // TODO move into extension class
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
                                                                 @PathParam("rcpsId") long rcpsId,
                                                                 @BeanParam JsonQueryParameters queryParameters,
                                                                 CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        validateRangeSourceValues(info.startTime, info.endTime);
        Range<Instant> range = RangeInstantBuilder.openClosedRange(info.startTime, info.endTime);
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

    @GET
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CustomPropertySetInfo getTimeSlicedCustomAttributeSetVersion(@PathParam("mrid") String usagePointMrid,
                                                                        @PathParam("rcpsId") long rcpsId,
                                                                        @PathParam("timestamp") long timestamp,
                                                                        @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        return customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet,
                usagePointExtension.getCustomPropertySetValue(registeredCustomPropertySet, Instant.ofEpochMilli(timestamp)));
    }

    @PUT
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public CustomPropertySetInfo updateTimeSlicedCustomAttributeSetVersion(@PathParam("mrid") String usagePointMrid,
                                                                           @PathParam("rcpsId") long rcpsId,
                                                                           @BeanParam JsonQueryParameters queryParameters,
                                                                           CustomPropertySetInfo<UsagePointInfo> info) {
        return null;
    }

    @GET
    @Path("{rcpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTimeSlicedCustomPropertySetVersionsConflicts(@PathParam("mrid") String usagePointMrid,
                                                                         @PathParam("rcpsId") long rcpsId,
                                                                         @BeanParam JsonQueryParameters queryParameters,
                                                                         @QueryParam("startTime") Long startTime,
                                                                         @QueryParam("endTime") Long endTime) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByMrIdOrThrowException(usagePointMrid).forCustomProperties();
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper
                .findRegisteredCustomPropertySetOnUsagePointOrThrowException(rcpsId, usagePointExtension);
        CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        validateRangeSourceValues(startTime, endTime);
        Range<Instant> range = RangeInstantBuilder.openClosedRange(startTime, endTime);
        List valuesRangeConflicts = customPropertySetService
                .calculateOverlapsFor(customPropertySet, usagePointExtension.getUsagePoint()).whenCreating(range)
                .stream()
                .sorted((c1, c2) -> new RangeInstantComparator().compare(c1.getConflictingRange(), c2.getConflictingRange()))
                .map(conflict -> {
                    ValuesRangeConflictInfo conflictInfo = customPropertySetInfoFactory.getValuesRangeConflictInfo(conflict);
                    conflictInfo.customPropertySet = customPropertySetInfoFactory.getFullInfo(registeredCustomPropertySet, conflict.getValues());
                    return conflictInfo;
                })
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("conflicts", valuesRangeConflicts, queryParameters);
    }
}
