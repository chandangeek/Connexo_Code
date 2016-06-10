package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Resource to manage end point configurations
 */
@Path("/endpointconfigurations")
public class EndPointConfigurationResource {

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public EndPointConfigurationResource(EndPointConfigurationService endPointConfigurationService, EndPointConfigurationInfoFactory endPointConfigurationInfoFactory, ExceptionFactory exceptionFactory) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public PagedInfoList getEndPointConfigurations(@BeanParam JsonQueryParameters queryParams) {
        List<EndPointConfigurationInfo> infoList = endPointConfigurationService.findEndPointConfigurations()
                .from(queryParams)
                .stream()
                .map(endPointConfigurationInfoFactory::from)
                .collect(toList());
        return PagedInfoList.fromPagedList("endpoints", infoList, queryParams);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    public EndPointConfigurationInfo getEndPointConfiguration(@PathParam("id") long id) {
        return endPointConfigurationService.getEndPointConfiguration(id)
                .map(endPointConfigurationInfoFactory::from)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response createEndPointConfiguration(EndPointConfigurationInfo info) {
        if (info == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.PAYLOAD_EXPECTED);
        }
        if (info.type == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "type");
        }
        if (info.active == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "active");
        }
        EndPointConfiguration endPointConfiguration = info.type.create(endPointConfigurationInfoFactory, info);
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration);
        return Response.status(Response.Status.CREATED).entity(endPointConfigurationInfo).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    public Response updateEndPointConfiguration(@PathParam("id") long id, EndPointConfigurationInfo info) {
        validatePostPayload(info);
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, info.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
        if (!info.active) { // Changes will be ignored if EndPointConfig is active, all but the actual active-state
            info.type.applyChanges(endPointConfigurationInfoFactory, endPointConfiguration, info);
            endPointConfiguration.save();
        }
        if (info.active && !endPointConfiguration.isActive()) {
            endPointConfigurationService.activate(endPointConfiguration);
        } else if (!info.active && endPointConfiguration.isActive()) {
            endPointConfigurationService.deactivate(endPointConfiguration);
        }
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration);
        return Response.ok(endPointConfigurationInfo).build();
    }

    private void validatePostPayload(EndPointConfigurationInfo info) {
        if (info == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.PAYLOAD_EXPECTED);
        }
        if (info.version == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "version");
        }
        if (info.type == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "type");
        }
        if (info.schemaValidation == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "schemaValidation");
        }
        if (info.httpCompression == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "httpCompression");
        }
        if (info.tracing == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "tracing");
        }
        if (info.active == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "active");
        }
    }


}
