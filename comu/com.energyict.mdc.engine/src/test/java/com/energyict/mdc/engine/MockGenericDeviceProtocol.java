package com.energyict.mdc.engine;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.ManufacturerInformation;
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
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Dummy DeviceProtocol for PluggableClassTestUsages
 * <p>
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
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getSerialNumber() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    public void setTime(Date timeToSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
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
    public DeviceProtocolCache getDeviceCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        //To change body of implemented methods use File | Settings | File Templates.
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
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return new ArrayList<>(0);
    }

    @Override
    public List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new ArrayList<>(0);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public String prepareMessageContext(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return null;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }

    /**
     * Dummy implementation that simply removes one comcommand from the root
     */
    @Override
    public CommandRoot organizeComCommands(CommandRoot commandRoot) {
        CommandRoot resultRoot = new CommandRootImpl(commandRoot.getExecutionContext(), commandRoot.getServiceProvider());
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            GroupedDeviceCommand command = resultRoot.getOrCreateGroupedDeviceCommand(groupedDeviceCommand.getOfflineDevice(), groupedDeviceCommand.getDeviceProtocol(), groupedDeviceCommand
                    .getDeviceProtocolSecurityPropertySet());
            for (ComTaskExecutionComCommand comTaskExecutionComCommand : groupedDeviceCommand) {
                for (ComCommand comCommand : comTaskExecutionComCommand) {
                    if (!comCommand.getCommandType().equals(ComCommandTypes.READ_REGISTERS_COMMAND)) {
                        command.addCommand(comCommand, comTaskExecutionComCommand.getComTaskExecution());
                    }
                }
            }
        }
        return resultRoot;
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

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

}