/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestProtocol implements DeviceProtocol {

    public static final String DIALECT_1_NAME = TestProtocolDialect1.class.getSimpleName();
    private static final String DIALECT_2_NAME = TestProtocolDialect2.class.getSimpleName();
    public static final String REQUIRED_PROPERTY_NAME = "ThisIsTheRequiredPropertyName";
    private static final String REQUIRED_PROPERTY_VALUE = "lmskdjfsmldkfjsqlmdkfj";
    public static final String OPTIONAL_PROPERTY_NAME = "ThisIsTheOptionalPropertyName";
    private static final String OPTIONAL_PROPERTY_VALUE = "sdlfkjnsqdlmfjsqdfsqdfsqdf";
    private static final String OPTIONAL_PROPERTY_WITH_CONVERTED_NAME = "OptionalPropertyWith1227106396";
    private static final String OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE = "jklmdsqfjkldsqlozidkcxjnnclsqkdkjoijfze65465zef65e6f51ze6f51zefze";
    private static final String INHERITED_OPTIONAL_PROPERTY_VALUE = "inheritedmqjdsflmdsqkjflmsqdjkfmsqldkfjlmdsqjkf";

    private final PropertySpecService propertySpecService;

    public static final String MYOPTIONALPROPERTY = "MyOptionalProperty";


    @Inject
    public TestProtocol(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

    }

    @Override
    public void terminate() {

    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.values());
    }

    @Override
    public String getProtocolDescription() {
        return "";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return null;
    }

    @Override
    public void logOn() {

    }

    @Override
    public void daisyChainedLogOn() {

    }

    @Override
    public void logOff() {

    }

    @Override
    public void daisyChainedLogOff() {

    }

    @Override
    public String getSerialNumber() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {

    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return new HashSet<>(Arrays.asList(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        DeviceProtocolDialect protocolDialect1 = new TestProtocolDialect1();
        DeviceProtocolDialect protocolDialect2 = new TestProtocolDialect2();
        return Arrays.asList(protocolDialect1, protocolDialect2);
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(getCustomPropertySet(this.propertySpecService));
    }

    public static CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>> getCustomPropertySet(PropertySpecService propertySpecService) {
        return new BasicAuthenticationCustomPropertySet(propertySpecService);
    }

    private PropertySpec getUserNamePropertySpec() {
        BasicPropertySpec propertySpec = new BasicPropertySpec(new StringFactory());
        propertySpec.setName(BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.javaName());
        return propertySpec;
    }

    private PropertySpec getPasswordPropertySpec() {
        BasicPropertySpec propertySpec = new BasicPropertySpec(new StringFactory());
        propertySpec.setName(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName());
        return propertySpec;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(new AuthenticationDeviceAccessLevel() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public String getTranslation() {
                return "Zero";
            }

            @Override
            public List<PropertySpec> getSecurityProperties() {
                return Arrays.asList(getPasswordPropertySpec(), getUserNamePropertySpec());
            }
        });
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.singletonList(new EncryptionDeviceAccessLevel() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public String getTranslation() {
                return "Zero";
            }

            @Override
            public List<PropertySpec> getSecurityProperties() {
                return Collections.emptyList();
            }
        });
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getVersion() {
        return "For Testing Purposes only";
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(getOptionalPropertySpec());
    }

    public PropertySpec getOptionalPropertySpec(){
        BasicPropertySpec propertySpec = new BasicPropertySpec(new StringFactory());
        propertySpec.setName(MYOPTIONALPROPERTY);
        return propertySpec;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    public static class TestProtocolDialect1 implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DIALECT_1_NAME;
        }

        @Override
        public String getDisplayName() {
            return this.getDeviceProtocolDialectName();
        }


        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.of(new ProtocolDialectCustomPropertySet());
        }

    }

    public static class TestProtocolDialect2 implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DIALECT_2_NAME;
        }

        @Override
        public String getDisplayName() {
            return this.getDeviceProtocolDialectName();
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.of(new ProtocolDialectCustomPropertySet());
        }

    }

}