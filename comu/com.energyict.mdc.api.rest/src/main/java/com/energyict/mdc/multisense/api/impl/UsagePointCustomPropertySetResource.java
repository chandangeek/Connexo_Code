package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

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

@Path("/usagepoints/{usagePointId}/custompropertysets")
public class UsagePointCustomPropertySetResource {

    private final UsagePointCustomPropertySetInfoFactory usagePointCustomPropertySetInfoFactory;
    private final CustomPropertySetService usagePointCustomPropertySetService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointCustomPropertySetResource(CustomPropertySetService usagePointCustomPropertySetService, UsagePointCustomPropertySetInfoFactory usagePointCustomPropertySetInfoFactory, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.usagePointCustomPropertySetService = usagePointCustomPropertySetService;
        this.usagePointCustomPropertySetInfoFactory = usagePointCustomPropertySetInfoFactory;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{cpsId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointCustomPropertySetInfo getUsagePointCustomPropertySet(@PathParam("usagePointId") long usagePointId,
                                                                          @PathParam("cpsId") long cpsId,
                                                                          @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        try {
            UsagePointPropertySet propertySet = meteringService.findUsagePoint(usagePointId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                    .forCustomProperties()
                    .getPropertySet(cpsId);
            return usagePointCustomPropertySetInfoFactory.from(propertySet, uriInfo, fieldSelection.getFields());
        } catch (UsagePointCustomPropertySetValuesManageException e) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROPERTY_SET);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<UsagePointCustomPropertySetInfo> getUsagePointCustomPropertySets(@PathParam("usagePointId") long usagePointId,
                                                                                          @BeanParam FieldSelection fieldSelection,
                                                                                          @Context UriInfo uriInfo,
                                                                                          @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointCustomPropertySetInfo> infos = meteringService.findUsagePoint(usagePointId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                .forCustomProperties()
                .getAllPropertySets().stream()
                .map(ct -> usagePointCustomPropertySetInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(UsagePointCustomPropertySetResource.class)
                .resolveTemplate("usagePointId", usagePointId);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{cpsId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointCustomPropertySetInfo updateUsagePointCustomPropertySet(@PathParam("usagePointId") long usagePointId,
                                                                             @PathParam("cpsId") long cpsId,
                                                                             UsagePointCustomPropertySetInfo propertySetInfo,
                                                                             @Context UriInfo uriInfo) {

        if (propertySetInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }

        try {
            UsagePointPropertySet propertySet = meteringService.findUsagePoint(usagePointId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_USAGE_POINT))
                    .forCustomProperties()
                    .getPropertySet(cpsId);
            propertySet.setValues(usagePointCustomPropertySetInfoFactory.getValues(propertySetInfo, propertySet));
            return usagePointCustomPropertySetInfoFactory.from(propertySet, uriInfo, Collections.emptyList());
        } catch (UsagePointCustomPropertySetValuesManageException e) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROPERTY_SET);
        }
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return usagePointCustomPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
