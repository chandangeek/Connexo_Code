package com.energyict.protocolimplv2.eict.gatewayz3;

import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 13:54
 */
@Deprecated //The Cynet RF functionality is no longer supported in EIServer 9.x. This class is placeholder so the old devices can be migrated.
public class GateWayZ3 extends AbstractDlmsProtocol {

    @Override
    public DlmsSessionProperties getDlmsSessionProperties() {
        return null;  // nothing to do
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        // nothing to do
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;  // nothing to do
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
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
        return "$Date: 2015-11-06 14:27:09 +0100 (Fri, 06 Nov 2015) $";
    }
}
