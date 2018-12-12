/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints/{mRID}/custompropertysets")
public class UsagePointCustomPropertySetResource {

    private final UsagePointCustomPropertySetInfoFactory usagePointCustomPropertySetInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointCustomPropertySetResource(UsagePointCustomPropertySetInfoFactory usagePointCustomPropertySetInfoFactory, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.usagePointCustomPropertySetInfoFactory = usagePointCustomPropertySetInfoFactory;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Models named set of properties whose values are managed against a usage point.
     *
     * @param mRID Unique identifier of the usage point
     * @param cpsId Id of the custom property set
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The identified custom property set
     * @summary Fetch a set of pre-configured property sets
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{cpsId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointCustomPropertySetInfo getUsagePointCustomPropertySet(@PathParam("mRID") String mRID,
                                                                          @PathParam("cpsId") long cpsId,
                                                                          @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        try {
            UsagePointPropertySet propertySet = meteringService.findUsagePointByMRID(mRID)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                    .forCustomProperties()
                    .getPropertySet(cpsId);
            return usagePointCustomPropertySetInfoFactory.from(propertySet, uriInfo, fieldSelection.getFields());
        } catch (UsagePointCustomPropertySetValuesManageException e) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROPERTY_SET);
        }
    }

    /**
     * /**
     * Models named set of properties whose values are managed against a usage point.
     *
     * @param mRID Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     * @summary Fetch a set of pre-configured property sets
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<UsagePointCustomPropertySetInfo> getUsagePointCustomPropertySets(@PathParam("mRID") String mRID,
                                                                                          @BeanParam FieldSelection fieldSelection,
                                                                                          @Context UriInfo uriInfo,
                                                                                          @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointCustomPropertySetInfo> infos = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                .forCustomProperties()
                .getAllPropertySets().stream()
                .map(ct -> usagePointCustomPropertySetInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(UsagePointCustomPropertySetResource.class)
                .resolveTemplate("mRID", mRID);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Updates the values of the specified custom property set.
     *
     * @param mRID Unique identifier of the usage point
     * @param cpsId Id of the custom property set
     * @param propertySetInfo New property values
     * @param uriInfo uriInfo
     * @return The updated property set
     * @Summary update property values
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{cpsId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public UsagePointCustomPropertySetInfo updateUsagePointCustomPropertySet(@PathParam("mRID") String mRID,
                                                                             @PathParam("cpsId") long cpsId,
                                                                             UsagePointCustomPropertySetInfo propertySetInfo,
                                                                             @Context UriInfo uriInfo) {

        if (propertySetInfo != null && propertySetInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        try {
            UsagePointPropertySet propertySet = meteringService.findAndLockUsagePointByMRIDAndVersion(mRID, propertySetInfo.version)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                    .forCustomProperties()
                    .getPropertySet(cpsId);
            propertySet.setValues(usagePointCustomPropertySetInfoFactory.getValues(propertySetInfo, propertySet));
            return usagePointCustomPropertySetInfoFactory.from(propertySet, uriInfo, Collections.emptyList());
        } catch (UsagePointCustomPropertySetValuesManageException e) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROPERTY_SET);
        }
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return usagePointCustomPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
