package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.common.AbstractDlmsProtocol;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 16:00
 */
public class RtuPlusServer extends AbstractDlmsProtocol {

    @Override
    protected String getFirmwareVersion() {
        return null;  // nothing to do yet
    }

    @Override
    protected DlmsSessionProperties getDlmsSessionProperties() {
        return null;  // nothing to do yet
    }

    @Override
    protected void initAfterConnect() {
        // nothing to do yet
    }

    @Override
    public int requestConfigurationChanges() {
        return -1;        // nothing to do yet
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
        return null;  // nothing to do yet
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
        return null;  // nothing to do yet
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;  // nothing to do yet
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;  // nothing to do yet
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new NoParamsDeviceProtocolDialect());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;  // nothing to do yet
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server2 G3 PLC";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
