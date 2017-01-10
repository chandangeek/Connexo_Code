package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.google.common.base.Supplier;

import java.time.Duration;
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
        propertySpecs.add(publicClientPreEstablishedPropertySpec());

        propertySpecs.remove(ntaSimulationToolPropertySpec());
        propertySpecs.remove(manufacturerPropertySpec());
        propertySpecs.remove(fixMbusHexShortIdPropertySpec());
        propertySpecs.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        propertySpecs.remove(deviceId());

        return propertySpecs;
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(REQUEST_AUTHENTICATED_FRAME_COUNTER, false, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ZERO);
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return this.stringSpecOfExactLength(IDIS.CALLING_AP_TITLE, false,8);
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return this.getPropertySpecService().referenceSpec("com.energyict.mdc.upl.properties.PrivateKeyAlias")
                .named(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .describedAs(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .finish();
    }

    /**
     * The TLS certificate of the server. Not actively used in the protocols.
     */
    private PropertySpec serverTLSCertificate() {
        return this.getPropertySpecService().referenceSpec("com.energyict.mdc.upl.properties.CertificateAlias")
                .named(DlmsSessionProperties.SERVER_TLS_CERTIFICATE, DlmsSessionProperties.SERVER_TLS_CERTIFICATE)
                .describedAs(DlmsSessionProperties.SERVER_TLS_CERTIFICATE)
                .finish();
    }

    /**
     * The private key of the client (the ComServer) used for key agreement (ECDH)
     */
    private PropertySpec clientPrivateKeyAgreementKeyPropertySpec() {
        return this.getPropertySpecService().referenceSpec("com.energyict.mdc.upl.properties.PrivateKeyAlias")
                .named(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY, DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY)
                .describedAs(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY)
                .finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return UPLPropertySpecFactory.stringWithDefault(
                DlmsProtocolProperties.CIPHERING_TYPE,
                false,
                CipheringType.GLOBAL.getDescription(),      //Default
                CipheringType.GLOBAL.getDescription(),
                CipheringType.DEDICATED.getDescription(),
                CipheringType.GENERAL_GLOBAL.getDescription(),
                CipheringType.GENERAL_DEDICATED.getDescription(),
                CipheringType.GENERAL_CIPHERING.getDescription()
        );
    }

    private PropertySpec generalCipheringKeyTypePropertySpec() {
        return UPLPropertySpecFactory.string(
                DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE,
                false,
                GeneralCipheringKeyType.IDENTIFIED_KEY.getDescription(),
                GeneralCipheringKeyType.WRAPPED_KEY.getDescription(),
                GeneralCipheringKeyType.AGREED_KEY.getDescription()
        );
    }

    /**
     * The KEK of the Beacon. Use this to wrap the AK/EK of the Beacon device itself
     */
    private PropertySpec dlmsWANKEKPropertySpec() {
        return UPLPropertySpecFactory.encryptedString(DLMS_WAN_KEK, false);
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.booleanValue(READCACHE_PROPERTY, false, false);
    }

    /**
     * A key used to encrypt DLMS keys of slave meters (aka a key encryption key, KEK)
     */
    private PropertySpec dlmsKEKPropertySpec() {
        return UPLPropertySpecFactory.encryptedString(DLMS_METER_KEK, false);
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return UPLPropertySpecFactory.encryptedString(PSK_ENCRYPTION_KEY, false);
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }

    private <T> PropertySpec spec(String name, boolean required, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, optionsSupplier).finish();
    }

    private PropertySpec stringSpecOfExactLength(String name, boolean required, int length) {
        return this.spec(name,required, () -> this.getPropertySpecService().stringSpecOfExactLength(length));
    }
}