package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 9/15/14.
 */
public class ExecutionLevelResource {
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;

    @Inject
    public ExecutionLevelResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, ExecutionLevelInfoFactory executionLevelInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
    }

    @GET
    public Response getPrivileges(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, @QueryParam("available") Boolean filterAvailable){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);

        Set<DeviceSecurityUserAction> userActions;
        Set<DeviceSecurityUserAction> existingUserActions = securityPropertySet.getUserActions();
        if (filterAvailable!=null && filterAvailable) {
            Set<DeviceSecurityUserAction> allUserActions = EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION3, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4,
                    DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4,
                    DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
            allUserActions.removeAll(existingUserActions);
            userActions=allUserActions;
        } else {
            userActions=existingUserActions;
        }
        List<ExecutionLevelInfo> userActionInfos = executionLevelInfoFactory.from(userActions);
        Map<String, Object> map = new HashMap<>();
        map.put("executionLevels", userActionInfos);
        return Response.ok(map).build();
    }


    @POST
    public Response linkDeviceConfigToPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, List<String> privilegeIds){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        List<String> unknownPrivileges = new ArrayList<>();
        privilegeIds.stream().filter(p->!DeviceSecurityUserAction.forPrivilege(p).isPresent()).forEach(unknownPrivileges::add);
        if (!unknownPrivileges.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, Joiner.on(",").join(unknownPrivileges));
        }

        privilegeIds.stream().map(DeviceSecurityUserAction::forPrivilege).filter(Optional::isPresent).map(Optional::get).forEach(securityPropertySet::addUserAction);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{executionLevelId}")
    public Response unlinkDeviceConfigFromPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, @PathParam("executionLevelId") String privilegeId){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        Optional<DeviceSecurityUserAction> optional = DeviceSecurityUserAction.forPrivilege(privilegeId);
        if (!optional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, privilegeId);
        }
        securityPropertySet.removeUserAction(optional.get());
        return Response.noContent().build();
    }
}
