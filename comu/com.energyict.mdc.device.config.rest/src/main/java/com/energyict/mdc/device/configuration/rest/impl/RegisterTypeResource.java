package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterMapping;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/registertypes")
public class RegisterTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;

    @Inject
    public RegisterTypeResource(DeviceConfigurationService deviceConfigurationService, MeteringService meteringService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterTypes(@BeanParam QueryParameters queryParameters) {
        List<RegisterMapping> registerMappings = deviceConfigurationService.findAllRegisterMappings().from(queryParameters).find();
        List<RegisterMappingInfo> registerTypeInfos = new ArrayList<>();
        for (RegisterMapping registerMapping : registerMappings) {
            registerTypeInfos.add(new RegisterMappingInfo(registerMapping));
        }
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo getRegisterType(@PathParam("id") long id) {
        return new RegisterMappingInfo(deviceConfigurationService.findRegisterMapping(id));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterType(@PathParam("id") long id) {
        RegisterMapping registerMapping = findRegisterTypeOrThrowException(id);
        registerMapping.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo createRegisterMapping(RegisterMappingInfo registerMappingInfo) {
        ReadingType readingType = findReadingTypeOrThrowException(registerMappingInfo);

        RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping(registerMappingInfo.name, registerMappingInfo.obisCode, registerMappingInfo.unit, readingType, registerMappingInfo.timeOfUse);
        registerMappingInfo.writeTo(registerMapping, findReadingTypeOrThrowException(registerMappingInfo));
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo updateRegisterMapping(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        RegisterMapping registerMapping = findRegisterTypeOrThrowException(id);
        registerMappingInfo.writeTo(registerMapping, findReadingTypeOrThrowException(registerMappingInfo));
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping);
    }

    private ReadingType findReadingTypeOrThrowException(RegisterMappingInfo registerMappingInfo) {
        return meteringService.getReadingType(registerMappingInfo.readingTypeInfo.mrid).orNull();
    }

    private RegisterMapping findRegisterTypeOrThrowException(long id) {
        RegisterMapping registerMapping = deviceConfigurationService.findRegisterMapping(id);
        if (registerMapping==null) {
            throw new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND);
        }
        return registerMapping;
    }
}
