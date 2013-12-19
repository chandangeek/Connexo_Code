package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.dlms.common.AbstractDlmsProtocol;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    protected DlmsProtocolProperties getProtocolProperties() {
        return new Dsmr23Properties();
    }

    @Override
    protected void initAfterConnect() {
        // nothing to do yet
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        return null;
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
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}
