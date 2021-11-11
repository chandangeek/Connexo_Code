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
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.energyict.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.USE_GBT;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class Beacon3100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String DLMS_METER_KEK = "DlmsMeterKEK";
    public static final String PSK_ENCRYPTION_KEY = "PSKEncryptionKey";
    public static final String DLMS_WAN_KEK = "DlmsWanKEK";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";

    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String READ_OLD_OBIS_CODES = "ReadOldObisCodes";
    public static final String PRE_2_0_FIRMWARE = "Pre20Firmware";
    public static final String BROADCAST_AUTHENTICATION_KEY = "BroadcastAuthenticationKey";
    public static final String BROADCAST_ENCRYPTION_KEY = "BroadcastEncryptionKey";
    public static final String UPDATE_IPV6_ON_TOPOLOGY = "UpdateIPv6OnTopology";

    public static final String DEFAULT_BACKLOG_LOADPROFILE = "DefaultBacklogLoadProfile";
    public static final String DEFAULT_BACKLOG_EVENTLOG = "DefaultBacklogEventLog";
    public static final String DEFAULT_BUFFERSIZE_REGISTERS = "DefaultBufferSizeRegisters";
    public static final String IPV6_ADDRESS_AND_PREFIX_LENGTH = "IPv6AddressAndPrefixLength";
    public static final String DO_PATH_REQUESTS_ON_TOPOLOGY = "DoPathRequestsOnTopology";
    public static final String DO_ROUTE_REQUESTS_ON_TOPOLOGY = "DoRouteRequestsOnTopology";
    private PropertySpecService propertySpecService;

    public Beacon3100ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
        this.propertySpecService = propertySpecService;
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

        propertySpecs.add(useCachedFrameCounter());
        propertySpecs.add(validateCachedFrameCounter());
        propertySpecs.add(frameCounterRecoveryRetries());
        propertySpecs.add(frameCounterRecoveryStep());
        propertySpecs.add(initialFrameCounter());
        propertySpecs.add(increaseFrameCounterOnHLSReply());
        propertySpecs.add(frameCounterLimit());
        propertySpecs.add(readOldObisCodes());
        propertySpecs.add(hasPre20Firmware());

        propertySpecs.add(defaultBacklogLoadProfile());
        propertySpecs.add(defaultBacklogEventLog());
        propertySpecs.add(defaultBufferSizeRegisters());

        propertySpecs.add(broadcastAuthenticationKeyPropertySpec());
        propertySpecs.add(broadcastEncryptionKeyPropertySpec());
        propertySpecs.add(ipv6AddressAndPrefixLength());
        propertySpecs.add(useGeneralBlockTransferPropertySpec());
        propertySpecs.add(generalBlockTransferWindowSizePropertySpec());

        propertySpecs.add(updateIpv6OnTopologyPropertySpec());
        propertySpecs.add(doPathRequestOnTopologyPropertySpec());
        propertySpecs.add(doRouteRequestOnTopologyPropertySpec());

        propertySpecs.remove(ntaSimulationToolPropertySpec());
        propertySpecs.remove(manufacturerPropertySpec());
        propertySpecs.remove(fixMbusHexShortIdPropertySpec());
        propertySpecs.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        propertySpecs.remove(deviceId());
        propertySpecs.remove(masterKeyPropertySpec());

        return propertySpecs;
    }

    private PropertySpec updateIpv6OnTopologyPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(UPDATE_IPV6_ON_TOPOLOGY, false, PropertyTranslationKeys.V2_TOPOLOGY_UPDATE_IPV6, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec doPathRequestOnTopologyPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DO_PATH_REQUESTS_ON_TOPOLOGY, false, PropertyTranslationKeys.V2_TOPOLOGY_DO_PATH_REQUEST, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    private PropertySpec doRouteRequestOnTopologyPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DO_ROUTE_REQUESTS_ON_TOPOLOGY, false, PropertyTranslationKeys.V2_TOPOLOGY_DO_ROUTE_REQUEST, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    private PropertySpec ipv6AddressAndPrefixLength() {
        return UPLPropertySpecFactory.specBuilder(IPV6_ADDRESS_AND_PREFIX_LENGTH, false, PropertyTranslationKeys.IPV6_ADDRESS_AND_PREFIX_LENGTH, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec defaultBufferSizeRegisters() {
        return UPLPropertySpecFactory.specBuilder(DEFAULT_BUFFERSIZE_REGISTERS, false, PropertyTranslationKeys.V2_DEFAULT_BUFFERSIZE_REGISTERS, this.getPropertySpecService()::bigDecimalSpec).setDefaultValue(BigDecimal.valueOf(1)).finish();
    }

    private PropertySpec defaultBacklogEventLog() {
        return UPLPropertySpecFactory.specBuilder(DEFAULT_BACKLOG_EVENTLOG, false, PropertyTranslationKeys.V2_DEFAULT_BACKLOG_EVENTLOG, this.getPropertySpecService()::bigDecimalSpec).setDefaultValue(BigDecimal.valueOf(10)).finish();
    }

    private PropertySpec defaultBacklogLoadProfile() {
        return UPLPropertySpecFactory.specBuilder(DEFAULT_BACKLOG_LOADPROFILE, false, PropertyTranslationKeys.V2_DEFAULT_BACKLOG_LOADPROFILE, this.getPropertySpecService()::bigDecimalSpec).setDefaultValue(BigDecimal.valueOf(10)).finish();
    }

    private PropertySpec readOldObisCodes() {
        return UPLPropertySpecFactory.specBuilder(READ_OLD_OBIS_CODES, false, PropertyTranslationKeys.V2_READ_OLD_OBIS_CODES, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec hasPre20Firmware() {
        return UPLPropertySpecFactory.specBuilder(PRE_2_0_FIRMWARE, false, PropertyTranslationKeys.V2_PRE_2_0_FIRMWARE, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec frameCounterRecoveryRetries() {
        return UPLPropertySpecFactory.specBuilder(FRAME_COUNTER_RECOVERY_RETRIES, false, PropertyTranslationKeys.V2_FRAME_COUNTER_RECOVERY_RETRIES, this.getPropertySpecService()::bigDecimalSpec).setDefaultValue(BigDecimal.valueOf(100)).finish();
    }

    private PropertySpec frameCounterRecoveryStep() {
        return UPLPropertySpecFactory.specBuilder(FRAME_COUNTER_RECOVERY_STEP, false, PropertyTranslationKeys.V2_FRAME_COUNTER_RECOVERY_STEP, this.getPropertySpecService()::bigDecimalSpec).setDefaultValue(BigDecimal.ONE).finish();
    }

    private PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(VALIDATE_CACHED_FRAMECOUNTER, false, PropertyTranslationKeys.V2_VALIDATE_CACHED_FRAMECOUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_USE_CACHED_FRAME_COUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec initialFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(INITIAL_FRAME_COUNTER, false, PropertyTranslationKeys.V2_INITIAL_FRAME_COUNTER, this.getPropertySpecService()::positiveBigDecimalSpec).finish();
    }

    /**
     * Property spec indicating whether or not to increment the FC for the reply to HLS.
     *
     * @return	The corresponding PropertySpec.
     */
    private PropertySpec increaseFrameCounterOnHLSReply() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false, PropertyTranslationKeys.V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, getPropertySpecService()::booleanSpec).setDefaultValue(false).finish();
    }

    private PropertySpec frameCounterLimit() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.FRAME_COUNTER_LIMIT, false, PropertyTranslationKeys.V2_NTA_FRAME_COUNTER_LIMIT, getPropertySpecService()::positiveBigDecimalSpec)
                .setDefaultValue(new BigDecimal(0)).finish();
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(REQUEST_AUTHENTICATED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_EICT_REQUEST_AUTHENTCATED_FRAME_COUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ZERO, PropertyTranslationKeys.V2_EICT_POLLING_DELAY);
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return this.hexStringSpecOfExactLength(IDIS.CALLING_AP_TITLE, false, 16, PropertyTranslationKeys.V2_EICT_CALLING_AP_TITLE);
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, PropertyTranslationKeys.V2_EICT_CLIENT_PRIVATE_SIGNING_KEY);
    }

    /**
     * The TLS certificate of the server. Not actively used in the protocols.
     */
    private PropertySpec serverTLSCertificate() {
        return this.keyAccessorTypeReferenceSpec(DlmsSessionProperties.SERVER_TLS_CERTIFICATE, PropertyTranslationKeys.V2_EICT_SERVER_TLS_CERTIFICATE);
    }

    /**
     * The private key of the client (the ComServer) used for key agreement (ECDH)
     */
    private PropertySpec clientPrivateKeyAgreementKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY, PropertyTranslationKeys.V2_EICT_CLIENT_PRIVATE_KEY_AGREEMENT_KEY);
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
        return this.keyAccessorTypeReferenceSpec(DLMS_WAN_KEK, PropertyTranslationKeys.V2_EICT_DLMS_WAN_KEK);
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
        return this.keyAccessorTypeReferenceSpec(DLMS_METER_KEK, PropertyTranslationKeys.V2_EICT_DLMS_METER_KEK);
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(PSK_ENCRYPTION_KEY, PropertyTranslationKeys.V2_EICT_PSK_ENCRYPTION_KEY);
    }

    private PropertySpec deviceSystemTitlePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE, false, PropertyTranslationKeys.V2_EICT_DLMS_DEVICE_SYSTEM_TITLE, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec broadcastAuthenticationKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(BROADCAST_AUTHENTICATION_KEY, PropertyTranslationKeys.V2_BROADCAST_AUTHENTICATION_KEY);
    }

    private PropertySpec broadcastEncryptionKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(BROADCAST_ENCRYPTION_KEY, PropertyTranslationKeys.V2_BROADCAST_ENCRYPTION_KEY);
    }

    private PropertySpec useGeneralBlockTransferPropertySpec() {
        return this.booleanSpec(USE_GBT, USE_GBT_DEFAULT_VALUE, PropertyTranslationKeys.V2_DLMS_USE_GBT);
    }

    private PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return this.bigDecimalSpec(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE, PropertyTranslationKeys.V2_DLMS_GBT_WINDOW_SIZE);
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }

    private <T> PropertySpec spec(String name, boolean required, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, translationKey, optionsSupplier).finish();
    }

    private PropertySpec hexStringSpecOfExactLength(String name, boolean required, int length, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, () -> this.getPropertySpecService().hexStringSpecOfExactLength(length));
    }

    private PropertySpec stringSpec(String name, boolean required, TranslationKey translationKey, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    private PropertySpec keyAccessorTypeReferenceSpec(String name, PropertyTranslationKeys translationKey) {
        return getPropertySpecService()
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }
}