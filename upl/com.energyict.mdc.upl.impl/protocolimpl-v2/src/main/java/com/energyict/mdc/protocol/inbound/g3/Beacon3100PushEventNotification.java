package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the Beacon3100 protocol to connect to the DC device.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class Beacon3100PushEventNotification extends PushEventNotification {

    //TODO junit test with encrypted traces

    /**
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    private static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    private boolean provideProtocolJavaClasName = true;

    protected DeviceProtocol newGatewayProtocol() {
        return new Beacon3100();
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((Beacon3100) gatewayProtocol).getDlmsSession();
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG);
        }
        return parser;
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw MdcManager.getComServerExceptionFactory().missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, this.getDeviceIdentifier());
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, "(hidden)", "Should be 32 hex characters");
        }

        return OctetString.fromByteArray(aesWrap(pskBytes, pskEncryptionKeyBytes));
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        final List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(PropertySpecFactory.notNullableBooleanPropertySpec(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true));
        return optionalProperties;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        super.addProperties(properties);
        this.provideProtocolJavaClasName = properties.<Boolean>getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
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