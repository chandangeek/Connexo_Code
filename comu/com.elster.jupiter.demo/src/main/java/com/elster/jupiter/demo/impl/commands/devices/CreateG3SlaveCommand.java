/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.SecurityPropertySetTpl;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;
import java.util.function.Consumer;

public class CreateG3SlaveCommand {

    public enum SlaveDeviceConfiguration {
        AS3000 {
            @Override
            MeterConfig getMeterConfig() {
                return new MeterConfig().setProperty("DeviceTypeName", "Elster AS3000 [AM540]")
                        .setProperty("name", "Demo board AS3000")
                        .setProperty("propertyID", "E0023000520685414")
                        .setProperty("serialNumber", "E0023000520685414")
                        .setProperty("callHomeId", "02237EFFFEFD835B")
                        .setProperty("masterKey", "00112233445566778899AABBCCDDEEFF")
                        .setProperty("AK", "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF")
                        .setProperty("EK", "000102030405060708090A0B0C0D0E0F")
                        .setProperty("PSK", "00112233445566778899AABBCCDDEEFF")
                        .setProperty("HLSsecretHEX", "31323334353637383930313233343536")
                        .setProperty("HLSsecretASCII", "1234567890123456")
                        .setProperty("TimeZone", TimeZone.getTimeZone("Europe/Brussels"));
            }
        },
        AS220 {
            @Override
            MeterConfig getMeterConfig() {
                return new MeterConfig().setProperty("DeviceTypeName", "Elster AS220 [AM540]")
                        .setProperty("name", "Demo board AS220")
                        .setProperty("propertyID", "123457S")
                        .setProperty("serialNumber", "123457S")
                        .setProperty("callHomeId", "02237EFFFEFD82F4")
                        .setProperty("masterKey", "00112233445566778899AABBCCDDEEFF")
                        .setProperty("AK", "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF")
                        .setProperty("EK", "000102030405060708090A0B0C0D0E0F")
                        .setProperty("PSK", "92DA010836AA91222BCBEA49713DD9C1")
                        .setProperty("HLSsecretHEX", "31323334353637383930313233343536")
                        .setProperty("HLSsecretASCII", "1234567890123456")
                        .setProperty("TimeZone", TimeZone.getTimeZone("Europe/Brussels"));
            }
        };

        abstract MeterConfig getMeterConfig();

    }

    private final SecurityManagementService securityManagementService;
    private final Provider<ActivateDevicesCommand> lifecyclePostBuilder;

    private String name;
    private MeterConfig meterConfig;
    private DeviceTypeTpl deviceTypeTemplate;


    @Inject
    public CreateG3SlaveCommand(SecurityManagementService securityManagementService, Provider<ActivateDevicesCommand> lifecyclePostBuilder) {
        this.securityManagementService = securityManagementService;
        this.lifecyclePostBuilder = lifecyclePostBuilder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfig(String meterConfigName){
        this.meterConfig = Arrays.stream(SlaveDeviceConfiguration.values())
                .filter(x-> x.name().equals(meterConfigName)).findFirst().orElseThrow(()-> new IllegalArgumentException("No configuration known for '" + meterConfigName + "'"))
                .getMeterConfig();
        this.deviceTypeTemplate = DeviceTypeTpl.fromName(meterConfigName);
    }

    public void run() {
        DeviceConfiguration deviceConfiguration = getConfiguration();
        meterConfig.setSecurityManagementService(securityManagementService);
        meterConfig.setDeviceConfiguration(deviceConfiguration);
        meterConfig.setSecurityPropertySet(deviceConfiguration.getSecurityPropertySets().stream().filter(s -> SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5.getName().equals(s.getName())).findFirst().get());

        if (name != null){
            meterConfig.setProperty("name", name);
        }
        lifecyclePostBuilder.get()
                .setDevices(Collections.singletonList(deviceFrom(meterConfig)))
                .run();
    }

    private Device deviceFrom(MeterConfig config) {
        return config.getDevice();
    }

    private DeviceConfiguration getConfiguration() {
        if (deviceTypeTemplate == null){
            throw new IllegalStateException("DeviceTypeTpl not set");
        }
        DeviceType deviceType = Builders.from(deviceTypeTemplate).get();
        DeviceConfiguration config = createDefaultConfiguration(deviceType);
        if (!config.isActive()) {
            config.activate();
        }
        return config;
    }

    private DeviceConfiguration createDefaultConfiguration(DeviceType deviceType) {
        return Builders.from(DeviceConfigurationTpl.AM540).withDeviceType(deviceType)
                .withDirectlyAddressable(false)
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
    }

    private static class MeterConfig {

        private TypedProperties props = TypedProperties.empty();
        private DeviceConfiguration deviceConfiguration;
        private SecurityPropertySet securityPropertySet;
        private SecurityManagementService securityManagementService;

        MeterConfig() {
        }

        MeterConfig setProperty(String propertyName, Object value) {
            props.setProperty(propertyName, value);
            return this;
        }

        Object getProperty(String propertyName){
            return props.getProperty(propertyName);
        }

        public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
            this.deviceConfiguration = deviceConfiguration;
        }

        public void setSecurityPropertySet(SecurityPropertySet securityPropertySet) {
            this.securityPropertySet = securityPropertySet;
        }

        public SecurityPropertySet getSecurityPropertySet() {
            return securityPropertySet;
        }

        public SecurityManagementService getSecurityManagementService() {
            return securityManagementService;
        }

        public void setSecurityManagementService(SecurityManagementService securityManagementService) {
            this.securityManagementService = securityManagementService;
        }

        Device getDevice() {
            return Builders.from(DeviceBuilder.class)
                    .withName((String) props.getProperty("name"))
                    .withDeviceConfiguration(deviceConfiguration)
                    .withSerialNumber((String) props.getProperty("serialNumber"))
                    .withPostBuilder(new SecurityPropertyPostBuilder(this, getSecurityManagementService()))
                    .withPostBuilder(new ProtocolPropertyPostBuilder(this))
                    .get();
        }
    }

    private static class SecurityPropertyPostBuilder implements Consumer<Device>{
        MeterConfig meterConfig;
        SecurityManagementService securityManagementService;

        SecurityPropertyPostBuilder(MeterConfig meterConfig, SecurityManagementService securityManagementService){
            this.meterConfig = meterConfig;
            this.securityManagementService = securityManagementService;
        }

        @Override
        public void accept(Device device) {
            SecurityPropertySet securityPropertySet = meterConfig.getSecurityPropertySet();
            securityPropertySet
                    .getPropertySpecs()
                    .stream()
                    .filter(ps -> "Password".equals(ps.getName()))
                    .findFirst()
                    .ifPresent(ps -> getKeyAccessorValuePersister().persistKeyAccessorValue(device, "Password", "1234567890123456"));
        }

        private KeyAccessorValuePersister getKeyAccessorValuePersister() {
               return new KeyAccessorValuePersister(securityManagementService);
        }
    }

    private static class ProtocolPropertyPostBuilder implements Consumer<Device>{
        MeterConfig meterConfig;

        ProtocolPropertyPostBuilder(MeterConfig meterConfig){
            this.meterConfig = meterConfig;
        }

        @Override
        public void accept(Device device) {
            device.setProtocolProperty("callHomeId", meterConfig.getProperty("callHomeId"));
            device.setProtocolProperty("TimeZone", meterConfig.getProperty("TimeZone"));
            device.save();
        }
    }

}
