package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 9/15/14.
 */
public class ExecutionLevelResource {
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ExecutionLevelResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @POST
    public Response linkDeviceConfigToPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, List<String> privilegeIds){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        List<String> unknownPrivileges = new ArrayList<>();
        privilegeIds.stream().filter(p->!DeviceSecurityUserAction.forName(p).isPresent()).forEach(unknownPrivileges::add);
        if (!unknownPrivileges.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, Joiner.on(",").join(unknownPrivileges));
        }

        privilegeIds.stream().map(DeviceSecurityUserAction::forName).filter(Optional::isPresent).map(Optional::get).forEach(securityPropertySet::addUserAction);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{executionLevelId}")
    public Response unlinkDeviceConfigFromPrivilege(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, @PathParam("executionLevelId") String privilegeId){
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        Optional<DeviceSecurityUserAction> optional = DeviceSecurityUserAction.forName(privilegeId);
        if (!optional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_PRIVILEGE_ID, privilegeId);
        }
        securityPropertySet.removeUserAction(optional.get());
        return Response.noContent().build();
    }
}
