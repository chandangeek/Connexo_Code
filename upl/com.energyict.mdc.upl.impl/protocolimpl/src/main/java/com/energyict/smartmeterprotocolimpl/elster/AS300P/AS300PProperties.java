package com.energyict.smartmeterprotocolimpl.elster.AS300P;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:16:15
 */
public class AS300PProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final String DEFAULT_AS300_CLIENT_MAC_ADDRESS = "64";
    private static final String DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS = "45";

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final String DEFAULT_VERIFY_FIRMWARE_VERSION = "0";

    private static final int FIRMWARE_CLIENT = 0x50;
    private static final String MaxReceivePduSize_Optical = "276";
    private static final String MaxReceivePduSize_TCP_IP = "1070";

    public AS300PProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
                this.integerSpec(SECURITY_LEVEL, true, PropertyTranslationKeys.ELSTER_SECURITYLEVEL),
                this.integerSpec(ADDRESSING_MODE, false, PropertyTranslationKeys.ELSTER_ADDRESSING_MODE),
                this.integerSpec(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.ELSTER_CLIENT_MAC_ADDRESS),
                this.stringSpec(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.ELSTER_SERVER_MAC_ADDRESS),
                this.integerSpec(CONNECTION, false, PropertyTranslationKeys.ELSTER_CONNECTION),
                this.integerSpec(PK_FORCED_DELAY, false, PropertyTranslationKeys.ELSTER_FORCED_DELAY),
                this.integerSpec(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.ELSTER_MAX_REC_PDU_SIZE),
                this.integerSpec(PK_RETRIES, false, PropertyTranslationKeys.ELSTER_RETRIES),
                this.integerSpec(PK_TIMEOUT, false, PropertyTranslationKeys.ELSTER_TIMEOUT),
                this.integerSpec(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.ELSTER_ROUND_TRIP_CORRECTION),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.ELSTER_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.ELSTER_DATATRANSPORT_ENCRYPTIONKEY),
                this.integerSpec(VERIFY_FIRMWARE_VERSION, false, PropertyTranslationKeys.ELSTER_VERIFY_FIRMWARE_VERSION),
                this.integerSpec(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.ELSTER_LOGBOOK_SELECTOR))
                .forEach(propertySpecs::add);
        return propertySpecs;
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_AS300_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "1");
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (super.securityProvider == null) {
            setSecurityProvider(new AM110RSecurityProvider(getProtocolProperties()));
        }
        return super.securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        super.securityProvider = securityProvider;
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        if (getConnectionMode().equals(ConnectionMode.HDLC)) {
            return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize_Optical);
        } else {
            return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize_TCP_IP);
        }
    }

    public boolean verifyFirmwareVersion() {
        return getBooleanProperty(VERIFY_FIRMWARE_VERSION, DEFAULT_VERIFY_FIRMWARE_VERSION);
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

}