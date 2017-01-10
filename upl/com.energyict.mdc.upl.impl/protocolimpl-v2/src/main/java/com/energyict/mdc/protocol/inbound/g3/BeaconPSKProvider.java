package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2015 - 12:04
 */
public class BeaconPSKProvider extends G3GatewayPSKProvider {

    private final boolean provideProtocolJavaClasName;

    public BeaconPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context, boolean provideProtocolJavaClasName) {
        super(deviceIdentifier, context);
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
    }

    protected DeviceProtocol newGatewayProtocol() {
        return new Beacon3100(getContext().getPropertySpecService(), getContext().getNlsService(), getContext().getConverter(), getContext().getCollectedDataFactory(), getContext().getIssueFactory(), getContext().getObjectMapperService(), getContext().getDeviceMasterDataExtractor(), getContext().getDeviceGroupExtractor());
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((Beacon3100) gatewayProtocol).getDlmsSession();
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        return this.wrap(com.energyict.protocolimpl.properties.TypedProperties.copyOf(properties), pskBytes);
    }

    private OctetString wrap(com.energyict.protocolimpl.properties.TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, this.getDeviceIdentifier().toString());
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw DeviceConfigurationException.invalidPropertyFormat(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, "(hidden)", "Should be 32 hex characters");
        }

        return OctetString.fromByteArray(ProtocolTools.aesWrap(pskBytes, pskEncryptionKeyBytes));
    }

    /**
     * Extension of the structure: also add the protocol java class name of the slave device.
     * The Beacon3100 then uses this to read out the e-meter serial number using the public client.
     */
    @Override
    protected Structure createMacAndKeyPair(OctetString macAddressOctetString, OctetString wrappedPSKKey, DeviceIdentifier slaveDeviceIdentifier) {
        final Structure macAndKeyPair = super.createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier);

        //Only add the protcool java class name if it is indicated by the property.
        if (provideProtocolJavaClasName) {
            macAndKeyPair.addDataType(OctetString.fromString(context.getInboundDAO().getDeviceProtocolClassName(slaveDeviceIdentifier)));
        }

        return macAndKeyPair;
    }
}