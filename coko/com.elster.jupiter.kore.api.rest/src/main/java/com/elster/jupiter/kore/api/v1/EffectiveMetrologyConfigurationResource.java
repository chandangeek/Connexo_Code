package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
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
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints/{mRID}/metrologyconfigurations")
public class EffectiveMetrologyConfigurationResource {

    private final EffectiveMetrologyConfigurationInfoFactory effectiveMetrologyConfigurationInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public EffectiveMetrologyConfigurationResource(MeteringService meteringService, EffectiveMetrologyConfigurationInfoFactory effectiveMetrologyConfigurationInfoFactory, ExceptionFactory exceptionFactory) {
        this.meteringService = meteringService;
        this.effectiveMetrologyConfigurationInfoFactory = effectiveMetrologyConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * A metrology configuration is a definition of what is going to be measured. Through a metrology configuration the
     * contract is made between a requirement and a deliverable
     *
     * @param mRID Unique identifier of the usage point
     * @param fieldSelection fieldSelection
     * @param uriInfo uriInfo
     * @return The values of the identified efective metrologyConfiguration from usage point
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public EffectiveMetrologyConfigurationInfo getEffectiveMetrologyConfiguration(@PathParam("mRID") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT))
                .getCurrentEffectiveMetrologyConfiguration()
                .map(ct -> effectiveMetrologyConfigurationInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));
    }

    /**
     * A metrology configuration is a definition of what is going to be measured. Through a metrology configuration the
     * contract is made between a requirement and a deliverable
     *
     * @param mRID Unique identifier of the usage point
     * @param timestamp Id of the metrology configuration
     * @param fieldSelection fieldSelection
     * @param uriInfo uriInfo
     * @return The values of the identified metrologyConfiguration
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{timestamp}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public EffectiveMetrologyConfigurationInfo getMetrologyConfiguration(@PathParam("mRID") String mRID, @PathParam("timestamp") long timestamp, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT))
                .getEffectiveMetrologyConfiguration(Instant.ofEpochMilli(timestamp))
                .map(ct -> effectiveMetrologyConfigurationInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));
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
        return effectiveMetrologyConfigurationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
