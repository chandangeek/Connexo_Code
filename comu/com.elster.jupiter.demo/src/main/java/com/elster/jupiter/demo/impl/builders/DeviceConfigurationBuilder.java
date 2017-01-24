package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.SecurityPropertySetTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DeviceConfigurationBuilder extends NamedBuilder<DeviceConfiguration, DeviceConfigurationBuilder> {
    private DeviceType deviceType;
    private GatewayType gatewayType;
    private boolean canActAsGateway = false;
    private boolean directlyAddressable = true;
    private boolean dataLoggerEnabled = false;
    private List<RegisterType> registerTypes;
    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;
    private List<ComTask> comTasks;
    private List<SecurityPropertySetBuilder> securityPropertySetBuilders;
    private BigDecimal overflowValue = new BigDecimal(9999999999L);

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

    public DeviceConfigurationBuilder withCanActAsGateway(boolean canActAsGateway) {
        this.canActAsGateway = canActAsGateway;
        return this;
    }

    public DeviceConfigurationBuilder withDirectlyAddressable(boolean directlyAddressable) {
        this.directlyAddressable = directlyAddressable;
        return this;
    }

    public DeviceConfigurationBuilder withDataLoggerEnabled(boolean dataLoggerEnabled) {
        this.dataLoggerEnabled = dataLoggerEnabled;
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
        configBuilder.dataloggerEnabled(dataLoggerEnabled);
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
                        .overflowValue(overflowValue)
                        .numberOfFractionDigits(0);
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

    private void addComTasks(DeviceConfiguration configuration) {
        if (comTasks != null) {
            for (ComTask comTask : comTasks) {
                addComTask(configuration, comTask);
            }
        }
    }

    public static void addComTask(DeviceConfiguration deviceConfiguration, ComTask comTask) {
        if (comTask != null && deviceConfiguration != null) {
            List<SecurityPropertySet> securityPropertySets = deviceConfiguration.getSecurityPropertySets();
            if (securityPropertySets.isEmpty()) {
                throw new UnableToCreate("Please specify at least one security set");
            }
            SecurityPropertySet securityPropertySet = securityPropertySets
                    .stream()
                    .filter(sps -> SecurityPropertySetTpl.NO_SECURITY.getName().equals(sps.getName()))
                    .findFirst()
                    .orElse(securityPropertySets.get(0));
            deviceConfiguration.enableComTask(comTask, securityPropertySet, getProtocolDialectConfigurationProperties(deviceConfiguration))
                    .setIgnoreNextExecutionSpecsForInbound(false)
                    .setPriority(100).add().save();
        }
    }

    private static ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(DeviceConfiguration configuration) {
        Optional<ProtocolDialectConfigurationProperties> tcpDialect = findTheTCPDialect(configuration);
        return tcpDialect.orElse(configuration.getProtocolDialectConfigurationPropertiesList().get(0));
    }

    private static Optional<ProtocolDialectConfigurationProperties> findTheTCPDialect(DeviceConfiguration configuration) {
        return configuration.getProtocolDialectConfigurationPropertiesList()
                .stream()
                .filter(protocolDialectConfigurationProperties ->
                        protocolDialectConfigurationProperties.getDeviceProtocolDialectName().toLowerCase().contains("tcp"))
                .findFirst();
    }
}
