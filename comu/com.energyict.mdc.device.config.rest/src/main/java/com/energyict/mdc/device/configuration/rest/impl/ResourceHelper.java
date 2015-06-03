package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;

public class ResourceHelper {

    private final ExceptionFactory exceptionFactory;
    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory,
                          MasterDataService masterDataService,
                          DeviceConfigurationService deviceConfigurationService,
                          DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.exceptionFactory = exceptionFactory;
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
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

    public DeviceType findAndLockDeviceType(long id, long version) {
        return deviceConfigurationService
                .findAndLockDeviceType(id, version)
                .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
    }

    public DeviceConfiguration findDeviceConfigurationForDeviceTypeOrThrowException(DeviceType deviceType, long deviceConfigurationId) {
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId() == deviceConfigurationId) {
                return deviceConfiguration;
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    public RegisterSpec findRegisterSpec(long registerSpecId) {
        return deviceConfigurationService
                .findRegisterSpec(registerSpecId)
                .orElseThrow(() -> new WebApplicationException("No register spec with id " + registerSpecId, Response.Status.NOT_FOUND));
    }

    public LoadProfileSpec findLoadProfileSpec(long loadProfileSpecId) {
        return deviceConfigurationService
                .findLoadProfileSpec((int) loadProfileSpecId)
                .orElseThrow(() -> new WebApplicationException("No load profile spec with id " + loadProfileSpecId, Response.Status.NOT_FOUND));
    }

    public ChannelSpec findChannelSpec(long channelSpecId) {
        return deviceConfigurationService
                .findChannelSpec(channelSpecId)
                .orElseThrow(() -> new WebApplicationException("No channel spec with id " + channelSpecId, Response.Status.NOT_FOUND));
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

    public SecurityPropertySet findAnySecurityPropertySetByIdOrThrowException(long securityPropertySetId) {
        return deviceConfigurationService.findSecurityPropertySet(securityPropertySetId).orElseThrow(() -> new WebApplicationException("Required security set is missing",
                Response.status(Response.Status.NOT_FOUND).entity("Required security set doesn't exist").build()));
    }

    public ProtocolDialectConfigurationProperties
    findAnyProtocolDialectConfigurationPropertiesByIdOrThrowException(long protocolDialectId) {
        return deviceConfigurationService.getProtocolDialectConfigurationProperties(protocolDialectId)
                .orElseThrow(() -> new WebApplicationException("Required protocol dialect connection properties are missing",
                        Response.status(Response.Status.NOT_FOUND).entity("Required protocol dialect connection properties are missing").build()));
    }

    public Optional<DeviceLifeCycle> findDeviceLifeCycleById(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id);
    }

    public DeviceLifeCycle findDeviceLifeCycleByIdOrThrowException(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE, id));
    }
}