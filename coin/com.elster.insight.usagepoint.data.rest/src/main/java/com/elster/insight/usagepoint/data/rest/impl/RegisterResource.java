package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;

public class RegisterResource {

    private final RegisterResourceHelper registerHelper;
    private final MeteringService meteringService;
    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final ExceptionFactory exceptionFactory;
    private final Provider<RegisterDataResource> registerDataResourceProvider;

    @Inject
    public RegisterResource(RegisterResourceHelper registerHelper, ResourceHelper resourceHelper, MeteringService meteringService, ExceptionFactory exceptionFactory, Clock clock,
           Provider<RegisterDataResource> registerDataResourceProvider) {
        this.registerHelper = registerHelper;
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.registerDataResourceProvider = registerDataResourceProvider;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getRegisters(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        return registerHelper.getRegisters(mrid, queryParameters);
    }

    @GET @Transactional
    @Path("/{rt_mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getRegister(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_REGISTER_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        return registerHelper.getRegister(() -> channel, mrid);
    }

    
    @Path("/{rt_mrid}/data")
    public RegisterDataResource getRegisterDataResource() {
            return registerDataResourceProvider.get();
    }            

    @Path("{rt_mrid}/validationstatus")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        UsagePointValidation upv = registerHelper.getUsagePointValidation(usagePoint);
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        ValidationStatusInfo validationStatusInfo = new ValidationStatusInfo(upv.isValidationActive(channel, clock.instant()), upv.getLastChecked(channel), channel.hasData());
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }
    
    @Path("{rt_mrid}/validationpreview")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationStatusPreview(@PathParam("mRID") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        UsagePointValidation upv = registerHelper.getUsagePointValidation(usagePoint);
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        DetailedValidationInfo detailedValidationInfo = registerHelper.getRegisterValidationInfo(usagePoint, channel);
        return Response.status(Response.Status.OK).entity(detailedValidationInfo).build();
    }
    
    @PUT @Transactional
    @Path("/{rt_mrid}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response validateNow(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid, RegisterTriggerValidationInfo info) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        UsagePointValidation upv = registerHelper.getUsagePointValidation(usagePoint);
        resourceHelper.lockUsagePointOrThrowException(usagePoint.getId(), info.version, usagePoint.getName());
        if (info.lastChecked != null) {
            upv.setLastChecked(channel, Instant.ofEpochMilli(info.lastChecked));
        }
        usagePoint.update();
        upv.validateChannel(channel);
        return Response.status(Response.Status.OK).build();
    }

    

}