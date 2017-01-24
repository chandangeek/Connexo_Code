package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/10/16
 * Time: 14:31
 */
public class Beacon3100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String DLMS_METER_KEK = "DlmsMeterKEK";
    public static final String PSK_ENCRYPTION_KEY = "PSKEncryptionKey";
    public static final String DLMS_WAN_KEK = "DlmsWanKEK";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";

    public Beacon3100ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(readCachePropertySpec());
        optionalProperties.add(dlmsKEKPropertySpec());
        optionalProperties.add(dlmsWANKEKPropertySpec());
        optionalProperties.add(pskEncryptionKeyPropertySpec());
        optionalProperties.add(generalCipheringKeyTypePropertySpec());
        optionalProperties.add(pollingDelayPropertySpec());
        optionalProperties.add(requestAuthenticatedFrameCounter());
//        optionalProperties.add(clientPrivateSigningKeyPropertySpec());
//        optionalProperties.add(clientPrivateKeyAgreementKeyPropertySpec());
//        optionalProperties.add(clientSigningCertificate());
//        optionalProperties.add(serverTLSCertificate());
        optionalProperties.add(callingAPTitlePropertySpec());
        optionalProperties.remove(ntaSimulationToolPropertySpec());
        optionalProperties.remove(manufacturerPropertySpec());
        optionalProperties.remove(fixMbusHexShortIdPropertySpec());
        optionalProperties.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        optionalProperties.remove(deviceId());
        return optionalProperties;
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return getPropertySpecService().booleanSpec().named(REQUEST_AUTHENTICATED_FRAME_COUNTER, REQUEST_AUTHENTICATED_FRAME_COUNTER).describedAs(REQUEST_AUTHENTICATED_FRAME_COUNTER).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return getPropertySpecService().timeDurationSpec().named(POLLING_DELAY, POLLING_DELAY).describedAs(POLLING_DELAY).setDefaultValue(new TimeDuration(0)).finish();
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return getPropertySpecService().hexStringSpec().named(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE).describedAs(IDIS.CALLING_AP_TITLE).finish();
    }
//
//    /**
//     * The private key of the client (the ComServer) used for digital signature (ECDSA)
//     */
//    private PropertySpec clientPrivateSigningKeyPropertySpec() {
//        return PropertySpecFactory.privateKeyAliasPropertySpec(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
//    }
//
//    /**
//     * The certificate that matches the private key of the client (the ComServer) used for digital signature (ECDSA)
//     */
//    private PropertySpec clientSigningCertificate() {
//        return PropertySpecFactory.certificateAliasPropertySpec(DlmsSessionProperties.CLIENT_SIGNING_CERTIFICATE);
//    }
//
//    /**
//     * The TLS certificate of the server. Not actively used in the protocols.
//     */
//    private PropertySpec serverTLSCertificate() {
//        return PropertySpecFactory.certificateAliasPropertySpec(DlmsSessionProperties.SERVER_TLS_CERTIFICATE);
//    }
//
//    /**
//     * The private key of the client (the ComServer) used for key agreement (ECDH)
//     */
//    private PropertySpec clientPrivateKeyAgreementKeyPropertySpec() {
//        return PropertySpecFactory.privateKeyAliasPropertySpec(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY);
//    }

    protected PropertySpec cipheringTypePropertySpec() {
        return getPropertySpecService().stringSpec().named(DlmsProtocolProperties.CIPHERING_TYPE, DlmsProtocolProperties.CIPHERING_TYPE)
                .describedAs(DlmsProtocolProperties.CIPHERING_TYPE).setDefaultValue(CipheringType.GLOBAL.getDescription())
                .addValues(CipheringType.GLOBAL.getDescription(),
                        CipheringType.DEDICATED.getDescription(),
                        CipheringType.GENERAL_GLOBAL.getDescription(),
                        CipheringType.GENERAL_DEDICATED.getDescription(),
                        CipheringType.GENERAL_CIPHERING.getDescription()).finish();
    }

    private PropertySpec generalCipheringKeyTypePropertySpec() {
        return getPropertySpecService().stringSpec().named(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE, DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE)
                .describedAs(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE)
                .addValues(GeneralCipheringKeyType.IDENTIFIED_KEY.getDescription(),
                        GeneralCipheringKeyType.WRAPPED_KEY.getDescription(),
                        GeneralCipheringKeyType.AGREED_KEY.getDescription()).finish();
    }

    /**
     * The KEK of the Beacon. Use this to wrap the AK/EK of the Beacon device itself
     */
    private PropertySpec dlmsWANKEKPropertySpec() {
        return getPropertySpecService().encryptedStringSpec().named(DLMS_WAN_KEK, DLMS_WAN_KEK).describedAs(DLMS_WAN_KEK).finish();
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService().booleanSpec().named(READCACHE_PROPERTY, READCACHE_PROPERTY).describedAs(READCACHE_PROPERTY).setDefaultValue(false).finish();
    }

    /**
     * A key used to encrypt DLMS keys of slave meters (aka a key encryption key, KEK)
     */
    private PropertySpec dlmsKEKPropertySpec() {
        return getPropertySpecService().encryptedStringSpec().named(DLMS_METER_KEK, DLMS_METER_KEK).describedAs(DLMS_METER_KEK).finish();
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return getPropertySpecService().stringSpec().named(PSK_ENCRYPTION_KEY, PSK_ENCRYPTION_KEY).describedAs(PSK_ENCRYPTION_KEY).finish();
    }
}
