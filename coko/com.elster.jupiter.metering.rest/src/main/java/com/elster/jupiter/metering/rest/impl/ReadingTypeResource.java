package com.elster.jupiter.metering.rest.impl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;


@Path("/readingtypes")
public class ReadingTypeResource {

    private final MeteringService meteringService;
    
    @Inject
    public ReadingTypeResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
	@Produces(MediaType.APPLICATION_JSON) 
	public ReadingTypeInfos getReadingTypes() {
    	return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
    }
    
    @GET
	@Path("/{mRID}/")
	@Produces(MediaType.APPLICATION_JSON) 
	public ReadingTypeInfos getReadingType(@PathParam("mRID") String mRID) {
    	return meteringService.getReadingType(mRID)
    		.map(readingType -> new ReadingTypeInfos(readingType))
    		.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

 }
