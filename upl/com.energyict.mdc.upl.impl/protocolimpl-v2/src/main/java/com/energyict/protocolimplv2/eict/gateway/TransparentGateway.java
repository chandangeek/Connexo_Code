package com.energyict.protocolimplv2.eict.gateway;

import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocolimplv2.common.AbstractGateway;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 8/06/2015 - 11:20
 */
public class TransparentGateway extends AbstractGateway {

    private DeviceProtocolSecurityCapabilities securitySupport;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public TransparentGateway(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, NlsService nlsService) {
        super(collectedDataFactory);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public String getProtocolDescription() {
        return "Generic transparent gateway";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-06-11 16:46:36 +0200 (Thu, 11 Jun 2015) $";
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(
                new SioSerialConnectionType(this.propertySpecService),
                new RxTxSerialConnectionType(this.propertySpecService),
                new SioAtModemConnectionType(this.propertySpecService),
                new RxTxAtModemConnectionType(this.propertySpecService),
                new OutboundUdpConnectionType(this.propertySpecService),
                new OutboundTcpIpConnectionType(this.propertySpecService)
        );
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new NoParamsDeviceProtocolDialect(this.nlsService),
                new SerialDeviceProtocolDialect(this.propertySpecService, this.nlsService),
                new TcpDeviceProtocolDialect(this.propertySpecService, this.nlsService)
        );
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return getSecuritySupport().getClientSecurityPropertySpec();
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
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
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
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
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