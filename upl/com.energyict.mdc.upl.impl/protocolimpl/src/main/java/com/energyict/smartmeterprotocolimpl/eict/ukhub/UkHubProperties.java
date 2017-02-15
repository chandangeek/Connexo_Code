package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class UkHubProperties extends DlmsProtocolProperties {

    private static final String DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS = "64";
    private static final String DEFAULT_UK_HUB_LOGICAL_DEVICE_ADDRESS = "1";
    private static final String MaxReceivePduSize = "4096";
    private static final String DefaultZ3BulkRequesSupport = "1";

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    public static final int FIRMWARE_CLIENT = 0x50;

    private SecurityProvider securityProvider;
    private final PropertySpecService propertySpecService;

    UkHubProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(SECURITY_LEVEL, true, PropertyTranslationKeys.EICT_SECURITY_LEVEL),
                this.integerSpec(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE),
                this.integerSpec(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS),
                this.stringSpec(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS),
                this.integerSpec(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION),
                this.integerSpec(PK_FORCED_DELAY, false, PropertyTranslationKeys.EICT_FORCED_DELAY),
                this.integerSpec(PK_DELAY_AFTER_ERROR, false, PropertyTranslationKeys.EICT_DELAY_AFTER_ERROR),
                this.integerSpec(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE),
                this.integerSpec(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE),
                this.integerSpec(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES),
                this.integerSpec(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT),
                this.integerSpec(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION),
                this.integerSpec(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST),
                this.integerSpec(CIPHERING_TYPE, false, PropertyTranslationKeys.EICT_CIPHERING_TYPE),
                this.integerSpec(NTA_SIMULATION_TOOL, false, PropertyTranslationKeys.EICT_NTA_SIMULATION_TOOL),
                this.integerSpec(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.EICT_LOGBOOK_SELECTOR),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY));
    }

    private <T> PropertySpec spec(String name, boolean required, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec hexStringSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::hexStringSpec);
    }

    private PropertySpec integerSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::integerSpec);
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_UK_HUB_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DefaultZ3BulkRequesSupport);
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    public void setSecurityProvider(final UkHubSecurityProvider ukHubSecurityProvider) {
        this.securityProvider = ukHubSecurityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(this.securityProvider == null){
            this.securityProvider = new UkHubSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}