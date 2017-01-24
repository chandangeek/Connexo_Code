package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

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

/**
 * Created by bvn on 7/13/15.
 */
@Path("/comportpools")
public class ComPortPoolResource {
    private final EngineConfigurationService engineConfigurationService;
    private final ComPortPoolInfoFactory comPortPoolFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComPortPoolResource(EngineConfigurationService engineConfigurationService, ComPortPoolInfoFactory comPortPoolFactory, ExceptionFactory exceptionFactory) {
        this.engineConfigurationService = engineConfigurationService;
        this.comPortPoolFactory = comPortPoolFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Models a collection of ComPorts with similar characteristics.
     * <br>
     * One of those characteristics is the nature of the ComPort, i.e. if it is inbound or outbound.
     *
     * @summary Fetch a set of communication port pools
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ComPortPoolInfo> getComPortPools(@BeanParam JsonQueryParameters queryParameters,
                                                          @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        List<ComPortPoolInfo> page = ListPager.
                of(engineConfigurationService.findAllComPortPools()).
                from(queryParameters).stream().
                map(cpp -> comPortPoolFactory.from(cpp, uriInfo, fieldSelection.getFields())).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ComPortPoolResource.class);
        return PagedInfoList.from(page, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Models a collection of ComPorts with similar characteristics.
     * <br>
     * One of those characteristics is the nature of the ComPort, i.e. if it is inbound or outbound.
     *
     * @summary Fetch a set of communication port pools
     * @param id Id of the communication port pool
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified communication port pool
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ComPortPoolInfo getComPortPool(@PathParam("id") long id, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        ComPortPool comPortPool = engineConfigurationService.findComPortPool(id).orElseThrow(() -> exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NOT_FOUND));
        return comPortPoolFactory.from(comPortPool, uriInfo, fieldSelection.getFields());
    }
}
