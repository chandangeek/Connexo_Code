package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.*;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ResourceHelper(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService) {
        super();
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public RegisterGroup findRegisterGroupByIdOrThrowException(long id) {
        Optional<RegisterGroup> registerGroup = masterDataService.findRegisterGroup(id);
        if (!registerGroup.isPresent()) {
            throw new WebApplicationException("No register group with id " + id, Response.Status.NOT_FOUND);
        }
        return registerGroup.get();
    }

    public RegisterMapping findRegisterMappingByIdOrThrowException(long id) {
        Optional<RegisterMapping> registerMapping = masterDataService.findRegisterMapping(id);
        if (!registerMapping.isPresent()) {
            throw new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND);
        }
        return registerMapping.get();
     }

    public DeviceType findDeviceTypeByIdOrThrowException(long id) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND);
        }
        return deviceType;
     }

    public DeviceConfiguration findDeviceConfigurationForDeviceTypeOrThrowException(DeviceType deviceType, long deviceConfigurationId) {
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return deviceConfiguration;
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    public RegisterSpec findRegisterSpec(long registerSpecId) {
        RegisterSpec registerSpec = deviceConfigurationService.findRegisterSpec(registerSpecId);
        if (registerSpec == null) {
            throw new WebApplicationException("No register spec with id " + registerSpecId, Response.Status.NOT_FOUND);
        }
        return registerSpec;
    }

    public LoadProfileSpec findLoadProfileSpec(long loadProfileSpecId) {
        LoadProfileSpec loadProfileSpec = deviceConfigurationService.findLoadProfileSpec((int) loadProfileSpecId);
        if (loadProfileSpec == null) {
            throw new WebApplicationException("No load profile spec with id " + loadProfileSpecId, Response.Status.NOT_FOUND);
        }
        return loadProfileSpec;
    }

    public ChannelSpec findChannelSpec(long channelSpecId) {
        ChannelSpec channelSpec = deviceConfigurationService.findChannelSpec(channelSpecId);
        if (channelSpec == null) {
            throw new WebApplicationException("No channel spec with id " + channelSpecId, Response.Status.NOT_FOUND);
        }
        return channelSpec;
    }
}
