/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.protocolimplv2.water.merlin;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.security.MerlinSecuritySupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

public class MerlinOutboundProtocol implements DeviceProtocol, SerialNumberSupport {

    public static final String PROTOCOL_VERSION = "2022-12-16";
    public static final String PROTOCOL_DESCRIPTION = "Merlin Water clip-on outbound protocol";

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final MerlinSecuritySupport securitySupport;

    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;
    private TypedProperties properties;

    public MerlinOutboundProtocol(
            PropertySpecService propertySpecService,
            NlsService nlsService,
            CollectedDataFactory collectedDataFactory,
            IssueFactory issueFactory
    ) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.securitySupport = new MerlinSecuritySupport(propertySpecService);
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new OutboundTcpIpConnectionType(this.getPropertySpecService()),
                new InboundIpConnectionType()
        );
    }

    private PropertySpecService getPropertySpecService() {
        return this.propertySpecService;
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return PROTOCOL_DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

    }

    @Override
    public void terminate() {

    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
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
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(
                new TcpDeviceProtocolDialect(getPropertySpecService(), nlsService)
        );
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

    }

    @Override
    public String getSerialNumber() {
        return null;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                timeZonePropertySpec()
        );
    }

    private PropertySpec timeZonePropertySpec() {
        TimeZone[] timeZones = Arrays.stream(TimeZone.getAvailableIDs()).map(TimeZone::getTimeZone).toArray(TimeZone[]::new);

        return this.propertySpecService
                .timeZoneSpec()
                .named(PropertyTranslationKeys.V2_ELSTER_TIMEZONE.getKey(), PropertyTranslationKeys.V2_ELSTER_TIMEZONE)
                .describedAs(PropertyTranslationKeys.V2_ELSTER_TIMEZONE_DESCRIPTION)
                .addValues(timeZones)
                .setDefaultValue(TimeZone.getTimeZone("UTC"))
                .markEditable()
                .finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return securitySupport.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return securitySupport.getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {

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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    public TypedProperties getProperties() {
        return properties;
    }

    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet() {
        return deviceProtocolSecurityPropertySet;
    }
}
