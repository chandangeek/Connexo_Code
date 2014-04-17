package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/registertypes")
public class RegisterTypeResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final ResourceHelper resourceHelper;

    @Inject
    public RegisterTypeResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, ResourceHelper resourceHelper) {
        super();
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterTypes(@BeanParam QueryParameters queryParameters) {
        List<RegisterMapping> registerMappings = this.masterDataService.findAllRegisterMappings().from(queryParameters).find();
        List<RegisterMappingInfo> registerTypeInfos = new ArrayList<>();
        for (RegisterMapping registerMapping : registerMappings) {
            registerTypeInfos.add(new RegisterMappingInfo(registerMapping, this.deviceConfigurationService.isRegisterMappingUsedByDeviceType(registerMapping)));
        }
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo getRegisterType(@PathParam("id") long id) {
        RegisterMapping registerMapping = this.resourceHelper.findRegisterMappingByIdOrThrowException(id);
        return new RegisterMappingInfo(registerMapping, this.deviceConfigurationService.isRegisterMappingUsedByDeviceType(registerMapping));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterType(@PathParam("id") long id) {
        RegisterMapping registerMapping = this.resourceHelper.findRegisterMappingByIdOrThrowException(id);
        registerMapping.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo createRegisterMapping(RegisterMappingInfo registerMappingInfo) {
        ReadingType readingType = findReadingType(registerMappingInfo);

        RegisterMapping registerMapping = this.masterDataService.newRegisterMapping(registerMappingInfo.name, registerMappingInfo.obisCode, registerMappingInfo.unit, readingType, registerMappingInfo.timeOfUse);
        registerMappingInfo.writeTo(registerMapping, findReadingType(registerMappingInfo));
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping, false); // It's a new one so cannot be used yet in a DeviceType right
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterMappingInfo updateRegisterMapping(@PathParam("id") long id, RegisterMappingInfo registerMappingInfo) {
        RegisterMapping registerMapping = this.resourceHelper.findRegisterMappingByIdOrThrowException(id);
        registerMappingInfo.writeTo(registerMapping, findReadingType(registerMappingInfo));
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping, this.deviceConfigurationService.isRegisterMappingUsedByDeviceType(registerMapping));
    }

    private ReadingType findReadingType(RegisterMappingInfo registerMappingInfo) {
        return meteringService.getReadingType(registerMappingInfo.readingType.mrid).orNull();
    }

}