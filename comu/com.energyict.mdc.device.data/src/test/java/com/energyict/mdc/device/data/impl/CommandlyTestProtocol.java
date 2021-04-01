/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test protocol with lots and lots of available device commands, intended to allow testing device message searches
 */
public class CommandlyTestProtocol implements DeviceProtocol {

    public static final String MYOPTIONALPROPERTY = "MyOptionalProperty";

    @Inject
    public CommandlyTestProtocol() {
        super();
    }

    @Override
    public void terminate() {

    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
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
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

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
    public void setTime(Date timeToSet) {

    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Stream.of(DeviceMessageId.values()).map(deviceMessageId->new TestDeviceMessageSpecImpl(deviceMessageId.dbValue())).collect(Collectors.toList());
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
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        DeviceProtocolDialect protocolDialect1 = new ProtocolDialectPropertiesImplIT.TestProtocolDialect1();
        DeviceProtocolDialect protocolDialect2 = new ProtocolDialectPropertiesImplIT.TestProtocolDialect2();
        return Arrays.asList(protocolDialect1, protocolDialect2);
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {

    }

    private PropertySpec getUserNamePropertySpec() {
        BasicPropertySpec propertySpec = new BasicPropertySpec(new StringFactory());
        propertySpec.setName("usrName");
        return propertySpec;
    }

    private PropertySpec getPasswordPropertySpec() {
        BasicPropertySpec propertySpec = new BasicPropertySpec(new StringFactory());
        propertySpec.setName("password");
        return propertySpec;
    }

    @Override
    public List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(new com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public String getTranslationKey() {
                return "Zero";
            }

            @Override
            public String getDefaultTranslation() {
                return "Zero";
            }

            @Override
            public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
                return Arrays.asList(ConnexoToUPLPropertSpecAdapter.adaptTo(getPasswordPropertySpec()), ConnexoToUPLPropertSpecAdapter.adaptTo(getUserNamePropertySpec()));
            }
        });
    }

    @Override
    public List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.singletonList(new com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel() {
            @Override
            public int getId() {
                return 0;
            }

            @Override
            public String getTranslationKey() {
                return "Zero";
            }

            @Override
            public String getDefaultTranslation() {
                return "Zero";
            }

            @Override
            public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
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
    public void init(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, ComChannel comChannel) {

    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(getOptionalPropertySpec());
    }

    public PropertySpec getOptionalPropertySpec() {
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

}