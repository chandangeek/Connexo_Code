package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("/endpoints")
public class EndPointResource {

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public EndPointResource(EndPointConfigurationService endPointConfigurationService, EndPointConfigurationInfoFactory endPointConfigurationInfoFactory, ExceptionFactory exceptionFactory) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
    public EndPointConfigurationInfo getEndPointConfiguration(@PathParam("id") long id) {
        return endPointConfigurationService.getEndPointConfiguration(id)
                .map(endPointConfigurationInfoFactory::from)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG, id));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response createEndPointConfigurations(EndPointConfigurationInfo info) {
        if (info == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.PAYLOAD_EXPECTED);
        }
        if (info.type == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "type");
        }
        EndPointConfiguration endPointConfiguration = info.type.create(endPointConfigurationInfoFactory, info);
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration);
        return Response.status(Response.Status.CREATED).entity(endPointConfigurationInfo).build();
    }


}
