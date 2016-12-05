package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.impl.servicecall.CommandRunStatusInfo;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHelper;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandInfo;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.JsonQueryParameters;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints")
public class UsagePointResource {

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final Provider<ElectricityDetailResource> electricityDetailResourceProvider;
    private final Provider<GasDetailResource> gasDetailResourceProvider;
    private final Provider<HeatDetailsResource> heatDetailResourceProvider;
    private final Provider<WaterDetailResource> waterDetailResourceProvider;
    private final UsagePointCommandHelper usagePointCommandHelper;

    @Inject
    public UsagePointResource(MeteringService meteringService, UsagePointInfoFactory usagePointInfoFactory,
                              ExceptionFactory exceptionFactory,
                              Provider<ElectricityDetailResource> electricityDetailResourceProvider,
                              Provider<GasDetailResource> gasDetailResourceProvider,
                              Provider<HeatDetailsResource> heatDetailResourceProvider,
                              Provider<WaterDetailResource> waterDetailResourceProvider,
                              UsagePointCommandHelper usagePointCommandHelper) {
        this.meteringService = meteringService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.electricityDetailResourceProvider = electricityDetailResourceProvider;
        this.gasDetailResourceProvider = gasDetailResourceProvider;
        this.heatDetailResourceProvider = heatDetailResourceProvider;
        this.waterDetailResourceProvider = waterDetailResourceProvider;
        this.usagePointCommandHelper = usagePointCommandHelper;
    }

    /**
     * <p>A usage point is a point in the grid where data is measured (energy consumption and/or production). This point can
     * be either virtual (in order to perform calculations within the system), or physical. A physical usage point can be
     * either used to deliver a service or not in which case we speak of a service delivery point.</p>
     * <p>
     * <p>The usage point is often also refered to as a service delivery point or a point of delivery (typically UK with a
     * meter point reference number - MPRN or MPAN).</p>
     * <p>
     * <p>On a physical usage point one or multiple meters can be installed (sub metering, subtracting or control metering,
     * etc.), and these can change over time.</p>
     *
     * @param mRID Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The usage point as identified
     * @summary fetch a usage point by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointInfo getUsagePoint(@PathParam("mRID") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return meteringService.findUsagePointByMRID(mRID)
                .map(ct -> usagePointInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
    }

    /**
     * <p>A usage point is a point in the grid where data is measured (energy consumption and/or production). This point can
     * be either virtual (in order to perform calculations within the system), or physical. A physical usage point can be
     * either used to deliver a service or not in which case we speak of a service delivery point.</p>
     * <p>
     * <p>The usage point is often also refered to as a service delivery point or a point of delivery (typically UK with a
     * meter point reference number - MPRN or MPAN).</p>
     * <p>
     * <p>On a physical usage point one or multiple meters can be installed (sub metering, subtracting or control metering,
     * etc.), and these can change over time.</p>
     *
     * @param mRID Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param usagePointInfo JSON description of new usage point values
     * @return The updated usage point
     * @summary Update a usage point
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public UsagePointInfo updateUsagePoint(@PathParam("mRID") String mRID, UsagePointInfo usagePointInfo, @Context UriInfo uriInfo) {
        if (usagePointInfo == null || usagePointInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        UsagePoint usagePoint = meteringService.findAndLockUsagePointByMRIDAndVersion(mRID, usagePointInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        usagePointInfoFactory.updateUsagePoint(usagePoint, usagePointInfo);

        return usagePointInfoFactory.from(usagePoint, uriInfo, Collections.emptyList());
    }

    /**
     * <p>A usage point is a point in the grid where data is measured (energy consumption and/or production). This point can
     * be either virtual (in order to perform calculations within the system), or physical. A physical usage point can be
     * either used to deliver a service or not in which case we speak of a service delivery point.</p>
     * <p>
     * <p>The usage point is often also refered to as a service delivery point or a point of delivery (typically UK with a
     * meter point reference number - MPRN or MPAN).</p>
     * <p>
     * <p>On a physical usage point one or multiple meters can be installed (sub metering, subtracting or control metering,
     * etc.), and these can change over time.</p>
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return Paged list of usage poins
     * @summary fetch a usage point by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<UsagePointInfo> getAllUsagePoints(@BeanParam FieldSelection fieldSelection,
                                                           @Context UriInfo uriInfo,
                                                           @BeanParam JsonQueryParameters queryParameters) {
        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setAccountabilityOnly(false);
        List<UsagePointInfo> infos = meteringService.getUsagePoints(usagePointFilter)
                .from(queryParameters).stream()
                .map(ct -> usagePointInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(UsagePointResource.class);

        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }


    /**
     * <p>A usage point is a point in the grid where data is measured (energy consumption and/or production). This point can
     * be either virtual (in order to perform calculations within the system), or physical. A physical usage point can be
     * either used to deliver a service or not in which case we speak of a service delivery point.</p>
     * <p>
     * <p>The usage point is often also refered to as a service delivery point or a point of delivery (typically UK with a
     * meter point reference number - MPRN or MPAN).</p>
     * <p>
     * <p>On a physical usage point one or multiple meters can be installed (sub metering, subtracting or control metering,
     * etc.), and these can change over time.</p>
     *
     * @param usagePointInfo JSON description of new usage point values
     * @param uriInfo uriInfo
     * @return The updated usage point
     * @summary Create a usage point
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public Response createUsagePoint(UsagePointInfo usagePointInfo, @Context UriInfo uriInfo) {
        if (usagePointInfo.serviceKind == null) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.FIELD_MISSING, "serviceKind");
        }
        UsagePoint usagePoint = usagePointInfoFactory.createUsagePoint(usagePointInfo);

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getUsagePoint").
                build(usagePoint.getMRID());

        return Response.created(uri).build();
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
        List<String> fields = usagePointInfoFactory.getAvailableFields().stream().sorted().collect(toList());
        fields.add("serviceKind"); // Jackson type property
        return fields;
    }


    @Path("/{mRID}/details")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Object getDetailsResource(@PathParam("mRID") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePoint usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                return electricityDetailResourceProvider.get().init(usagePoint);
            case GAS:
                return gasDetailResourceProvider.get().init(usagePoint);
            case HEAT:
                return heatDetailResourceProvider.get().init(usagePoint);
            case WATER:
                return waterDetailResourceProvider.get().init(usagePoint);
            default:
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.UNSUPPORTED_SERVICE_KIND);
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}/commands")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public CommandRunStatusInfo runCommandOnUsagePoint(@PathParam("mRID") String mRID, UsagePointCommandInfo usagePointCommandInfo) {
        UsagePoint usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        return usagePointCommandInfo.command.process(usagePoint, usagePointCommandInfo, usagePointCommandHelper);
    }

    /**
     * Delete a usage point identified by mRID.
     * This call requires a payload is sent in the request body to provide an actual version of target usage point.
     *
     * @param mRID The usage point's unique mRID identifier
     * @param usagePointInfo JSON description of a usage point to be deleted
     * @return No content
     * @summary Delete a usage point
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public Response deleteUsagePoint(@PathParam("mRID") String mRID, UsagePointInfo usagePointInfo, @Context UriInfo uriInfo) {
        if (usagePointInfo == null || usagePointInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        UsagePoint usagePoint = meteringService.findAndLockUsagePointByMRIDAndVersion(mRID, usagePointInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        usagePoint.makeObsolete();
        return Response.noContent().build();
    }
}
