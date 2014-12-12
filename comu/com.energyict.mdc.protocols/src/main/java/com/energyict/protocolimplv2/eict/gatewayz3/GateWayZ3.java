package com.energyict.protocolimplv2.eict.gatewayz3;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
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
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.common.AbstractDlmsProtocol;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;

import javax.inject.Inject;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 13:54
 */
public class GateWayZ3 extends AbstractDlmsProtocol {

    @Inject
    public GateWayZ3(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.emptyList();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getSerialNumber() {
        return null;  // nothing to do
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
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;  // nothing to do
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;  // nothing to do
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;  // nothing to do
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.emptyList();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;  // nothing to do
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS Gateway";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
