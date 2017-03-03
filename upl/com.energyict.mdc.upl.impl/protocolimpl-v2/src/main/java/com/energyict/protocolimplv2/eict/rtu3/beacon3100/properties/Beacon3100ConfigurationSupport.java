package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.CertificateAlias;
import com.energyict.mdc.upl.security.PrivateKeyAlias;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";

    public Beacon3100ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(readCachePropertySpec());
        propertySpecs.add(dlmsKEKPropertySpec());
        propertySpecs.add(dlmsWANKEKPropertySpec());
        propertySpecs.add(pskEncryptionKeyPropertySpec());
        propertySpecs.add(generalCipheringKeyTypePropertySpec());
        propertySpecs.add(pollingDelayPropertySpec());
        propertySpecs.add(requestAuthenticatedFrameCounter());
        propertySpecs.add(clientPrivateSigningKeyPropertySpec());
        propertySpecs.add(clientPrivateKeyAgreementKeyPropertySpec());
        propertySpecs.add(serverTLSCertificate());
        propertySpecs.add(callingAPTitlePropertySpec());
        propertySpecs.add(deviceSystemTitlePropertySpec());
        propertySpecs.add(publicClientPreEstablishedPropertySpec());

        propertySpecs.remove(ntaSimulationToolPropertySpec());
        propertySpecs.remove(manufacturerPropertySpec());
        propertySpecs.remove(fixMbusHexShortIdPropertySpec());
        propertySpecs.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        propertySpecs.remove(deviceId());

        return propertySpecs;
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(REQUEST_AUTHENTICATED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_EICT_REQUEST_AUTHENTCATED_FRAME_COUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ZERO, PropertyTranslationKeys.V2_EICT_POLLING_DELAY);
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return this.stringSpecOfExactLength(IDIS.CALLING_AP_TITLE, false, 8, PropertyTranslationKeys.V2_EICT_CALLING_AP_TITLE);
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return this.getPropertySpecService().referenceSpec(PrivateKeyAlias.class.getName())
                .named(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .describedAs(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .finish();
    }

    /**
     * The TLS certificate of the server. Not actively used in the protocols.
     */
    private PropertySpec serverTLSCertificate() {
        return this.getPropertySpecService().referenceSpec(CertificateAlias.class.getName())
                .named(DlmsSessionProperties.SERVER_TLS_CERTIFICATE, DlmsSessionProperties.SERVER_TLS_CERTIFICATE)
                .describedAs(DlmsSessionProperties.SERVER_TLS_CERTIFICATE)
                .finish();
    }

    /**
     * The private key of the client (the ComServer) used for key agreement (ECDH)
     */
    private PropertySpec clientPrivateKeyAgreementKeyPropertySpec() {
        return this.getPropertySpecService().referenceSpec(PrivateKeyAlias.class.getName())
                .named(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY, DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY)
                .describedAs(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY)
                .finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return this.stringWithDefaultSpec(
                DlmsProtocolProperties.CIPHERING_TYPE,
                false,
                PropertyTranslationKeys.V2_EICT_CIPHERING_TYPE,
                CipheringType.GLOBAL.getDescription(),      //Default
                CipheringType.GLOBAL.getDescription(),
                CipheringType.DEDICATED.getDescription(),
                CipheringType.GENERAL_GLOBAL.getDescription(),
                CipheringType.GENERAL_DEDICATED.getDescription(),
                CipheringType.GENERAL_CIPHERING.getDescription()
        );
    }

    private PropertySpec generalCipheringKeyTypePropertySpec() {
        return this.stringSpec(
                DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE,
                false,
                PropertyTranslationKeys.V2_EICT_GENERAL_CIPHERING_KEY_TYPE,
                GeneralCipheringKeyType.IDENTIFIED_KEY.getDescription(),
                GeneralCipheringKeyType.WRAPPED_KEY.getDescription(),
                GeneralCipheringKeyType.AGREED_KEY.getDescription()
        );
    }

    /**
     * The KEK of the Beacon. Use this to wrap the AK/EK of the Beacon device itself
     */
    private PropertySpec dlmsWANKEKPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DLMS_WAN_KEK, false, PropertyTranslationKeys.V2_EICT_DLMS_WAN_KEK, this.getPropertySpecService()::encryptedStringSpec).finish();
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_EICT_READCACHE, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * A key used to encrypt DLMS keys of slave meters (aka a key encryption key, KEK)
     */
    private PropertySpec dlmsKEKPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DLMS_METER_KEK, false, PropertyTranslationKeys.V2_EICT_DLMS_METER_KEK, this.getPropertySpecService()::encryptedStringSpec).finish();
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PSK_ENCRYPTION_KEY, false, PropertyTranslationKeys.V2_EICT_PSK_ENCRYPTION_KEY, this.getPropertySpecService()::encryptedStringSpec).finish();
    }

    public PropertySpec deviceSystemTitlePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE, false, PropertyTranslationKeys.V2_EICT_DLMS_DEVICE_SYSTEM_TITLE, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }

    private <T> PropertySpec spec(String name, boolean required, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpecOfExactLength(String name, boolean required, int length, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, () -> this.getPropertySpecService().stringSpecOfExactLength(length));
    }

    private PropertySpec stringSpec(String name, boolean required, TranslationKey translationKey, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }
}