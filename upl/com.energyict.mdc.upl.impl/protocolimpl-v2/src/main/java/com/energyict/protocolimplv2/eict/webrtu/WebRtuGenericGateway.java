package com.energyict.protocolimplv2.eict.webrtu;

import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.common.AbstractDlmsProtocol;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/04/13
 * Time: 10:02
 */
public class WebRtuGenericGateway extends AbstractDlmsProtocol {

    @Override
    protected String getFirmwareVersion() {
        return null;  // nothing to do yet
    }

    @Override
    protected DlmsProtocolProperties getProtocolProperties() {
        return null;  // nothing to do yet
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
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
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
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return null;  // nothing to do yet
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return null;  // nothing to do yet
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
    public String getVersion() {
        return "$Date$";
    }
}
