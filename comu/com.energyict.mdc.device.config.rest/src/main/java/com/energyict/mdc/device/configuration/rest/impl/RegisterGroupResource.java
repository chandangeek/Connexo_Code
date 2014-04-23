package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.Finder;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/registergroups")
public class RegisterGroupResource {

    private final MasterDataService masterDataService;
    private final ResourceHelper resourceHelper;

    @Inject
    public RegisterGroupResource(MasterDataService masterDataService, ResourceHelper resourceHelper) {
        super();
        this.masterDataService = masterDataService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterGroups(@BeanParam QueryParameters queryParameters) {
        List<RegisterGroup> allRegisterGroups = this.masterDataService.findAllRegisterGroups();
        List<RegisterGroupInfo> registerGroupInfos = new ArrayList<>();
        for(RegisterGroup registerGroup : allRegisterGroups){
            registerGroupInfos.add(new RegisterGroupInfo(registerGroup));
        }

        return PagedInfoList.asJson("registerGroups", registerGroupInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo getRegisterGroup(@PathParam("id") long id) {
        return new RegisterGroupInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterGroup(@PathParam("id") long id) {
        resourceHelper.findRegisterGroupByIdOrThrowException(id).delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo createRegisterGroup(RegisterGroupInfo registerGroupInfo) {
        //TODO: add transactional context here - it wasn't for RegisterTypes?
        RegisterGroup newGroup = this.masterDataService.newRegisterGroup(registerGroupInfo.name);
        for(RegisterMappingInfo mapping : registerGroupInfo.registerTypes){
            newGroup.addRegisterMapping(resourceHelper.findRegisterMappingByIdOrThrowException(mapping.id));
        }
        newGroup.save();

        return null;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo updateRegisterGroup(@PathParam("id") long id, RegisterGroupInfo registerGroupInfo) {
        /*RegisterMapping registerMapping = this.resourceHelper.findRegisterMappingByIdOrThrowException(id);
        registerMappingInfo.writeTo(registerMapping, findReadingType(registerMappingInfo));
        registerMapping.save();
        return new RegisterMappingInfo(registerMapping, this.deviceConfigurationService.isRegisterMappingUsedByDeviceType(registerMapping));*/
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        group.setName(registerGroupInfo.name);
        //TODO: remove all register mappings here
        group.removeRegisterMappings();

        for(RegisterMappingInfo mapping : registerGroupInfo.registerTypes){
            group.addRegisterMapping(resourceHelper.findRegisterMappingByIdOrThrowException(mapping.id));
        }
        group.save();
        return new RegisterGroupInfo(group);
    }
}
