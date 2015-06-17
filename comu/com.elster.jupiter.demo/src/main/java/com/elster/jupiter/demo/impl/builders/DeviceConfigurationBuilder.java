package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DeviceConfigurationBuilder extends NamedBuilder<DeviceConfiguration, DeviceConfigurationBuilder> {
    private DeviceType deviceType;
    private GatewayType gatewayType;
    private boolean directlyAddressable = true;
    private List<RegisterType> registerTypes;
    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;
    private List<ComTask> comTasks;
    private List<SecurityPropertySetBuilder> securityPropertySetBuilders;

    private List<Consumer<DeviceConfiguration>> postBuilders;

    @Inject
    public DeviceConfigurationBuilder() {
        super(DeviceConfigurationBuilder.class);
    }

    public DeviceConfigurationBuilder withDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public DeviceConfigurationBuilder withGatewayType(GatewayType gatewayType) {
        this.gatewayType = gatewayType;
        return this;
    }


    public DeviceConfigurationBuilder withDirectlyAddressable(boolean directlyAddressable){
        this.directlyAddressable = directlyAddressable;
        return this;
    }

    public DeviceConfigurationBuilder withRegisterTypes(List<RegisterType> registerTypes) {
        this.registerTypes = registerTypes;
        return this;
    }

    public DeviceConfigurationBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        this.loadProfileTypes = loadProfileTypes;
        return this;
    }

    public DeviceConfigurationBuilder withLogBookTypes(List<LogBookType> logBookTypes) {
        this.logBookTypes = logBookTypes;
        return this;
    }

    public DeviceConfigurationBuilder withComTasks(List<ComTask> comTasks) {
        this.comTasks = comTasks;
        return this;
    }

    public DeviceConfigurationBuilder withPostBuilder(Consumer<DeviceConfiguration> postBuilder) {
        if (this.postBuilders == null) {
            this.postBuilders = new ArrayList<>();
        }
        this.postBuilders.add(postBuilder);
        return this;
    }

    public DeviceConfigurationBuilder withSecurityPropertySetBuilders(List<SecurityPropertySetBuilder> securityPropertySetBuilders) {
        this.securityPropertySetBuilders = securityPropertySetBuilders;
        return this;
    }

    @Override
    public Optional<DeviceConfiguration> find() {
        if (this.deviceType == null) {
            throw new UnableToCreate("You must specify a device type");
        }
        return this.deviceType.getConfigurations().stream().filter(conf -> conf.getName().equals(getName())).findFirst();
    }

    @Override
    public DeviceConfiguration create() {
        Log.write(this);
        DeviceType.DeviceConfigurationBuilder configBuilder = this.deviceType.newConfiguration(getName());
        configBuilder.description(getName() + " configuration for device type: " + this.deviceType.getName());
        configBuilder.gatewayType(this.gatewayType);
        configBuilder.isDirectlyAddressable(directlyAddressable);
        addRegisters(configBuilder);
        addLoadProfiles(configBuilder);
        addLogBooks(configBuilder);
        DeviceConfiguration configuration = configBuilder.add();
        addSecurityPropertySet(configuration);
        applyPostBuilders(configuration);
        addComTasks(configuration);
        configuration.save();
        return configuration;
    }

    private void addRegisters(DeviceType.DeviceConfigurationBuilder builder) {
        if (this.registerTypes != null) {
            for (RegisterType registerType : registerTypes) {
                builder.newNumericalRegisterSpec(registerType)
                        .setOverflowValue(new BigDecimal(99999999))
                        .setNumberOfDigits(8)
                        .setNumberOfFractionDigits(0);
            }
        }
    }

    private void addLoadProfiles(DeviceType.DeviceConfigurationBuilder builder) {
        if (this.loadProfileTypes != null) {
            for (LoadProfileType loadProfileType : loadProfileTypes) {
                builder.newLoadProfileSpec(loadProfileType);
            }
        }
    }

    private void addLogBooks(DeviceType.DeviceConfigurationBuilder builder) {
        if (logBookTypes != null) {
            for (LogBookType logBookType : logBookTypes) {
                builder.newLogBookSpec(logBookType);
            }
        }
    }

    private void addSecurityPropertySet(DeviceConfiguration configuration) {
        if (securityPropertySetBuilders != null) {
            for (SecurityPropertySetBuilder securityPropertySetBuilder : securityPropertySetBuilders) {
                securityPropertySetBuilder.withDeviceConfiguration(configuration).get();
            }
        }
    }

    private void applyPostBuilders(DeviceConfiguration configuration) {
        if (postBuilders != null) {
            for (Consumer<DeviceConfiguration> postBuilder : postBuilders) {
                postBuilder.accept(configuration);
            }
        }
    }

    private void addComTasks(DeviceConfiguration configuration) {
        if (comTasks != null) {
            if (configuration.getSecurityPropertySets().isEmpty()) {
                throw new UnableToCreate("Please specify at least one security set");
            }
            for (ComTask comTask : comTasks) {
                configuration.enableComTask(comTask, configuration.getSecurityPropertySets().get(0), getProtocolDialectConfigurationProperties(configuration))
                        .setIgnoreNextExecutionSpecsForInbound(true)
                        .setPriority(100).add().save();
            }
        }
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(DeviceConfiguration configuration) {
        Optional<ProtocolDialectConfigurationProperties> tcpDialect = findTheTCPDialect(configuration);
        return tcpDialect.orElse(configuration.getProtocolDialectConfigurationPropertiesList().get(0));
    }

    private Optional<ProtocolDialectConfigurationProperties> findTheTCPDialect(DeviceConfiguration configuration) {
        return configuration.getProtocolDialectConfigurationPropertiesList()
                    .stream()
                    .filter(protocolDialectConfigurationProperties ->
                            protocolDialectConfigurationProperties.getDeviceProtocolDialectName().toLowerCase().contains("tcp"))
                    .findFirst();
    }
}
