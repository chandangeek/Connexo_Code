/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

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
import java.util.List;
import java.util.stream.Collectors;

@Path("/registergroups")
public class RegisterGroupResource {

    public static final String ALL = "all";
    private final MasterDataService masterDataService;
    private final ResourceHelper resourceHelper;
    private final RegisterGroupInfoFactory registerGroupInfoFactory;

    @Inject
    public RegisterGroupResource(MasterDataService masterDataService, ResourceHelper resourceHelper, RegisterGroupInfoFactory registerGroupInfoFactory) {
        super();
        this.masterDataService = masterDataService;
        this.resourceHelper = resourceHelper;
        this.registerGroupInfoFactory = registerGroupInfoFactory;
    }

    @GET @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public PagedInfoList getRegisterGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<RegisterGroup> allRegisterGroups = this.masterDataService.findAllRegisterGroups().from(queryParameters).find();
        List<RegisterGroupInfo> registerGroupInfos =
                allRegisterGroups
                        .stream()
                        .map(registerGroup -> registerGroupInfoFactory.asInfo(registerGroup.getId(), registerGroup.getName(), registerGroup.getVersion()))
                        .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("registerGroups", registerGroupInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public RegisterGroupInfo getRegisterGroup(@PathParam("id") long id) {
        return registerGroupInfoFactory.asInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
    }

    @GET @Transactional
    @Path("/{id}/registertypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public Response getRegisterTypesOfRegisterGroup(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        RegisterGroupInfo registerGroupInfo = registerGroupInfoFactory.asInfo(resourceHelper.findRegisterGroupByIdOrThrowException(id));
        return Response.ok(PagedInfoList.fromCompleteList("registerTypes", registerGroupInfo.registerTypes, queryParameters)).build();
    }

    @DELETE @Transactional
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteRegisterGroup(@PathParam("id") long id, RegisterGroupInfo info) {
        info.id = id;
        RegisterGroup group = resourceHelper.lockRegisterGroupOrThrowException(info);
        group.delete();
        return Response.ok().build();
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public RegisterGroupInfo createRegisterGroup(RegisterGroupInfo registerGroupInfo, @Context UriInfo uriInfo) {
        RegisterGroup newGroup = this.masterDataService.newRegisterGroup(registerGroupInfo.name);

        return updateRegisterTypeInGroup(newGroup, registerGroupInfo, getBoolean(uriInfo, ALL));

    }

    @PUT @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public RegisterGroupInfo updateRegisterGroup(@PathParam("id") long id, RegisterGroupInfo info, @Context UriInfo uriInfo) {
        info.id = id;
        RegisterGroup group = resourceHelper.lockRegisterGroupOrThrowException(info);
        if (!group.getName().equals(info.name)) {
            group.setName(info.name);
        }
        return updateRegisterTypeInGroup(group, info, getBoolean(uriInfo, ALL));
    }

    private RegisterGroupInfo updateRegisterTypeInGroup(RegisterGroup group, RegisterGroupInfo registerGroupInfo, boolean all) {
        group.updateRegisterTypes(extractMappings(registerGroupInfo, all));
        group.save();
        return registerGroupInfoFactory.asInfo(group);
    }

    private List<RegisterType> extractMappings(RegisterGroupInfo registerGroupInfo, boolean all) {
        if (all) {
            return this.masterDataService.findAllRegisterTypes().find();
        }
        else {
            return registerGroupInfo.registerTypes
                    .stream()
                    .map(each -> resourceHelper.findRegisterTypeByIdOrThrowException(each.id))
                    .collect(Collectors.toList());
        }
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

}
