package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devices")
public class DeviceResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;

    @Inject
    public DeviceResource(RestQueryService queryService, MeteringService meteringService) {
        this.queryService = queryService;
        this.meteringService = meteringService;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevices(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Meter> list = queryDevices(maySeeAny(securityContext), params);
        return toMeterInfos(params.clipToLimit(list), params.getStartInt(), params.getLimit());
    }

    private MeterInfos toMeterInfos(List<Meter> list, int start, int limit) {
        MeterInfos infos = new MeterInfos(list);
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.Constants.VIEW_ANY_USAGEPOINT);
    }

    private List<Meter> queryDevices(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Meter> query = meteringService.getMeterQuery();
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        return queryService.wrap(query).select(queryParameters);
    }

    @GET
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableMeters(@BeanParam JsonQueryParameters params) {
        Query<Meter> meterQuery = meteringService.getMeterQuery();
        String searchText = params.getLike();
        Integer start = params.getStart().isPresent() ? params.getStart().get() : 1;
        Integer limit = params.getLimit().isPresent() ? params.getLimit().get() : 50;
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? ("*" + searchText + "*") : "*";
        Condition condition = where("mRID").likeIgnoreCase(dbSearchText);
        List<Meter> listMeters = meterQuery.select(condition, start + 1, limit, Order.ascending("mRID"))
                .stream()
                .filter(ed -> ed.getState().isPresent() && !ed.getState().get().getName().equals("dlc.default.removed"))
                .filter(ed -> ed.getState().isPresent() && !ed.getState().get().getName().equals("dlc.default.decomissioned"))
                .collect(Collectors.toList());
        return Response.ok().entity(toMeterInfos(listMeters, start, limit)).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevice(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
        MeterInfos result = null;
        if (maySeeAny(securityContext)) {
            Optional<Meter> ometer = meteringService.findMeter(mRID);
            if (ometer.isPresent()) {
                result = new MeterInfos(ometer.get());
            }
        }
        return result;
    }
}
