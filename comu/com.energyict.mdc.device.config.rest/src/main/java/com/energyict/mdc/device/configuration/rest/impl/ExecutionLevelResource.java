package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;

import com.elster.jupiter.users.UserService;
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

    @Inject
    public ExecutionLevelResource(ResourceHelper resourceHelper, UserService userService, ExceptionFactory exceptionFactory, ExecutionLevelInfoFactory executionLevelInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.userService = userService;
        this.exceptionFactory = exceptionFactory;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getPrivileges(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, @QueryParam("available") Boolean filterAvailable){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);

        Set<DeviceSecurityUserAction> userActions;
        Set<DeviceSecurityUserAction> existingUserActions = securityPropertySet.getUserActions();
        if (filterAvailable!=null && filterAvailable) {
            Set<DeviceSecurityUserAction> allUserActions =
                EnumSet.of(
                    DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4,
                    DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
            allUserActions.removeAll(existingUserActions);
            userActions=allUserActions;
        } else {
            userActions=existingUserActions;
        }
        List<ExecutionLevelInfo> userActionInfos = executionLevelInfoFactory.from(userActions, this.userService.getGroups());
        Map<String, Object> map = new HashMap<>();
        map.put("executionLevels", userActionInfos);
        return Response.ok(map).build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response linkDeviceConfigToPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, List<String> privilegeIds){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        List<String> unknownPrivileges = new ArrayList<>();
        privilegeIds.stream().filter(p->!DeviceSecurityUserAction.forPrivilege(p).isPresent()).forEach(unknownPrivileges::add);
        if (!unknownPrivileges.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, Joiner.on(",").join(unknownPrivileges));
        }

        privilegeIds.stream().map(DeviceSecurityUserAction::forPrivilege).flatMap(asStream()).forEach(securityPropertySet::addUserAction);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{executionLevelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response unlinkDeviceConfigFromPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, @PathParam("executionLevelId") String privilegeId){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        DeviceSecurityUserAction userAction = DeviceSecurityUserAction.forPrivilege(privilegeId).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.UNKNOWN_PRIVILEGE_ID, privilegeId));
        securityPropertySet.removeUserAction(userAction);
        return Response.noContent().build();
    }
}
