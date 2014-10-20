package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.function.Function;

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
        return masterDataService
                .findRegisterGroup(id)
                .orElseThrow(() -> new WebApplicationException("No register group with id " + id, Response.Status.NOT_FOUND));
    }

    public com.energyict.mdc.masterdata.RegisterType findRegisterTypeByIdOrThrowException(long id) {
        return masterDataService
                .findRegisterType(id)
                .orElseThrow(() -> new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND));
     }

    public ChannelType findChannelTypeByIdOrThrowException(long id) {
        return masterDataService
                .findChannelTypeById(id)
                .orElseThrow(() -> new WebApplicationException("No channel type with id " + id, Response.Status.NOT_FOUND));
     }



    public DeviceType findDeviceTypeByIdOrThrowException(long id) {
        return deviceConfigurationService
                .findDeviceType(id)
                .orElseThrow(() -> new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND));
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

    public SecurityPropertySet findSecurityPropertySetByIdOrThrowException(DeviceConfiguration deviceConfiguration, long securityPropertySetId) {
        return deviceConfiguration
                .getSecurityPropertySets().stream().filter(sps -> sps.getId() == securityPropertySetId)
                .findAny()
                    .map(Function.identity())
                    .orElseThrow(() -> new WebApplicationException("No such security property set for the device configuration", Response.status(Response.Status.NOT_FOUND)
                            .entity("No such security property set for the device configuration")
                            .build()));
    }

}