package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterMapping;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/registertypes")
public class RegisterTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public RegisterTypeResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
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
    public RegisterMappingInfo getRegisterType(@PathParam("id") Long id) {
        return new RegisterMappingInfo(deviceConfigurationService.findRegisterMapping(id));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterType(@PathParam("id") Long id) {
        RegisterMapping registerMapping = findRegisterTypeOrThrowException(id);
        registerMapping.delete();
        return Response.ok().build();
    }

    private RegisterMapping findRegisterTypeOrThrowException(Long id) {
        RegisterMapping registerMapping = deviceConfigurationService.findRegisterMapping(id);
        if (registerMapping==null) {
            throw new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND);
        }
        return registerMapping;
    }
}
