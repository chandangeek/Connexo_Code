package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class AM110RProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final String DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS = "64";
    private static final String DEFAULT_AM110R_HUB_LOGICAL_DEVICE_ADDRESS = "1";
    private static final String MaxReceivePduSize = "4096";
    private static final String DefaultZ3BulkRequesSupport = "1";
    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final String DEFAULT_VERIFY_FIRMWARE_VERSION = "0";
    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    protected SecurityProvider securityProvider;

    public AM110RProperties() {
        super(propertySpecService);
    }

    @ProtocolProperty
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, true),
                UPLPropertySpecFactory.integer(ADDRESSING_MODE, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer(PK_FORCED_DELAY, false),
                UPLPropertySpecFactory.integer(MAX_REC_PDU_SIZE, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(ROUND_TRIP_CORRECTION, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false),
                UPLPropertySpecFactory.integer(VERIFY_FIRMWARE_VERSION, false),
                UPLPropertySpecFactory.integer(LOGBOOK_SELECTOR, false))
            .forEach(propertySpecs::add);
        return propertySpecs;
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
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    public boolean verifyFirmwareVersion() {
        return getBooleanProperty(VERIFY_FIRMWARE_VERSION, DEFAULT_VERIFY_FIRMWARE_VERSION);
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DefaultZ3BulkRequesSupport);
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == MultipleClientRelatedObisCodes.FIRMWARE_CLIENT.getClientId();
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