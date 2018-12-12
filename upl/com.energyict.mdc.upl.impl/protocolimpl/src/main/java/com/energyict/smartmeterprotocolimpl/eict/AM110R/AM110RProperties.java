package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class AM110RProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final int DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS = 64;
    private static final String DEFAULT_AM110R_HUB_LOGICAL_DEVICE_ADDRESS = "1";
    private static final int MAX_RECEIVE_PDU_SIZE = 4096;
    private static final boolean DEFAULT_Z_3_BULK_REQUES_SUPPORT = true;
    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final boolean DEFAULT_VERIFY_FIRMWARE_VERSION = false;
    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final int DEFAULT_LOGBOOK_SELECTOR = -1;
    private static final String ZIGBEE_LINK_KEY = "ZigbeeLinkKey";

    protected SecurityProvider securityProvider;

    public AM110RProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @ProtocolProperty
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
                this.integerSpec(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE),
                this.stringSpec(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS),
                this.integerSpec(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION),
                this.integerSpec(PK_FORCED_DELAY, false, PropertyTranslationKeys.EICT_FORCED_DELAY),
                this.integerSpec(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE),
                this.integerSpec(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES),
                this.integerSpec(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT),
                this.integerSpec(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION),
                this.integerSpec(VERIFY_FIRMWARE_VERSION, false, PropertyTranslationKeys.EICT_VERIFY_FIRMWARE_VERSION),
                this.integerSpec(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.EICT_LOGBOOK_SELECTOR),
                this.keyAccessorTypeReferencePropertySpec(ZIGBEE_LINK_KEY, PropertyTranslationKeys.EICT_ZIGBEE_LINK_KEY))
            .forEach(propertySpecs::add);
        return propertySpecs;
    }

    private PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return getPropertySpecService()
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AM110R_HUB_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MAX_RECEIVE_PDU_SIZE);
    }

    public boolean verifyFirmwareVersion() {
        return getBooleanProperty(VERIFY_FIRMWARE_VERSION, DEFAULT_VERIFY_FIRMWARE_VERSION);
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DEFAULT_Z_3_BULK_REQUES_SUPPORT);
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == MultipleClientRelatedObisCodes.FIRMWARE_CLIENT.getClientId();
    }

    public String getZigbeeLinkKey() {
        return getStringValue(ZIGBEE_LINK_KEY, "");
    }

    public void setSecurityProvider(AM110RSecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new AM110RSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }
}