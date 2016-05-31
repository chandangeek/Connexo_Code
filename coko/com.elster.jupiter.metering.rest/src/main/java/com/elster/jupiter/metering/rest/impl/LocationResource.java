package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationBuilder.LocationMemberBuilder;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Path("/location")
public class LocationResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public LocationResource(RestQueryService queryService, MeteringService meteringService, TransactionService transactionService, ExceptionFactory exceptionFactory) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public LocationMemberInfos getLocations(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<LocationMember> list = queryLocations(maySeeAny(securityContext), params);
        return toLocationMemberInfos(params.clipToLimit(list), params.getStartInt(), params.getLimit());
    }

    private LocationMemberInfos toLocationMemberInfos(List<LocationMember> list, int start, int limit) {
        LocationMemberInfos infos = new LocationMemberInfos(list);
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.Constants.VIEW_ANY_USAGEPOINT);
    }

    private List<LocationMember> queryLocations(boolean maySeeAny, QueryParameters queryParameters) {
        Query<LocationMember> query = meteringService.getLocationMemberQuery();
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        List<LocationMember> locations = queryService.wrap(query).select(queryParameters);
        return locations;
    }

    private List<LocationMemberInfo> convertToLocationMemberInfos(List<LocationMember> locations) {
        List<LocationMemberInfo> locationMemberInfos = new ArrayList<LocationMemberInfo>();
        for (LocationMember location : locations) {
            LocationMemberInfo mi = new LocationMemberInfo(location);
            locationMemberInfos.add(mi);
        }
        return locationMemberInfos;
    }


    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public LocationMemberInfos getLocations(@PathParam("id") long id, @Context SecurityContext securityContext) {
        LocationMemberInfos result = null;
        if (maySeeAny(securityContext)) {
            Location location = fetchLocation(id);
            List<? extends LocationMember> locationMembers = fetchAllLocationMembers(location);
            result = new LocationMemberInfos(locationMembers);
        }
        return result;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Path("/{id}/{locale}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public LocationMemberInfos getLocation(@PathParam("id") long id, @PathParam("locale") String locale, @Context SecurityContext securityContext) {
        LocationMemberInfos result = null;
        if (maySeeAny(securityContext)) {
            Location location = fetchLocation(id);
            LocationMember locationMember = fetchLocationMember(location, locale);
            result = new LocationMemberInfos(locationMember);
        }
        return result;
    }


    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response createLocation(LocationMemberInfo info) {
        try (TransactionContext context = transactionService.getContext()) {
            Optional<Location> location = meteringService.findDeviceLocation(info.locationId);
            if (location.isPresent()) {
                Optional<LocationMember> member = location.get().getMember(info.locale);
                if (member.isPresent()) {
                    throw exceptionFactory.newException(MessageSeeds.DUPLICATE_LOCATION_ENTRY);
                }
            }
            doCreateLocation(info);
            context.commit();
            return Response.ok().build();
        }
    }


    @PUT
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response updateLocation(@PathParam("id") long id, LocationMemberInfo info, @Context UriInfo uriInfo) {
        try (TransactionContext context = transactionService.getContext()) {
            Location location = meteringService.findLocation(id)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_LOCATION));
            Optional<LocationMember> member = location.getMember(info.locale);
            if (!member.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOCATION);
            } else {
                doUpdateLocation(info);
                context.commit();
                return Response.ok(info).build();
            }
        }
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response deleteLocation(@PathParam("id") long id) {
        Location location = meteringService.findLocation(id).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_LOCATION));
        location.remove();
        return Response.noContent().build();
    }

    private Location doCreateLocation(LocationMemberInfo info) {
        LocationBuilder builder = meteringService.newLocationBuilder();
        setLocationAttributes(builder.member(), info);
        return builder.create();
    }

    private Location doUpdateLocation(LocationMemberInfo info) {
        LocationBuilder builder = meteringService.newLocationBuilder();
        Optional<LocationMemberBuilder> memberBuilder = builder.getMember(info.locale);
        if (memberBuilder.isPresent()) {
            setLocationAttributes(memberBuilder.get(), info);
            return builder.create();
        } else {
            return doCreateLocation(info);
        }
    }

    void setLocationAttributes(LocationMemberBuilder builder, LocationMemberInfo info) {
        builder.setLocationId(info.locationId)
                .setCountryCode(info.countryCode)
                .setCountryName(info.countryName)
                .setAdministrativeArea(info.administrativeArea)
                .setLocality(info.locality)
                .setSubLocality(info.subLocality)
                .setStreetType(info.streetType)
                .setStreetName(info.streetName)
                .setStreetNumber(info.streetNumber)
                .setEstablishmentType(info.establishmentType)
                .setEstablishmentName(info.establishmentName)
                .setEstablishmentNumber(info.establishmentNumber)
                .setAddressDetail(info.addressDetail)
                .setZipCode(info.zipCode)
                .isDaultLocation(info.defaultLocation)
                .setLocale(info.locale);
    }

    private Location fetchLocation(long id) {
        return meteringService.findLocation(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private List<? extends LocationMember> fetchAllLocationMembers(Location location) {
        return location.getMembers();
    }

    private LocationMember fetchLocationMember(Location location, String locale) {
        return location.getMember(locale).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
