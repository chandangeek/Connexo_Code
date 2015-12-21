package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;

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
        return new Beacon3100();
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((Beacon3100) gatewayProtocol).getDlmsSession();
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, this.getDeviceIdentifier().toString());
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw DeviceConfigurationException.invalidPropertyFormat(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, "(hidden)", "Should be 32 hex characters");
        }

        return OctetString.fromByteArray(aesWrap(pskBytes, pskEncryptionKeyBytes));
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
            macAndKeyPair.addDataType(OctetString.fromString(context.getInboundDAO().getDeviceProtocol(slaveDeviceIdentifier)));
        }

        return macAndKeyPair;
    }

    private byte[] aesWrap(byte[] key, byte[] dlmsMeterKEK) {
        final Key keyToWrap = new SecretKeySpec(key, "AES");
        final Key kek = new SecretKeySpec(dlmsMeterKEK, "AES");
        try {
            final Cipher aesWrap = Cipher.getInstance("AESWrap");
            aesWrap.init(Cipher.WRAP_MODE, kek);
            return aesWrap.wrap(keyToWrap);
        } catch (GeneralSecurityException e) {
            throw DataEncryptionException.dataEncryptionException(e);
        }
    }
}