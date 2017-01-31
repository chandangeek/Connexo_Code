/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
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
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.impl.channels.inbound.EIWebPlusConnectionType;
import com.energyict.protocols.mdc.protocoltasks.EiWebPlusDialect;

import com.energyict.protocolimplv2.messages.convertor.EIWebPlusMessageConverter;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
public class RtuServer implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private LegacyMessageConverter messageConverter;

    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final NoOrPasswordSecuritySupport securitySupport;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public RtuServer(NoOrPasswordSecuritySupport securitySupport, CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, Clock clock, Thesaurus thesaurus) {
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.clock = clock;
        this.securitySupport = securitySupport;
        this.thesaurus = thesaurus;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        // nothing much to do
        this.offlineDevice = offlineDevice;
    }

    @Override
    public void terminate() {
        // nothing much to do
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.<ConnectionType>singletonList(new EIWebPlusConnectionType(this.thesaurus, this.propertySpecService));
    }

    @Override
    public void logOn() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOn() {
        // nothing much to do
    }

    @Override
    public void logOff() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOff() {
        // nothing much to do
    }

    @Override
    public String getSerialNumber() {
        if (this.offlineDevice != null) {
            return this.offlineDevice.getSerialNumber();
        }
        else {
            return "";
        }
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // nothing much to do
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {
        // nothing much to do
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList();
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        return Date.from(getClock().instant());
    }

    private Clock getClock() {
        return this.clock;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getMessageConverter().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new EIWebPlusMessageConverter();
        }
        return messageConverter;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.<DeviceProtocolDialect>singletonList(new EiWebPlusDialect(this.thesaurus, this.propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // nothing much to do
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        // nothing much to do
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return this.securitySupport.getCustomPropertySet();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return this.securitySupport.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return this.securitySupport.getEncryptionAccessLevels();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server EIWebPlus";
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
    public String getVersion() {
        return "$Date: 2014-04-25 14:19:38 +0200 (Fri, 25 Apr 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return collectedDataFactory.createFirmwareVersionsCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
    }

}