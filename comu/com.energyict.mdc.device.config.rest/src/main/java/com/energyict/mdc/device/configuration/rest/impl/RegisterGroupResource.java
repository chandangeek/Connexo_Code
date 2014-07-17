package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
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
        List<RegisterGroup> allRegisterGroups = this.masterDataService.findAllRegisterGroups().from(queryParameters).find();
        List<RegisterGroupInfo> registerGroupInfos = new ArrayList<>();
        for(RegisterGroup registerGroup : allRegisterGroups){
            registerGroupInfos.add(new RegisterGroupInfo(registerGroup.getId(), registerGroup.getName()));
        }

        return PagedInfoList.asJson("registerGroups", registerGroupInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo getRegisterGroup(@PathParam("id") long id) {
        return new RegisterGroupInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
    }

    @GET
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterTypesOfRegisterGroup(@PathParam("id") long id, @BeanParam QueryParameters queryParameters) {
        RegisterGroupInfo registerGroupInfo = new RegisterGroupInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
        List<RegisterTypeInfo> registerTypeInfos = registerGroupInfo.registerTypes;
        if(queryParameters.getStart() != null && queryParameters.getStart() < registerGroupInfo.registerTypes.size()){
            registerTypeInfos = registerTypeInfos.subList(queryParameters.getStart(), registerTypeInfos.size());
        }
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterGroup(@PathParam("id") long id) {
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        group.removeRegisterTypes();
        group.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo createRegisterGroup(RegisterGroupInfo registerGroupInfo) {
        RegisterGroup newGroup = this.masterDataService.newRegisterGroup(registerGroupInfo.name);
        newGroup.save();

        return updateRegisterTypeInGroup(newGroup, registerGroupInfo, true);

    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo updateRegisterGroup(@PathParam("id") long id, RegisterGroupInfo registerGroupInfo) {
        boolean modified = false;
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        if(!group.getName().equals(registerGroupInfo.name)){
            group.setName(registerGroupInfo.name);
            modified = true;
        }

        return updateRegisterTypeInGroup(group, registerGroupInfo, modified);
    }

    private RegisterGroupInfo updateRegisterTypeInGroup(RegisterGroup group, RegisterGroupInfo registerGroupInfo, boolean modified){
        HashMap<Long, RegisterType> registerType = new HashMap<>();
        for(RegisterTypeInfo mapping : registerGroupInfo.registerTypes){
            registerType.put(mapping.id, resourceHelper.findRegisterTypeByIdOrThrowException(mapping.id));
        }

        modified |= group.updateRegisterTypes(registerType);

        if(modified){
            this.masterDataService.validateRegisterGroup(group);
            group.save();
        }
        return new RegisterGroupInfo(group);
    }
}
