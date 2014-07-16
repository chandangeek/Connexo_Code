package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("/registergroups")
public class RegisterGroupResource {

    public static final String ALL = "all";
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
        for (RegisterGroup registerGroup : allRegisterGroups) {
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
        List<RegisterMappingInfo> registerMappingInfos = registerGroupInfo.registerTypes;
        if (queryParameters.getStart() != null && queryParameters.getStart() < registerGroupInfo.registerTypes.size()) {
            registerMappingInfos = registerMappingInfos.subList(queryParameters.getStart(), registerMappingInfos.size());
        }
        return PagedInfoList.asJson("registerTypes", registerMappingInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRegisterGroup(@PathParam("id") long id) {
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        group.removeRegisterMappings();
        group.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo createRegisterGroup(RegisterGroupInfo registerGroupInfo, @Context UriInfo uriInfo) {
        RegisterGroup newGroup = this.masterDataService.newRegisterGroup(registerGroupInfo.name);
        newGroup.save();

        return updateRegisterMappingInGroup(newGroup, registerGroupInfo, true, getBoolean(uriInfo, ALL));

    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterGroupInfo updateRegisterGroup(@PathParam("id") long id, RegisterGroupInfo registerGroupInfo, @Context UriInfo uriInfo) {
        boolean modified = false;
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        if (!group.getName().equals(registerGroupInfo.name)) {
            group.setName(registerGroupInfo.name);
            modified = true;
        }

        return updateRegisterMappingInGroup(group, registerGroupInfo, modified, getBoolean(uriInfo, ALL));
    }

    private RegisterGroupInfo updateRegisterMappingInGroup(RegisterGroup group, RegisterGroupInfo registerGroupInfo, boolean modified, boolean all) {

        boolean didUpdateMappings = group.updateRegisterMappings(extractMappings(registerGroupInfo, all));

        if (didUpdateMappings || modified) {
            this.masterDataService.validateRegisterGroup(group);
            group.save();
        }
        return new RegisterGroupInfo(group);
    }

    private HashMap<Long, RegisterMapping> extractMappings(RegisterGroupInfo registerGroupInfo, boolean all) {
        HashMap<Long, RegisterMapping> registerMappings = new HashMap<>();
        if (all) {
            List<RegisterMapping> mappings = masterDataService.findAllRegisterMappings().find();
            for (RegisterMapping mapping : mappings) {
                registerMappings.put(mapping.getId(), mapping);
            }
        } else {
            for (RegisterMappingInfo mapping : registerGroupInfo.registerTypes) {
                registerMappings.put(mapping.id, resourceHelper.findRegisterMappingByIdOrThrowException(mapping.id));
            }
        }
        return registerMappings;
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

}
