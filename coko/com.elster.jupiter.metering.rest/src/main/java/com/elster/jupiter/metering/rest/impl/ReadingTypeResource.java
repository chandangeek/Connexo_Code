package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path("/readingtypes")
public class ReadingTypeResource {

    public static final String EQUIDISTANT = "equidistant";
    private final MeteringService meteringService;
    
    @Inject
    public ReadingTypeResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public ReadingTypeInfos getReadingTypes(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (queryParameters.containsKey(EQUIDISTANT)) {
            boolean equidistant = Boolean.valueOf(queryParameters.getFirst(EQUIDISTANT));
            return new ReadingTypeInfos(
                    equidistant
                            ? meteringService.getAvailableEquidistantReadingTypes()
                            : meteringService.getAvailableNonEquidistantReadingTypes());
        }
        return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
    }
    
    @GET
	@Path("/{mRID}/")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public ReadingTypeInfos getReadingType(@PathParam("mRID") String mRID) {
    	return meteringService.getReadingType(mRID)
    		.map(readingType -> new ReadingTypeInfos(readingType))
    		.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{mRID}/calculated")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getCalculatedReadingType(@PathParam("mRID") String mRID) {
        return meteringService.getReadingType(mRID)
                .map(rt -> rt.getCalculatedReadingType())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .map(readingType -> new ReadingTypeInfos(readingType)).orElse(new ReadingTypeInfos());
    }
 }
