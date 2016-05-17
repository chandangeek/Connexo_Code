package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints/{usagePointId}/meteractivations")
public class MeterActivationResource {

    private final MeterActivationInfoFactory meterActivationInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public MeterActivationResource(MeterActivationInfoFactory meterActivationInfoFactory, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.meterActivationInfoFactory = meterActivationInfoFactory;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * @param usagePointId Unique identifier of the usage point
     * @param meterActivationId Unique identifier of the meter activation
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The meter activation on a usage point
     * @summary fetch a specific meter activation on a usage point by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{meterActivationId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public MeterActivationInfo getMeterActivation(@PathParam("usagePointId") long usagePointId, @PathParam("meterActivationId") long meterActivationId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        MeterActivation meterActivation = meteringService.findUsagePoint(usagePointId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT))
                .getMeterActivations().stream()
                .filter(ma -> ma.getId() == meterActivationId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_METER_ACTIVATION));
        return meterActivationInfoFactory.from(meterActivation, uriInfo, fieldSelection.getFields());
    }

    /**
     * @param usagePointId Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return All meter activations on a usage point
     * @summary fetch all meter activation on a usage point
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<MeterActivationInfo> getMeterActivations(@PathParam("usagePointId") long usagePointId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = meteringService.findUsagePoint(usagePointId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        List<MeterActivationInfo> meterActivationInfos = usagePoint
                .getMeterActivations().stream()
                .map(ma -> meterActivationInfoFactory.from(ma, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(MeterActivationResource.class)
                .resolveTemplate("usagePointId", usagePoint.getId());

        return PagedInfoList.from(meterActivationInfos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * @param meterActivationInfo Description of the to be created meter activation
     * @param usagePointId Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @return All meter activations on a usage point
     * @summary Create a new activation for a meter
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public MeterActivationInfo createMeterActivation(@PathParam("usagePointId") long usagePointId, @Context UriInfo uriInfo, MeterActivationInfo meterActivationInfo) {
        if (meterActivationInfo == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EMPTY_REQUEST);
        }
        UsagePoint usagePoint = meteringService.findUsagePoint(usagePointId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));

        if (meterActivationInfo.interval == null || meterActivationInfo.interval.start == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "interval.start");
        }

        MeterActivation activation;
        if (meterActivationInfo.meter != null) {
            Meter meter = meteringService.findMeter(meterActivationInfo.meter)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER, "meter"));
            activation = usagePoint.activate(meter, Instant.ofEpochMilli(meterActivationInfo.interval.start));
        } else {
            activation = usagePoint.activate(Instant.ofEpochMilli(meterActivationInfo.interval.start));
        }
        return meterActivationInfoFactory.from(activation, uriInfo, Collections.emptyList());
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
        return meterActivationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
