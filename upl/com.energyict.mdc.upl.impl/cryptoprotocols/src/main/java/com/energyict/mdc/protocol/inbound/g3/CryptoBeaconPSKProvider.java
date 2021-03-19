package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.common.tls.TLSHSMConnectionType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.HsmException;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2016 - 17:24
 */
public class CryptoBeaconPSKProvider extends BeaconPSKProvider {

    public CryptoBeaconPSKProvider(DeviceIdentifier deviceIdentifier, boolean provideProtocolJavaClasName) {
        super(deviceIdentifier, provideProtocolJavaClasName);
    }

    /**
     * Use the crypto Beacon3100 protocol for this, it will use the HSM if irreversible keys are configured.
     */
    @Override
    protected DeviceProtocol newGatewayProtocol(InboundDiscoveryContext context) {
        this.context = context;
        return new CryptoBeacon3100(context.getPropertySpecService(), context.getNlsService(), context.getConverter(), context.getCollectedDataFactory(), context.getIssueFactory(), context.getObjectMapperService(), context.getDeviceMasterDataExtractor(), context.getDeviceGroupExtractor(), context.getCertificateWrapperExtractor(), context.getKeyAccessorTypeExtractor(), context.getDeviceExtractor(), context.getMessageFileExtractor(), context.getHsmProtocolService());
    }

    @Override
    protected DlmsSession getDlmsSession(DeviceProtocol deviceProtocol) {
        return ((CryptoBeacon3100) deviceProtocol).getDlmsSession();
    }

    /**
     * Use the HSM to wrap the key in the case of irreversible keys.
     * Otherwise, do the wrapping manually.
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);
        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, this.getDeviceIdentifier().toString());
        }
        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        try {
            //If the keys are not reversible, use the HSM to calculate the wrapped keys.
            byte[] wrappedKey = context.getHsmProtocolService().wrapMeterKeyForConcentrator(IrreversibleKeyImpl.fromByteArray(pskBytes), IrreversibleKeyImpl.fromByteArray(pskEncryptionKeyBytes));
            return OctetString.fromByteArray(wrappedKey);
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e));
        }
    }

    @Override
    protected byte[] parseKey(String labelAndKey) {
        return new IrreversibleKeyImpl(labelAndKey).toBase64ByteArray();
    }

    @Override
    protected ConnectionType createNewTLSConnectionType(InboundDiscoveryContext context) {
        return new TLSHSMConnectionType(context.getPropertySpecService(), context.getCertificateWrapperExtractor());
    }

}