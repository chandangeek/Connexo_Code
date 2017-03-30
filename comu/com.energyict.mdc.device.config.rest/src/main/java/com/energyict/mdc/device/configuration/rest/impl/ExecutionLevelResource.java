/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;

import com.google.common.base.Joiner;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.util.streams.Functions.asStream;

/**
 * Created by bvn on 9/15/14.
 */
public class ExecutionLevelResource {
    private final ResourceHelper resourceHelper;
    private final UserService userService;
    private final ExceptionFactory exceptionFactory;
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ExecutionLevelResource(ResourceHelper resourceHelper, UserService userService, ExceptionFactory exceptionFactory, ExecutionLevelInfoFactory executionLevelInfoFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.userService = userService;
        this.exceptionFactory = exceptionFactory;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getPrivileges(@PathParam("securityPropertySetId") long securityPropertySetId, @QueryParam("available") Boolean filterAvailable) {
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(securityPropertySetId);

        Set<DeviceSecurityUserAction> userActions;
        Set<DeviceSecurityUserAction> existingUserActions = securityPropertySet.getUserActions();
        if (filterAvailable != null && filterAvailable) {
            Set<DeviceSecurityUserAction> allUserActions =
                    EnumSet.of(
                            DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4,
                            DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
            allUserActions.removeAll(existingUserActions);
            userActions = allUserActions;
        } else {
            userActions = existingUserActions;
        }
        List<ExecutionLevelInfo> userActionInfos = executionLevelInfoFactory.from(userActions, this.userService.getGroups(), securityPropertySet);
        Map<String, Object> map = new HashMap<>();
        map.put("executionLevels", userActionInfos);
        return Response.ok(map).build();
    }


    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response linkDeviceConfigToPrivilege(@PathParam("securityPropertySetId") long securityPropertySetId, List<String> privilegeIds) {
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(securityPropertySetId);
        List<String> unknownPrivileges = new ArrayList<>();
        privilegeIds.stream().filter(p -> !DeviceSecurityUserAction.forPrivilege(p).isPresent()).forEach(unknownPrivileges::add);
        if (!unknownPrivileges.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, Joiner.on(",").join(unknownPrivileges));
        }

        privilegeIds.stream().map(DeviceSecurityUserAction::forPrivilege).flatMap(asStream()).forEach(securityPropertySet::addUserAction);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE @Transactional
    @Path("/{executionLevelId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response unlinkDeviceConfigFromPrivilege(@PathParam("securityPropertySetId") long securityPropertySetId, @PathParam("executionLevelId") String privilegeId, ExecutionLevelInfo info) {
        SecurityPropertySet securityPropertySet = resourceHelper.getLockedSecurityPropertySet(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualParent(() -> resourceHelper.getCurrentSecurityPropertySetVersion(info.parent.id), info.parent.id)
                        .supplier());
        DeviceSecurityUserAction userAction = DeviceSecurityUserAction.forPrivilege(privilegeId).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.UNKNOWN_PRIVILEGE_ID, privilegeId));
        securityPropertySet.removeUserAction(userAction);
        return Response.noContent().build();
    }
}
