package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
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
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public RegisterGroupInfo getRegisterGroup(@PathParam("id") long id) {
        return new RegisterGroupInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
    }

    @GET
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public PagedInfoList getRegisterTypesOfRegisterGroup(@PathParam("id") long id, @BeanParam QueryParameters queryParameters) {
        RegisterGroupInfo registerGroupInfo = new RegisterGroupInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
        List<RegisterTypeInfo> registerTypeInfos = registerGroupInfo.registerTypes;
        int totalCount = registerGroupInfo.registerTypes.size();
        if (queryParameters.getStart() != null && queryParameters.getStart() < registerGroupInfo.registerTypes.size()) {
            registerTypeInfos = registerTypeInfos.subList(queryParameters.getStart(), registerTypeInfos.size());
        }
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters, totalCount);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRegisterGroup(@PathParam("id") long id) {
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        group.removeRegisterTypes();
        group.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    public RegisterGroupInfo createRegisterGroup(RegisterGroupInfo registerGroupInfo, @Context UriInfo uriInfo) {
        RegisterGroup newGroup = this.masterDataService.newRegisterGroup(registerGroupInfo.name);
        newGroup.save();

        return updateRegisterTypeInGroup(newGroup, registerGroupInfo, true, getBoolean(uriInfo, ALL));

    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    public RegisterGroupInfo updateRegisterGroup(@PathParam("id") long id, RegisterGroupInfo registerGroupInfo, @Context UriInfo uriInfo) {
        boolean modified = false;
        RegisterGroup group = resourceHelper.findRegisterGroupByIdOrThrowException(id);
        if (!group.getName().equals(registerGroupInfo.name)) {
            group.setName(registerGroupInfo.name);
            modified = true;
        }

        return updateRegisterTypeInGroup(group, registerGroupInfo, modified, getBoolean(uriInfo, ALL));
    }

    private RegisterGroupInfo updateRegisterTypeInGroup(RegisterGroup group, RegisterGroupInfo registerGroupInfo, boolean modified, boolean all) {

        boolean didUpdateMappings = group.updateRegisterTypes(extractMappings(registerGroupInfo, all));

        if (didUpdateMappings || modified) {
            this.masterDataService.validateRegisterGroup(group);
            group.save();
        }
        return new RegisterGroupInfo(group);
    }

    private HashMap<Long, RegisterType> extractMappings(RegisterGroupInfo registerGroupInfo, boolean all) {
        HashMap<Long, RegisterType> registerMappings = new HashMap<>();
        if (all) {
            List<RegisterType> mappings = masterDataService.findAllRegisterTypes().find();
            for (RegisterType mapping : mappings) {
                registerMappings.put(mapping.getId(), mapping);
            }
        } else {
            for (RegisterTypeInfo mapping : registerGroupInfo.registerTypes) {
                registerMappings.put(mapping.id, resourceHelper.findRegisterTypeByIdOrThrowException(mapping.id));
            }
        }
        return registerMappings;
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

}
