package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class DeviceTypeBuilder extends NamedBuilder<DeviceType, DeviceTypeBuilder> {
    private final DeviceConfigurationService deviceConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;

    private String protocol;
    private List<RegisterType> registerTypes;
    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;

    @Inject
    public DeviceTypeBuilder(DeviceConfigurationService deviceConfigurationService, ProtocolPluggableService protocolPluggableService) {
        super(DeviceTypeBuilder.class);
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
    }

    public DeviceTypeBuilder withProtocol(String protocol){
        this.protocol = protocol;
        return this;
    }

    public DeviceTypeBuilder withRegisterTypes(List<RegisterType> registerTypes){
        this.registerTypes = registerTypes;
        return this;
    }

    public DeviceTypeBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes){
        this.loadProfileTypes = loadProfileTypes;
        return this;
    }

    public DeviceTypeBuilder withLogBookTypes(List<LogBookType> logBookTypes){
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
        List<DeviceProtocolPluggableClass> protocols = protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(this.protocol);
        if (protocols.isEmpty()){
            throw new IllegalStateException("Unable to retrieve the " + this.protocol + " protocol. Please check that license was correctly installed and that indexing process was finished for protocols.");
        }
        DeviceType deviceType = deviceConfigurationService.newDeviceType(getName(), protocols.get(0));
        if (this.registerTypes != null){
            for (RegisterType registerType : registerTypes) {
                deviceType.addRegisterType(registerType);
            }
        }
        if (this.loadProfileTypes != null){
            for (LoadProfileType loadProfileType : loadProfileTypes) {
                deviceType.addLoadProfileType(loadProfileType);
            }
        }
        if (logBookTypes != null) {
            for (LogBookType logBookType : logBookTypes) {
                deviceType.addLogBookType(logBookType);
            }
        }
        deviceType.save();
        return deviceType;
    }
}
