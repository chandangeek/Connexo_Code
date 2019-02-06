/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.ComTaskCfgTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.tasks.ComTask;

import com.energyict.obis.ObisCode;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeviceConfigurationBuilder extends NamedBuilder<DeviceConfiguration, DeviceConfigurationBuilder> {
    private DeviceType deviceType;
    private GatewayType gatewayType;
    private boolean canActAsGateway = false;
    private boolean directlyAddressable = true;
    private boolean dataLoggerEnabled = false;
    private boolean multiElementEnabled = false;
    private List<RegisterType> registerTypes;
    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;
    private Map<ComTask, ComTaskCfgTpl> comTasks;
    private Map<String, Object> generalAttributes;
    private List<SecurityPropertySetBuilder> securityPropertySetBuilders;
    private BigDecimal overflowValue = new BigDecimal(9999999999L);
    private boolean validateOnStore = true;

    @Inject
    public DeviceConfigurationBuilder() {
        super(DeviceConfigurationBuilder.class);
    }

    public DeviceConfigurationBuilder withDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public DeviceConfigurationBuilder withValidateOnStore(boolean validateOnStore) {
        this.validateOnStore = validateOnStore;
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

    public DeviceConfigurationBuilder withGeneralAttributes(ImmutableMap<String, Object> generalAttributes) {
        this.generalAttributes = generalAttributes;
        return this;
    }

    public DeviceConfigurationBuilder withDataLoggerEnabled(boolean dataLoggerEnabled) {
        this.dataLoggerEnabled = dataLoggerEnabled;
        return this;
    }

    public DeviceConfigurationBuilder withMultiElementEnabled(boolean multiElementEnabled) {
        this.multiElementEnabled = multiElementEnabled;
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

    public DeviceConfigurationBuilder withComTasks(Map<ComTask, ComTaskCfgTpl> comTasks) {
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
        configBuilder.multiElementEnabled(multiElementEnabled);
        configBuilder.validateOnStore(validateOnStore);
        addRegisters(configBuilder);
        addLoadProfiles(configBuilder);
        addLogBooks(configBuilder);
        DeviceConfiguration configuration = configBuilder.add();
        addSecurityPropertySet(configuration);
        applyPostBuilders(configuration);
        addComTasks(configuration);
        addGenetalAttributes(configuration);
        configuration.save();
        return configuration;
    }

    private void addRegisters(DeviceType.DeviceConfigurationBuilder builder) {
        if (this.registerTypes != null) {
            for (RegisterType registerType : registerTypes) {
                builder.newNumericalRegisterSpec(registerType)
                        .overruledObisCode(ObisCode.fromString(RegisterTypeTpl.findByMRID(registerType.getReadingType().getMRID()).getObisCode()))
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
            for (ComTask comTask : comTasks.keySet()) {
                addComTask(configuration, comTask, comTasks.get(comTask));
            }
        }
    }

    public static void addComTask(DeviceConfiguration deviceConfiguration, ComTask comTask, ComTaskCfgTpl comTaskCfg) {
        if (comTask != null && deviceConfiguration != null) {
            List<SecurityPropertySet> securityPropertySets = deviceConfiguration.getSecurityPropertySets();
            if (securityPropertySets.isEmpty()) {
                throw new UnableToCreate("Please specify at least one security set");
            }
            Optional<SecurityPropertySet> securityPropertySet = securityPropertySets.stream().filter(o -> o.getName().equals(comTaskCfg.getSecurityPropertySetTpl().getName())).findFirst();
            if (securityPropertySet.isPresent()) {
                deviceConfiguration.enableComTask(comTask, securityPropertySet.get())
                        .setIgnoreNextExecutionSpecsForInbound(comTaskCfg.getIgnoreNextExecutionSpecs())
                        .setPartialConnectionTask(resolveConnectionTask(deviceConfiguration, comTaskCfg.getConnectionTask()))
                        .useDefaultConnectionTask(true)
                        .setPriority(100).add().save();
            }
        }
    }

    public void addGenetalAttributes(DeviceConfiguration deviceConfiguration) {
        DeviceProtocolConfigurationProperties deviceProtocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        if (generalAttributes != null) {
            generalAttributes.forEach((name, value) -> {
                deviceProtocolProperties.setProperty(name, resolveAttributeValue(deviceConfiguration, name, value));
            });
        }
    }

    private Object resolveAttributeValue(DeviceConfiguration deviceConfiguration, String name, Object value) {
        if ((name.compareToIgnoreCase("PSKEncryptionKey") == 0) || (name.compareToIgnoreCase("PSK") == 0) ||
                (name.compareToIgnoreCase("IncrementFrameCounterForReplyToHLS") == 0)) {
            return deviceConfiguration.getDeviceType().getSecurityAccessorTypes().stream()
                    .filter(securityAccessorType -> securityAccessorType.getName().compareToIgnoreCase(value.toString()) == 0)
                    .findFirst()
                    .orElse(null);
        }
        return value;
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

    private static PartialConnectionTask resolveConnectionTask(DeviceConfiguration deviceConfiguration, String connectionTask) {
        return deviceConfiguration.getPartialConnectionTasks()
                .stream()
                .filter(pct -> pct.getName().equals(connectionTask))
                .findFirst().orElse(null);
    }
}
