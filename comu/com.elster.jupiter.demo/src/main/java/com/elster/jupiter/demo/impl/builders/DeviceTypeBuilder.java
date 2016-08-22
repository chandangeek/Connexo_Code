package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.base.Strings;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class DeviceTypeBuilder extends NamedBuilder<DeviceType, DeviceTypeBuilder> {
    private final DeviceConfigurationService deviceConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private String protocol;
    private List<RegisterType> registerTypes;
    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;

    @Inject
    public DeviceTypeBuilder(DeviceConfigurationService deviceConfigurationService, ProtocolPluggableService protocolPluggableService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super(DeviceTypeBuilder.class);
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public DeviceTypeBuilder withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public DeviceTypeBuilder withRegisterTypes(List<RegisterType> registerTypes) {
        this.registerTypes = registerTypes;
        return this;
    }

    public DeviceTypeBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        this.loadProfileTypes = loadProfileTypes;
        return this;
    }

    public DeviceTypeBuilder withLogBookTypes(List<LogBookType> logBookTypes) {
        this.logBookTypes = logBookTypes;
        return this;
    }

    @Override
    public Optional<DeviceType> find() {
        return deviceConfigurationService.findDeviceTypeByName(getName());
    }

    @Override
    public DeviceType create() {
        Log.write(this);
        DeviceType.DeviceTypeBuilder deviceType;
        if (Strings.isNullOrEmpty(protocol)){
            deviceType = deviceConfigurationService.newDataloggerSlaveDeviceTypeBuilder(getName(), deviceLifeCycleConfigurationService
                    .findDefaultDeviceLifeCycle()
                    .get());
        }else{
            List<DeviceProtocolPluggableClass> protocols = protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(this.protocol);
            if (protocols.isEmpty()) {
                throw new IllegalStateException("Unable to retrieve the " + this.protocol + " protocol. Please check that license was correctly installed and that indexing process was finished for protocols.");
            }
            deviceType = deviceConfigurationService.newDeviceTypeBuilder(getName(), protocols.get(0), deviceLifeCycleConfigurationService
                    .findDefaultDeviceLifeCycle()
                    .get());
        }

        if (this.registerTypes != null) {
            deviceType.withRegisterTypes(registerTypes);
        }
        if (this.loadProfileTypes != null) {
            deviceType.withLoadProfileTypes(loadProfileTypes);
        }
        if (this.logBookTypes != null) {
            deviceType.withLogBookTypes(logBookTypes);
        }
        return deviceType.create();
    }
}
