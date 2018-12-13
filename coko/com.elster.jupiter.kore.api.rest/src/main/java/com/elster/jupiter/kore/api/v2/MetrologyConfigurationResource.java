/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
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

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private final MetrologyConfigurationService metrologyService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public MetrologyConfigurationResource(MetrologyConfigurationService metrologyService, MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory, ExceptionFactory exceptionFactory) {
        this.metrologyService = metrologyService;
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * A metrology configuration is a definition of what is going to be measured. Through a metrology configuration the
     * contract is made between a requirement and a deliverable
     *
     * @param metrologyConfigurationId Id of the metrology configuration
     * @param fieldSelection fieldSelection
     * @param uriInfo uriInfo
     * @return The values of the identified metrologyConfiguration
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{metrologyConfigurationId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public MetrologyConfigurationInfo getMetrologyConfiguration(@PathParam("metrologyConfigurationId") long metrologyConfigurationId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return metrologyService.findMetrologyConfiguration(metrologyConfigurationId)
                .map(ct -> metrologyConfigurationInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));
    }

    /**
     * A metrology configuration is a definition of what is going to be measured. Through a metrology configuration the
     * contract is made between a requirement and a deliverable
     *
     * @param fieldSelection fieldSelection
     * @param uriInfo uriInfo
     * @param queryParameters queryParameters
     * @return All known metrology configurations in the system
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<MetrologyConfigurationInfo> getMetrologys(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<MetrologyConfigurationInfo> infos = metrologyService.findAllMetrologyConfigurations().stream()
                .map(ct -> metrologyConfigurationInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(MetrologyConfigurationResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
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
        return metrologyConfigurationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
