package com.energyict.mdc.engine;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Dummy DeviceProtocol for PluggableClassTestUsages
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 15:16
 */
public class MockGenericDeviceProtocol implements GenericDeviceProtocol {

    private OfflineDevice offlineDevice;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
    }

    @Override
    public void terminate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.values());
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.emptyList();
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Collections.emptyList();
    }

    @Override
    public String getSerialNumber() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTime(Date timeToSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getTime() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logOn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void daisyChainedLogOn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logOff() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void daisyChainedLogOff() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return new ArrayList<>(0);
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new ArrayList<>(0);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String format (PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    /**
     * Dummy implementation that simply removes one comcommand from the root
     */
    @Override
    public CommandRoot organizeComCommands(CommandRoot commandRoot) {
        return ((CommandRootImpl) commandRoot).shallowCloneFor(offlineDevice, mock(CommandRoot.ServiceProvider.class), EnumSet.of(ComCommandTypes.READ_REGISTERS_COMMAND));
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>();
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
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }
}