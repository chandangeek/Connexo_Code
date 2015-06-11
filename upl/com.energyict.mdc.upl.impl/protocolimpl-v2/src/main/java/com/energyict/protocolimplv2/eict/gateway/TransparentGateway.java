package com.energyict.protocolimplv2.eict.gateway;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.common.AbstractGateway;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 8/06/2015 - 11:20
 */
public class TransparentGateway extends AbstractGateway {

    private DeviceProtocolSecurityCapabilities securitySupport;

    @Override
    public String getProtocolDescription() {
        return "EnergyICT transparent gateway";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(
                new SioSerialConnectionType(),
                new RxTxSerialConnectionType(),
                new SioAtModemConnectionType(),
                new RxTxAtModemConnectionType(),
                new OutboundUdpConnectionType(),
                new OutboundTcpIpConnectionType()
        );
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new NoParamsDeviceProtocolDialect(),
                new SerialDeviceProtocolDialect(),
                new TcpDeviceProtocolDialect()
        );
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // Do nothing
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // Do nothing
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // Do nothing
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        // Do nothing
    }

    private DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (this.securitySupport == null) {
            this.securitySupport = new NoSecuritySupport();
        }
        return this.securitySupport;
    }
}