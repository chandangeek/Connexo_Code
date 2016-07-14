package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class Beacon3100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String DLMS_METER_KEK = "DlmsMeterKEK";
    public static final String PSK_ENCRYPTION_KEY = "PSKEncryptionKey";
    public static final String DLMS_WAN_KEK = "DlmsWanKEK";
    public static final String POLLING_DELAY = "PollingDelay";

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(readCachePropertySpec());
        optionalProperties.add(dlmsKEKPropertySpec());
        optionalProperties.add(dlmsWANKEKPropertySpec());
        optionalProperties.add(pskEncryptionKeyPropertySpec());
        optionalProperties.add(generalCipheringKeyTypePropertySpec());
        optionalProperties.add(clientPrivateSigningKeyPropertySpec());
        optionalProperties.add(clientPrivateKeyAgreementKeyPropertySpec());
        optionalProperties.add(clientSigningCertificate());
        optionalProperties.add(serverTLSCertificate());
        optionalProperties.add(callingAPTitlePropertySpec());
        optionalProperties.add(pollingDelayPropertySpec());
        optionalProperties.remove(ntaSimulationToolPropertySpec());
        optionalProperties.remove(manufacturerPropertySpec());
        optionalProperties.remove(fixMbusHexShortIdPropertySpec());
        optionalProperties.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        optionalProperties.remove(deviceId());
        return optionalProperties;
    }

    private PropertySpec pollingDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(POLLING_DELAY, new TimeDuration(0));
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return PropertySpecFactory.fixedLengthHexStringPropertySpec(IDIS.CALLING_AP_TITLE, 8);
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return PropertySpecFactory.privateKeyAliasPropertySpec(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
    }

    /**
     * The certificate that matches the private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientSigningCertificate() {
        return PropertySpecFactory.certificateAliasPropertySpec(DlmsSessionProperties.CLIENT_SIGNING_CERTIFICATE);
    }

    /**
     * The TLS certificate of the server. Not actively used in the protocols.
     */
    private PropertySpec serverTLSCertificate() {
        return PropertySpecFactory.certificateAliasPropertySpec(DlmsSessionProperties.SERVER_TLS_CERTIFICATE);
    }

    /**
     * The private key of the client (the ComServer) used for key agreement (ECDH)
     */
    private PropertySpec clientPrivateKeyAgreementKeyPropertySpec() {
        return PropertySpecFactory.privateKeyAliasPropertySpec(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY);
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(
                DlmsProtocolProperties.CIPHERING_TYPE,
                CipheringType.GLOBAL.getDescription(),      //Default
                CipheringType.GLOBAL.getDescription(),
                CipheringType.DEDICATED.getDescription(),
                CipheringType.GENERAL_GLOBAL.getDescription(),
                CipheringType.GENERAL_DEDICATED.getDescription(),
                CipheringType.GENERAL_CIPHERING.getDescription()
        );
    }

    private PropertySpec generalCipheringKeyTypePropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValues(
                DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE,
                GeneralCipheringKeyType.IDENTIFIED_KEY.getDescription(),
                GeneralCipheringKeyType.WRAPPED_KEY.getDescription(),
                GeneralCipheringKeyType.AGREED_KEY.getDescription()
        );
    }

    /**
     * The KEK of the Beacon. Use this to wrap the AK/EK of the Beacon device itself
     */
    private PropertySpec dlmsWANKEKPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(DLMS_WAN_KEK);
    }

    private PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(READCACHE_PROPERTY, false);
    }

    /**
     * A key used to encrypt DLMS keys of slave meters (aka a key encryption key, KEK)
     */
    private PropertySpec dlmsKEKPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(DLMS_METER_KEK);
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(PSK_ENCRYPTION_KEY);
    }
}