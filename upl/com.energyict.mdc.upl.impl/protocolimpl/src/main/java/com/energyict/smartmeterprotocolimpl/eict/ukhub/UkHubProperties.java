package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
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
                this.integerSpec(SECURITY_LEVEL, true),
                this.integerSpec(ADDRESSING_MODE, false),
                this.integerSpec(CLIENT_MAC_ADDRESS, false),
                this.stringSpec(SERVER_MAC_ADDRESS, false),
                this.integerSpec(CONNECTION, false),
                this.integerSpec(PK_FORCED_DELAY, false),
                this.integerSpec(PK_DELAY_AFTER_ERROR, false),
                this.integerSpec(INFORMATION_FIELD_SIZE, false),
                this.integerSpec(MAX_REC_PDU_SIZE, false),
                this.integerSpec(PK_RETRIES, false),
                this.integerSpec(PK_TIMEOUT, false),
                this.integerSpec(ROUND_TRIP_CORRECTION, false),
                this.integerSpec(BULK_REQUEST, false),
                this.integerSpec(CIPHERING_TYPE, false),
                this.integerSpec(NTA_SIMULATION_TOOL, false),
                this.integerSpec(LOGBOOK_SELECTOR, false),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, false),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, false));
    }

    private <T> PropertySpec spec(String name, boolean required, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, boolean required) {
        return this.spec(name, required, this.propertySpecService::stringSpec);
    }

    private PropertySpec hexStringSpec(String name, boolean required) {
        return this.spec(name, required, this.propertySpecService::hexStringSpec);
    }

    private PropertySpec integerSpec(String name, boolean required) {
        return this.spec(name, required, this.propertySpecService::integerSpec);
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