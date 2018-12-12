/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.cps.rest.ValuesRangeConflictInfo;
import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.util.time.RangeInstantBuilder;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetResource {
    private static final String PRIVILEGE_EDITABLE_CPS = "privilege.edit.usage.point.cps";

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

    private PagedInfoList getCustomPropertySetValues(List<UsagePointPropertySet> customPropertySetValues,
                                                     JsonQueryParameters queryParameters) {
        List<CustomPropertySetInfo> infos = customPropertySetValues
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(rcps -> {
                    CustomPropertySetInfo info = customPropertySetInfoFactory.getFullInfo(rcps, rcps.getValues());
                    info.parent = getParentInfo(rcps.getUsagePoint());
                    return info;
                })
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

    private void validateRangeStart(Long start, UsagePoint usagePoint) {
        new RestValidationBuilder()
                .on(start)
                .check(startTime -> !usagePoint.getInstallationTime().isAfter(Instant.ofEpochMilli(startTime)))
                .field("startTime")
                .message(MessageSeeds.START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE).test().validate();
    }

    private List<ValuesRangeConflict> getValuesRangeConflicts(UsagePointPropertySet usagePointPropertySet,
                                                              boolean returnOnlyGaps,
                                                              Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier) {
        return conflictValuesSupplier.apply(customPropertySetService
                .calculateOverlapsFor(usagePointPropertySet.getCustomPropertySet(), usagePointPropertySet.getUsagePoint()))
                .stream()
                .filter(c -> !returnOnlyGaps
                        || c.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)
                        || c.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE))
                .collect(Collectors.toList());
    }

    private PagedInfoList getConflictsInfo(String usagePointName,
                                           long registeredCustomPropertySetId,
                                           Long versionStartTime, Long versionEndTime,
                                           JsonQueryParameters queryParameters,
                                           Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName).forCustomProperties();
        UsagePointVersionedPropertySet versionedPropertySet = usagePointExtension.getVersionedPropertySet(registeredCustomPropertySetId);
        validateRangeSourceValues(versionStartTime, versionEndTime);
        List valuesRangeConflicts = conflictValuesSupplier.apply(customPropertySetService
                .calculateOverlapsFor(versionedPropertySet.getCustomPropertySet(), usagePointExtension.getUsagePoint()))
                .stream()
                .sorted(Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT))
                .map(conflict -> {
                    ValuesRangeConflictInfo conflictInfo = customPropertySetInfoFactory.getValuesRangeConflictInfo(conflict);
                    conflictInfo.customPropertySet = customPropertySetInfoFactory.getFullInfo(versionedPropertySet, conflict.getValues());
                    return conflictInfo;
                })
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("conflicts", valuesRangeConflicts, queryParameters);
    }

    private UsagePointInfo getParentInfo(UsagePoint usagePoint) {
        UsagePointInfo info = new UsagePointInfo();
        info.id = usagePoint.getId();
        info.mRID = usagePoint.getMRID();
        info.version = usagePoint.getVersion();
        info.name = usagePoint.getName();
        return info;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCustomPropertySets(@PathParam("name") String usagePointName,
                                                  @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension.getAllPropertySets(), queryParameters);
    }

    @GET
    @Path("/metrologyconfiguration")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getCustomPropertySetsOnMetrologyConfiguration(@PathParam("name") String usagePointName,
                                                                       @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension.getPropertySetsOnMetrologyConfiguration(), queryParameters);
    }

    @GET
    @Path("/servicecategory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getCustomPropertySetsOnServiceCategory(@PathParam("name") String usagePointName,
                                                                @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName).forCustomProperties();
        return getCustomPropertySetValues(usagePointExtension.getPropertySetsOnServiceCategory(), queryParameters);
    }

    @GET
    @Path("/{rcpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public CustomPropertySetInfo getCustomPropertySetByRegisteredId(@PathParam("name") String usagePointName,
                                                                    @PathParam("rcpsId") long rcpsId) {
        UsagePointPropertySet propertySet = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName)
                .forCustomProperties()
                .getPropertySet(rcpsId);
        CustomPropertySetInfo info = customPropertySetInfoFactory.getFullInfo(propertySet, propertySet.getValues());
        info.parent = getParentInfo(propertySet.getUsagePoint());
        return info;
    }

    @GET
    @Path("/{rcpsId}/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getCustomPropertySetPrivileges(@PathParam("name") String usagePointName,
                                                   @PathParam("rcpsId") long rcpsId,
                                                   @BeanParam JsonQueryParameters queryParameters) {
        UsagePointPropertySet propertySet = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName)
                .forCustomProperties()
                .getPropertySet(rcpsId);

        List<IdWithNameInfo> privileges = (propertySet.isEditableByCurrentUser()
                ? Collections.singletonList(PRIVILEGE_EDITABLE_CPS)
                : Collections.<String>emptyList())
                .stream()
                .map(privilege -> new IdWithNameInfo(null, privilege))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("privileges", privileges, queryParameters)).build();
    }

    @PUT
    @Path("/{rcpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public CustomPropertySetInfo setCustomPropertySetValuesByRegisteredId(@PathParam("rcpsId") long rcpsId,
                                                                          @BeanParam JsonQueryParameters queryParameters,
                                                                          CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointPropertySet propertySet = resourceHelper
                .lockUsagePointOrThrowException(info.parent)
                .forCustomProperties()
                .getPropertySet(rcpsId);
        propertySet.setValues(this.customPropertySetInfoFactory
                .getCustomPropertySetValues(info, propertySet.getCustomPropertySet().getPropertySpecs()));
        return customPropertySetInfoFactory.getFullInfo(propertySet, propertySet.getValues());
    }

    @GET
    @Path("{rcpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public IntervalInfo getCurrentTimeSlicedCustomPropertySetInterval(@PathParam("name") String usagePointName,
                                                                      @PathParam("rcpsId") long rcpsId) {
        Range<Instant> versionInterval = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName)
                .forCustomProperties()
                .getVersionedPropertySet(rcpsId)
                .getNewVersionInterval();
        return IntervalInfo.from(versionInterval);
    }

    @GET
    @Path("{rcpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getAllTimeSlicedCustomPropertySetVersions(@PathParam("name") String usagePointName,
                                                                   @PathParam("rcpsId") long rcpsId,
                                                                   @BeanParam JsonQueryParameters queryParameters) {
        UsagePointVersionedPropertySet versionedPropertySet = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName)
                .forCustomProperties()
                .getVersionedPropertySet(rcpsId);
        List<CustomPropertySetInfo> versions = versionedPropertySet
                .getAllVersionValues()
                .stream()
                .map(value -> customPropertySetInfoFactory.getFullInfo(versionedPropertySet, value))
                .collect(Collectors.toList());
        Collections.reverse(versions);
        return PagedInfoList.fromCompleteList("versions", versions, queryParameters);
    }

    @POST
    @Path("{rcpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public Response addNewVersionForTimeSlicedCustomAttributeSet(@PathParam("name") String usagePointName,
                                                                 @PathParam("rcpsId") long rcpsId,
                                                                 @QueryParam("forced") boolean forced,
                                                                 @Context SecurityContext securityContext,
                                                                 CustomPropertySetInfo<UsagePointInfo> info) {
        if (!securityContext.isUserInRole(Privileges.Constants.ADMINISTER_USAGEPOINT_TIME_SLICED_CPS)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        info.isVersioned = true;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        UsagePointVersionedPropertySet versionedPropertySet = usagePoint.forCustomProperties().getVersionedPropertySet(rcpsId);
        validateRangeStart(info.startTime, usagePoint);
        validateRangeSourceValues(info.startTime, info.endTime);
        Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier =
                builder -> builder.whenCreating(RangeInstantBuilder.closedOpenRange(info.startTime, info.endTime));
        List<ValuesRangeConflict> valuesRangeConflicts = getValuesRangeConflicts(versionedPropertySet, forced, conflictValuesSupplier);

        if (!valuesRangeConflicts.isEmpty()) {
            UsagePointAddVersionFailResponse errorInfo = new UsagePointAddVersionFailResponse(valuesRangeConflicts);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorInfo).build();
        }
        CustomPropertySetValues versionValues = customPropertySetInfoFactory
                .getCustomPropertySetValues(info, versionedPropertySet.getCustomPropertySet().getPropertySpecs());
        versionedPropertySet.setVersionValues(null, versionValues);
        return Response.ok().build();
    }

    @GET
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public CustomPropertySetInfo getTimeSlicedCustomAttributeSetVersion(@PathParam("name") String usagePointName,
                                                                        @PathParam("rcpsId") long rcpsId,
                                                                        @PathParam("timestamp") long timestamp) {
        UsagePointVersionedPropertySet versionedPropertySet = resourceHelper
                .findUsagePointByNameOrThrowException(usagePointName)
                .forCustomProperties()
                .getVersionedPropertySet(rcpsId);
        CustomPropertySetInfo info = customPropertySetInfoFactory.getFullInfo(versionedPropertySet,
                versionedPropertySet.getVersionValues(Instant.ofEpochMilli(timestamp)));
        info.parent = getParentInfo(versionedPropertySet.getUsagePoint());
        return info;
    }

    @PUT
    @Path("{rcpsId}/versions/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public Response updateTimeSlicedCustomAttributeSetVersion(@PathParam("name") String usagePointName,
                                                              @PathParam("rcpsId") long rcpsId,
                                                              @BeanParam JsonQueryParameters queryParameters,
                                                              @QueryParam("forced") boolean forced,
                                                              @Context SecurityContext securityContext,
                                                              CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        UsagePointVersionedPropertySet versionedPropertySet = usagePoint.forCustomProperties().getVersionedPropertySet(rcpsId);
        validateRangeStart(info.startTime, usagePoint);
        Instant versionStartTime = Instant.ofEpochMilli(info.versionId);
        CustomPropertySetValues values = customPropertySetInfoFactory
                .getCustomPropertySetValues(info, versionedPropertySet.getCustomPropertySet().getPropertySpecs());
        if (!securityContext.isUserInRole(Privileges.Constants.ADMINISTER_USAGEPOINT_TIME_SLICED_CPS)
                && !values.getEffectiveRange().equals(versionedPropertySet.getVersionValues(versionStartTime).getEffectiveRange())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier =
                builder -> builder.whenUpdating(versionStartTime, RangeInstantBuilder.closedOpenRange(info.startTime, info.endTime));
        List<ValuesRangeConflict> valuesRangeConflicts = getValuesRangeConflicts(versionedPropertySet, forced, conflictValuesSupplier);
        if (!valuesRangeConflicts.isEmpty()) {
            UsagePointAddVersionFailResponse errorInfo = new UsagePointAddVersionFailResponse(valuesRangeConflicts);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorInfo).build();
        }
        versionedPropertySet.setVersionValues(versionStartTime, values);
        return Response.ok(customPropertySetInfoFactory.getFullInfo(versionedPropertySet,
                versionedPropertySet.getVersionValues(versionStartTime))).build();
    }

    @GET
    @Path("{rcpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getTimeSlicedCustomPropertySetVersionsConflicts(@PathParam("name") String usagePointName,
                                                                         @PathParam("rcpsId") long rcpsId,
                                                                         @BeanParam JsonQueryParameters queryParameters,
                                                                         @QueryParam("startTime") Long startTime,
                                                                         @QueryParam("endTime") Long endTime) {
        Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier =
                builder -> builder.whenCreating(RangeInstantBuilder.closedOpenRange(startTime, endTime));
        return getConflictsInfo(usagePointName, rcpsId, startTime, endTime, queryParameters, conflictValuesSupplier);
    }

    @GET
    @Path("{rcpsId}/conflicts/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getTimeSlicedCustomPropertySetVersionsConflicts(@PathParam("name") String usagePointName,
                                                                         @PathParam("rcpsId") long rcpsId,
                                                                         @PathParam("timestamp") long timestamp,
                                                                         @BeanParam JsonQueryParameters queryParameters,
                                                                         @QueryParam("startTime") Long startTime,
                                                                         @QueryParam("endTime") Long endTime) {
        Function<OverlapCalculatorBuilder, List<ValuesRangeConflict>> conflictValuesSupplier =
                builder -> builder.whenUpdating(Instant.ofEpochMilli(timestamp), RangeInstantBuilder.closedOpenRange(startTime, endTime));
        return getConflictsInfo(usagePointName, rcpsId, startTime, endTime, queryParameters, conflictValuesSupplier);
    }
}
