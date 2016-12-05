package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints/{mRID}/details")
// This tag exists for Miredot only. Is not required for production as this resource is obtained through sub-resource location
public class ElectricityDetailResource {

    private final ElectricityDetailInfoFactory electricityDetailInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private UsagePoint usagePoint;
    private final MeteringService meteringService;

    @Inject
    public ElectricityDetailResource(ElectricityDetailInfoFactory electricityDetailInfoFactory, ExceptionFactory exceptionFactory, Clock clock, MeteringService meteringService) {
        this.electricityDetailInfoFactory = electricityDetailInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.meteringService = meteringService;
    }

    ElectricityDetailResource init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    /**
     * Usage points have detailed information. These details have time-based values: each time values change, a new set
     * of details is created, bound to a validity period called 'effectivity'.
     * The detail properties of a usage point vary according to the usage points Service category. This means an
     * electricity usage point and a gas usage point will have different detail properties.
     * The Id of the details is the time in milliseconds when the details became active. Each detail will have a link
     * to the temporal predecessor and successor, if one exists.
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param time Instant for which one wants to obtain the effective details (milliseconds since Epoch)
     * @return Details that were effective at the lowerEnd
     * @summary Fetch details that are effective at a certain moment
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{time}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getElectricityDetails(@PathParam("time") long time, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        UsagePointDetail usagePointDetail = usagePoint.getDetail(Instant.ofEpochMilli(time))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DETAIL));
        return electricityDetailInfoFactory.from((ElectricityDetail) usagePointDetail, uriInfo, fieldSelection.getFields());
    }

    /**
     * Usage points have detailed information. These details have time-based values: each time values change, a new set
     * of details is created, bound to a validity period called 'effectivity'.
     * The detail properties of a usage point vary according to the usage points Service category. This means an
     * electricity usage point and a gas usage point will have different detail properties.
     * The Id of the details is the time in milliseconds when the details became active. Each detail will have a link
     * to the temporal predecessor and successor, if one exists.
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Details that are currently effective.
     * @summary Fetch details that are currently effective
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ElectricityDetailInfo getCurrentElectricityDetails(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return getElectricityDetails(Instant.now(clock).toEpochMilli(), fieldSelection, uriInfo);
    }

    /**
     * Usage points have detailed information. These details have time-based values: each time values change, a new set
     * of details is created, bound to a validity period called 'effectivity'.
     * The detail properties of a usage point vary according to the usage points Service category. This means an
     * electricity usage point and a gas usage point will have different detail properties.
     * The Id of the details is the time in milliseconds when the details became active. Each detail will have a link
     * to the temporal predecessor and successor, if one exists.
     *
     * @param detailInfo JSON description of new usage point detail values
     * @param uriInfo uriInfo
     * @return The updated details
     * @summary Create a new version of the usage point details
     */
    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createElectricityDetail(ElectricityDetailInfo detailInfo, @Context UriInfo uriInfo) {
        if (detailInfo == null || detailInfo.version == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.VERSION_MISSING, "version");
        }
        meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), detailInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (detailInfo.effectivity == null || detailInfo.effectivity.lowerEnd == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "effectivity.lowerEnd");
        }
        UsagePointDetailBuilder builder = electricityDetailInfoFactory.createDetail(usagePoint, detailInfo);
        builder.validate();
        UsagePointDetail detail = builder.create();

        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDetailsResource").
                path(ElectricityDetailResource.class, "getElectricityDetails").
                build(usagePoint.getMRID(), detail.getRange().lowerEndpoint().toEpochMilli());

        return Response.created(uri).build();
    }

    /**
     * List the fields available on this type of entity. In the case of usage points details, these fields will
     * depend on the service category of the usage point being queried.
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
        return electricityDetailInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
