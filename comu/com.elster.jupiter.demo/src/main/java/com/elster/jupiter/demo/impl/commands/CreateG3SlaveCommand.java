package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.*;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Create 2 Slave devices for a G3 gatewat
 * <p>
 * Meter AS3000 E0023000520685414
 * PropertyID: E0023000520685414
 * SerialNumber (meter) 05206854
 * Master key: 00112233445566778899AABBCCDDEEFF
 * MAC: 02237EFFFEFD835B     'MAC_address' and value '02237EFFFEFD82F4''
 * AK: D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF
 * EK: 000102030405060708090A0B0C0D0E0F
 * PSK: 00112233445566778899AABBCCDDEEFF
 * HLSsecretHEX: 31323334353637383930313233343536
 * HLSsecretASCII: 1234567890123456
 * <p>
 * Meter AS220 123457S
 * PropertyID: 123457S
 * SerialNumber (meter) 35075302
 * Master key: 00112233445566778899AABBCCDDEEFF
 * MAC: 02237EFFFEFD82F4
 * AK: D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF
 * EK: 000102030405060708090A0B0C0D0E0F
 * PSK: 92DA010836AA91222BCBEA49713DD9C1
 * HLSsecretHEX: 31323334353637383930313233343536
 * HLSsecretASCII: 1234567890123456
 * <p>
 * Copyrights EnergyICT
 * Date: 22/04/2015
 * Time: 9:30
 */
public class CreateG3SlaveCommand {

    private final static String SECURITY_SET_NAME = "High level MD5 authentication - No encryption";
    private final MeterConfig meterConfig1 = new MeterConfig().setProperty("MRID", "E0023000520685414")
            .setProperty("propertyID", "E0023000520685414")
            .setProperty("serialNumber", "05206854")
            .setProperty("MAC_address", "02237EFFFEFD835B")
            .setProperty("masterKey", "00112233445566778899AABBCCDDEEFF")
            .setProperty("AK", "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF")
            .setProperty("EK", "000102030405060708090A0B0C0D0E0F")
            .setProperty("PSK", "00112233445566778899AABBCCDDEEFF")
            .setProperty("HLSsecretHEX", "31323334353637383930313233343536")
            .setProperty("HLSsecretASCII", "1234567890123456")
            .setProperty("ClientMacAddress", "1");

    private final MeterConfig meterConfig2 = new MeterConfig().setProperty("MRID", "123457S")
            .setProperty("propertyID", "123457S")
            .setProperty("serialNumber", "35075302")
            .setProperty("MAC_address", "02237EFFFEFD82F4")
            .setProperty("masterKey", "00112233445566778899AABBCCDDEEFF")
            .setProperty("AK", "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF")
            .setProperty("EK", "000102030405060708090A0B0C0D0E0F")
            .setProperty("PSK", "92DA010836AA91222BCBEA49713DD9C1")
            .setProperty("HLSsecretHEX", "31323334353637383930313233343536")
            .setProperty("HLSsecretASCII", "1234567890123456")
            .setProperty("ClientMacAddress", 1);


    private DeviceConfiguration deviceConfiguration;
    private SecurityPropertySet securityPropertySet;
    @Inject
    public CreateG3SlaveCommand() {}

    public void run() {
        deviceConfiguration = getConfiguration();
        securityPropertySet = deviceConfiguration.getSecurityPropertySets().stream().filter(s -> SECURITY_SET_NAME.equals(s.getName())).findFirst().get();

        deviceFrom(meterConfig1);
        deviceFrom(meterConfig2);
    }

    private Device deviceFrom(MeterConfig config) {
        return config.getDevice();
    }

    private DeviceConfiguration getConfiguration() {
        DeviceType deviceType = Builders.from(DeviceTypeTpl.AM540).get();
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
                .withPostBuilder(new SecurityPropertySetPostBuilder())
                .get();
    }

    private class MeterConfig {

        TypedProperties props = TypedProperties.empty();

        MeterConfig() {
        }

        MeterConfig setProperty(String propertyName, Object value) {
            props.setProperty(propertyName, value);
            return this;
        }

        Object getProperty(String propertyName){
            return props.getProperty(propertyName);
        }

        TypedProperties getSecuritySetProperties(){
            TypedProperties securitySetProperties = TypedProperties.empty();
            securitySetProperties.setProperty("ClientMacAddress", props.getProperty("ClientMacAddress"));
            securitySetProperties.setProperty("Password", new Password((String) props.getProperty("HLSsecretASCII")));
            return securitySetProperties;
        }

        Device getDevice() {
            return Builders.from(DeviceBuilder.class)
                    .withMrid((String) props.getProperty("MRID"))
                    .withDeviceConfiguration(deviceConfiguration)
                    .withSerialNumber((String) props.getProperty("serialNumber"))
                    .withPostBuilder(new SecurityPropertyPostBuilder(this))
                    .withPostBuilder(new ProtocolPropertyPostBuilder(this))
                    .get();
        }
    }

    private class SecurityPropertySetPostBuilder implements Consumer<DeviceConfiguration> {

        @Override
        public void accept(DeviceConfiguration configuration) {
             configuration.createSecurityPropertySet(SECURITY_SET_NAME)
                     .authenticationLevel(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_MD5.getValue())
                    .encryptionLevel(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue())
                    .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1)
                    .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2)
                    .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1)
                    .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2)
                    .build();
        }
    }

    private class SecurityPropertyPostBuilder implements Consumer<Device>{
        MeterConfig meterConfig;

        SecurityPropertyPostBuilder(MeterConfig meterConfig){
            this.meterConfig = meterConfig;
        }

        @Override
        public void accept(Device device) {
            device.setSecurityProperties(securityPropertySet, meterConfig.getSecuritySetProperties());
            device.save();
        }
    }

    private class ProtocolPropertyPostBuilder implements Consumer<Device>{
        MeterConfig meterConfig;

        ProtocolPropertyPostBuilder(MeterConfig meterConfig){
            this.meterConfig = meterConfig;
        }

        @Override
        public void accept(Device device) {
            device.setProtocolProperty("MAC_address", meterConfig.getProperty("MAC_address"));
            device.save();
        }
    }


}

