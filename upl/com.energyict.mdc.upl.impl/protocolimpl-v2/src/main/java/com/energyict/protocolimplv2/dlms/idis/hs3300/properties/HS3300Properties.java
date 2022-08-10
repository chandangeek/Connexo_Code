package com.energyict.protocolimplv2.dlms.idis.hs3300.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

import java.math.BigDecimal;
import java.time.Duration;

import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

public class HS3300Properties extends IDISProperties {

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private CertificateWrapperExtractor certificateWrapperExtractor;
    private Integer securitySuite = null;

    public HS3300Properties(PropertySpecService propertySpecService, NlsService nlsService, CertificateWrapperExtractor certificateWrapperExtractor) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    public byte[] getSystemIdentifier() {
        // Property CallingAPTitle is used as system identifier in the AARQ
        final String callingAPTitle = getProperties().getTypedProperty(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE_DEFAULT).trim();
        if (callingAPTitle.isEmpty()) {
            return super.getSystemIdentifier();
        } else {
            try {
                return ProtocolTools.getBytesFromHexString(callingAPTitle, "");
            } catch (Throwable e) {
                throw DeviceConfigurationException.invalidPropertyFormat(IDIS.CALLING_AP_TITLE, callingAPTitle, "Should be a hex string of 16 characters");
            }
        }
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new HS3300SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), getSecuritySuite(), certificateWrapperExtractor);
        }
        return securityProvider;
    }

    @Override
    public int getSecuritySuite() {
        if (securitySuite == null) {
            securitySuite = doGetSecuritySuite();
        }
        return securitySuite;
    }

    public void setSecuritySuite(int securitySuite) {
        this.securitySuite = securitySuite;
        ((HS3300SecurityProvider) getSecurityProvider()).setSecuritySuite(securitySuite);
    }

    private int doGetSecuritySuite() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
            return advancedSecurityPropertySet.getSecuritySuite();
        } else {
            return 0;
        }
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();

        conformanceBlock.setGeneralBlockTransfer(useGeneralBlockTransfer());
        conformanceBlock.setGeneralProtection(getCipheringType().equals(CipheringType.GENERAL_DEDICATED) || getCipheringType().equals(CipheringType.GENERAL_GLOBAL));

        conformanceBlock.setGeneralProtection(true);
        conformanceBlock.setAccess(true);
        conformanceBlock.setDataNotification(true);
        conformanceBlock.setAction(true);
        conformanceBlock.setPriorityManagementSupported(false);
        conformanceBlock.setEventNotification(false);

        return conformanceBlock;
    }

    /**
     * The AM540 protocol will also run embedded in the Beacon3100, so by default: avoid polling on the inputstream
     */
    @Override
    public Duration getPollingDelay() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.POLLING_DELAY, Duration.ofSeconds(0));
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, HS3300ConfigurationSupport.DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    @Override
    public int getServerUpperMacAddress() {
        if (useBeaconMirrorDeviceDialect()) {
            return getMirrorLogicalDeviceId();  // The Beacon mirrored device
        } else if (useBeaconGatewayDeviceDialect()) {
            return getGatewayLogicalDeviceId(); // Beacon acts as a gateway
        } else {
            return getNodeAddress();            // Classic G3 gateway
        }
    }

    protected int getMirrorLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(HS3300ConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(HS3300ConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    protected int getGatewayLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(HS3300ConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(HS3300ConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    public int getNodeAddress() {
        return parseBigDecimalProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
    }

    /**
     * False by default, to return the serial number of the connected e-meter
     */
    @Override
    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, HS3300ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        if (dialectName == null) {
            return false;
        }
        MirrorTcpDeviceProtocolDialect dialect = new MirrorTcpDeviceProtocolDialect(this.propertySpecService, this.nlsService);
        // for compatibility with ProtocolTester - here the protocol dialect is the "display name"
        return dialect.getDeviceProtocolDialectDisplayName().equals(dialectName) || dialect.getDeviceProtocolDialectName().equals(dialectName);
    }

    public boolean useBeaconGatewayDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.AARQ_TIMEOUT_PROPERTY, HS3300ConfigurationSupport.DEFAULT_NOT_USED_AARQ_TIMEOUT).toMillis();
    }

    /**
     * A timeout (lack of response from the AM540) should be handled differently according to the context:
     * - in case of G3 gateway mode, you can still read out the next physical slave devices if one slave device does not reply.
     * - in case of Beacon DC mode (reading out 'mirror' logical devices), a timeout is fatal, the next physical slaves cannot be read out.
     * - in case of a serial connection we should fail on a timeout
     */
    @Override
    public boolean timeoutMeansBrokenConnection() {
        return useBeaconMirrorDeviceDialect() || useSerialDialect();
    }

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public long getInitialFrameCounter() {
        return getProperties().getTypedProperty(HS3300ConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(100)).longValue();
    }

    public long getFrameCounterLimit() {
        return getProperties().getTypedProperty(DlmsProtocolProperties.FRAME_COUNTER_LIMIT, BigDecimal.valueOf(0)).longValue();
    }

    /**
     * Indicates whether or not to skip the frame counter authentication tag check.
     *
     * @return    <code>true</code> if this needs to be skipped, <code>false</code> otherwise.
     */
    public final boolean skipFramecounterAuthenticationTag() {
        return this.getProperties().<Boolean>getTypedProperty(HS3300ConfigurationSupport.SKIP_FC_AUTH_TAG_VALIDATION, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ProtocolProperty
    public final CipheringType getCipheringType() {
        final String cipheringDescription = getProperties().getTypedProperty(DlmsProtocolProperties.CIPHERING_TYPE, HS3300ConfigurationSupport.DEFAULT_CIPHERING_TYPE.getDescription());

        for (CipheringType cipheringType : CipheringType.values()) {
            if (cipheringType.getDescription().equals(cipheringDescription)) {
                return cipheringType;
            }
        }

        return HS3300ConfigurationSupport.DEFAULT_CIPHERING_TYPE;
    }

    /**
     * The security policy of suite 1 and 2 are not backwards compatible.
     * It is now a byte where every bit is a flag:
     * 0 unused, shall be set to 0,
     * 1 unused, shall be set to 0,
     * 2 authenticated request,
     * 3 encrypted request,
     * 4 digitally signed request,
     * 5 authenticated response,
     * 6 encrypted response,
     * 7 digitally signed response
     */
    @Override
    protected int doGetDataTransportSecurityLevel() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            if (getSecuritySuite() <= 0) {
                //Suite 0 uses the old field, EncryptionDeviceAccessLevel. It is either 0, 1, 2 or 3.
                return super.doGetDataTransportSecurityLevel();
            } else {
                AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
                int result = 0;
                result |= advancedSecurityPropertySet.getRequestSecurityLevel() << 2;
                result |= advancedSecurityPropertySet.getResponseSecurityLevel() << 5;
                return result;
            }
        } else {
            return super.doGetDataTransportSecurityLevel();
        }
    }

    protected CertificateWrapperExtractor getCertificateWrapperExtractor() {
        return certificateWrapperExtractor;
    }

}
