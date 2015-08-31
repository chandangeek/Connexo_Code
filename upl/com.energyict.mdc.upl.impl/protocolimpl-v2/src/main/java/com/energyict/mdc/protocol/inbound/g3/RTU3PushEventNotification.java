package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3ConfigurationSupport;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the RTU3 protocol to connect to the DC device.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class RTU3PushEventNotification extends PushEventNotification {

    //TODO: support events from the meter too
    //TODO junit test with trace from Alex

    protected DeviceProtocol newGatewayProtocol() {
        return new RTU3();
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((RTU3) gatewayProtocol).getDlmsSession();
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(RTU3ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw MdcManager.getComServerExceptionFactory().missingProperty(RTU3ConfigurationSupport.PSK_ENCRYPTION_KEY);
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(RTU3ConfigurationSupport.PSK_ENCRYPTION_KEY, "(hidden)", "Should be 32 hex characters");
        }

        return OctetString.fromByteArray(aesWrap(pskBytes, pskEncryptionKeyBytes));
    }

    /**
     * Extension of the structure: also add the protocol java class name of the slave device.
     * The RTU3 then uses this to read out the e-meter serial number using the public client.
     */
    @Override
    protected Structure createMacAndKeyPair(OctetString macAddressOctetString, OctetString wrappedPSKKey, DeviceIdentifier slaveDeviceIdentifier) {
        final Structure macAndKeyPair = super.createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier);
        macAndKeyPair.addDataType(OctetString.fromString(context.getInboundDAO().findOfflineDevice(slaveDeviceIdentifier).getDeviceProtocolPluggableClass().getJavaClassName()));
        return macAndKeyPair;
    }

    private byte[] aesWrap(byte[] key, byte[] masterKey) {
        final Key keyToWrap = new SecretKeySpec(key, "AES");
        final Key kek = new SecretKeySpec(masterKey, "AES");
        try {
            final Cipher aesWrap = Cipher.getInstance("AESWrap");
            aesWrap.init(Cipher.WRAP_MODE, kek);
            return aesWrap.wrap(keyToWrap);
        } catch (GeneralSecurityException e) {
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
        }
    }
}