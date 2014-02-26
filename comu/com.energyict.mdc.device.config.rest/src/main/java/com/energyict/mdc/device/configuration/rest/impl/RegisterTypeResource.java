package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo createRegisterMapping(RegisterMappingInfo registerMappingInfo) {
        Optional<ReadingType> readingType = findReadingTypeOrThrowException(registerMappingInfo);
        ProductSpec productSpecByReadingType = findProductSpecOrThrowException(registerMappingInfo, readingType.get());

        return new RegisterMappingInfo(deviceConfigurationService.newRegisterMapping(registerMappingInfo.name, registerMappingInfo.obisCode, productSpecByReadingType));
    }

    private ProductSpec findProductSpecOrThrowException(RegisterMappingInfo registerMappingInfo, ReadingType readingType) {
        ProductSpec productSpecByReadingType = deviceConfigurationService.findProductSpecByReadingType(readingType);
        if (productSpecByReadingType==null) {
            throw new WebApplicationException("No product spec for reading type " + registerMappingInfo.readingTypeInfo.mrid, Response.Status.BAD_REQUEST);
        }
        return productSpecByReadingType;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo updateRegisterMapping(@PathParam("id") Long id, RegisterMappingInfo registerMappingInfo) {
        RegisterMapping registerMapping = findRegisterTypeOrThrowException(id);
        registerMappingInfo.writeTo(registerMapping);
        Optional<ReadingType> readingType = findReadingTypeOrThrowException(registerMappingInfo);
        ProductSpec productSpecByReadingType = findProductSpecOrThrowException(registerMappingInfo, readingType.get());
        registerMapping.setProductSpec(productSpecByReadingType);
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping);
    }

    private Optional<ReadingType> findReadingTypeOrThrowException(RegisterMappingInfo registerMappingInfo) {
        Optional<ReadingType> readingType = meteringService.getReadingType(registerMappingInfo.readingTypeInfo.mrid);
        if (!readingType.isPresent()) {
            throw new WebApplicationException("No reading type " + registerMappingInfo.readingTypeInfo.mrid, Response.Status.BAD_REQUEST);
        }
        return readingType;
    }

    private RegisterMapping findRegisterTypeOrThrowException(Long id) {
        RegisterMapping registerMapping = deviceConfigurationService.findRegisterMapping(id);
        if (registerMapping==null) {
            throw new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND);
        }
        return registerMapping;
    }
}
