package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Path("/devices")
public class DeviceResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;

    @Inject
    public DeviceResource(RestQueryService queryService, MeteringService meteringService, TransactionService transactionService, Clock clock) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
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
        return securityContext.isUserInRole(Privileges.BROWSE_ANY);
    }
    
    private List<Meter> queryDevices(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Meter> query = meteringService.getMeterQuery();
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        List<Meter> meters = queryService.wrap(query).select(queryParameters);
        return meters;
    }
    
    private List<MeterInfo> convertToMeterInfo(List<Meter> meters) {
    	List<MeterInfo> meterInfos = new ArrayList<MeterInfo>();
    	for (Meter meter : meters) {
    		MeterInfo mi = new MeterInfo(meter);
    		meterInfos.add(mi);
    	}
    	return meterInfos;
    }


    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{mRID}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevice(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
    	MeterInfos result = null;
        if (maySeeAny(securityContext)) {
        	Optional<Meter> ometer = meteringService.findMeter(mRID);
        	if ( ometer.isPresent()) {
        		result = new MeterInfos(ometer.get());
        	}
        }
        return result;
    }
}