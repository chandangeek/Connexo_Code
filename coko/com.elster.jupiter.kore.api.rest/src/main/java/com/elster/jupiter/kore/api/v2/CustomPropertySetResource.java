/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.JsonQueryParameters;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/custompropertysets")
public class CustomPropertySetResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final CustomPropertySetService customPropertySetService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CustomPropertySetResource(CustomPropertySetInfoFactory customPropertySetInfoFactory, CustomPropertySetService customPropertySetService, ExceptionFactory exceptionFactory) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.customPropertySetService = customPropertySetService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Models named set of properties.
     *
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
    public CustomPropertySetInfo getCustomPropertySet(@PathParam("cpsId") String cpsId,
                                                                          @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
            RegisteredCustomPropertySet propertySet = customPropertySetService.findActiveCustomPropertySet(cpsId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_PROPERTY_SET, cpsId));
            return customPropertySetInfoFactory.from(propertySet, uriInfo, fieldSelection.getFields());
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
        return customPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
