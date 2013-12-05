package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.mdc.protocol.DeviceProtocolDialect;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.OptionalPropertySpecFactory;
import com.energyict.mdc.protocol.dynamic.impl.RequiredPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
* Models a {@link DeviceProtocolDialect} for the CTR protocol
*
* @author sva
* @since 16/10/12 (113:25)
*/
public class Dsmr23DeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    // Required properties
    public static final String SECURITY_LEVEL_PROPERTY_NAME = DlmsProtocolProperties.SECURITY_LEVEL;

    // Optional properties
    public static final String ADDRESSING_MODE_PROPERTY_NAME = DlmsProtocolProperties.ADDRESSING_MODE;
    public static final String CLIENT_MAC_ADDRESS_PROPERTY_NAME = DlmsProtocolProperties.CLIENT_MAC_ADDRESS;
    public static final String SERVER_MAC_ADDRESS_PROPERTY_NAME = DlmsProtocolProperties.SERVER_MAC_ADDRESS;
    public static final String CONNECTION_PROPERTY_NAME = DlmsProtocolProperties.CONNECTION;
    public static final String FORCED_DELAY_PROPERTY_NAME = DlmsProtocolProperties.FORCED_DELAY;
    public static final String DELAY_AFTER_ERROR_PROPERTY_NAME = DlmsProtocolProperties.DELAY_AFTER_ERROR;
    public static final String INFORMATION_FIELD_SIZE_PROPERTY_NAME = DlmsProtocolProperties.INFORMATION_FIELD_SIZE;
    public static final String MAX_REC_PDU_SIZE_PROPERTY_NAME = DlmsProtocolProperties.MAX_REC_PDU_SIZE;
    public static final String RETRIES_PROPERTY_NAME = DlmsProtocolProperties.RETRIES;
    public static final String TIMEOUT_PROPERTY_NAME = DlmsProtocolProperties.TIMEOUT;
    public static final String ROUND_TRIP_CORRECTION_PROPERTY_NAME = DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
    public static final String BULK_REQUEST_PROPERTY_NAME = DlmsProtocolProperties.BULK_REQUEST;
    public static final String CIPHERING_TYPE_PROPERTY_NAME = DlmsProtocolProperties.CIPHERING_TYPE;
    public static final String NTA_SIMULATION_TOOL_PROPERTY_NAME = DlmsProtocolProperties.NTA_SIMULATION_TOOL;
    public static final String DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME = NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
    public static final String DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME = NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;
    public static final String NEW_DATATRANSPORT_ENCRYPTION_KEY_PROPERTY_NAME = NTASecurityProvider.NEW_DATATRANSPORT_ENCRYPTION_KEY;
    public static final String NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME = NTASecurityProvider.NEW_DATATRANSPORT_AUTHENTICATION_KEY;
    public static final String NEW_HLS_SECRET_PROPERTY_NAME = NTASecurityProvider.NEW_HLS_SECRET;
    public static final String WAKE_UP_PROPERTY_NAME = DlmsProtocolProperties.WAKE_UP;
    public static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.DSMR23_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "DSRM 2.3";
    }

    private PropertySpec securityLevelPropertySpec() {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(SECURITY_LEVEL_PROPERTY_NAME);
    }

    private PropertySpec addressingModePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(ADDRESSING_MODE_PROPERTY_NAME);
    }

    private PropertySpec clientMacAddressPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(CLIENT_MAC_ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec serverMacAddressPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(SERVER_MAC_ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec connectionPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(CONNECTION_PROPERTY_NAME);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(FORCED_DELAY_PROPERTY_NAME);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME);
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(INFORMATION_FIELD_SIZE_PROPERTY_NAME);
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(MAX_REC_PDU_SIZE_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    private PropertySpec timeoutPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(ROUND_TRIP_CORRECTION_PROPERTY_NAME);
    }

    private PropertySpec bulkRequestPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(BULK_REQUEST_PROPERTY_NAME);
    }

    private PropertySpec cipheringTypePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(CIPHERING_TYPE_PROPERTY_NAME);
    }

    private PropertySpec ntaSimulationToolPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(NTA_SIMULATION_TOOL_PROPERTY_NAME);
    }

    private PropertySpec dataTransportAuthenticationKeyPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME);
    }

    private PropertySpec newDataTransportAuthenticationKeyPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME);
    }

    private PropertySpec dataTransportEncryptionKeyPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME);
    }

    private PropertySpec newDataTransportEncryptionKeyPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(NEW_DATATRANSPORT_ENCRYPTION_KEY_PROPERTY_NAME);
    }

    private PropertySpec newHlsSecretPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(NEW_HLS_SECRET_PROPERTY_NAME);
    }

    private PropertySpec wakeUpPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(WAKE_UP_PROPERTY_NAME);
    }

    private PropertySpec oldMbusDiscoverySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(OLD_MBUS_DISCOVERY);
    }

    private PropertySpec fixMbusHexShortIdSpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(FIX_MBUS_HEX_SHORT_ID);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case SECURITY_LEVEL_PROPERTY_NAME:
                return this.securityLevelPropertySpec();
            case ADDRESSING_MODE_PROPERTY_NAME:
                return this.addressingModePropertySpec();
            case CLIENT_MAC_ADDRESS_PROPERTY_NAME:
                return this.clientMacAddressPropertySpec();
            case SERVER_MAC_ADDRESS_PROPERTY_NAME:
                return this.serverMacAddressPropertySpec();
            case CONNECTION_PROPERTY_NAME:
                return this.connectionPropertySpec();
            case FORCED_DELAY_PROPERTY_NAME:
                return this.forcedDelayPropertySpec();
            case DELAY_AFTER_ERROR_PROPERTY_NAME:
                return this.delayAfterErrorPropertySpec();
            case INFORMATION_FIELD_SIZE_PROPERTY_NAME:
                return this.informationFieldSizePropertySpec();
            case MAX_REC_PDU_SIZE_PROPERTY_NAME:
                return this.maxRecPduSizePropertySpec();
            case RETRIES_PROPERTY_NAME:
                return this.retriesPropertySpec();
            case TIMEOUT_PROPERTY_NAME:
                return this.timeoutPropertySpec();
            case ROUND_TRIP_CORRECTION_PROPERTY_NAME:
                return this.roundTripCorrectionPropertySpec();
            case BULK_REQUEST_PROPERTY_NAME:
                return this.bulkRequestPropertySpec();
            case CIPHERING_TYPE_PROPERTY_NAME:
                return this.cipheringTypePropertySpec();
            case NTA_SIMULATION_TOOL_PROPERTY_NAME:
                return this.ntaSimulationToolPropertySpec();
            case DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME:
                return this.dataTransportAuthenticationKeyPropertySpec();
            case DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME:
                return this.dataTransportEncryptionKeyPropertySpec();
            case NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME:
                return this.newDataTransportEncryptionKeyPropertySpec();
            case NEW_HLS_SECRET_PROPERTY_NAME:
                return this.newHlsSecretPropertySpec();
            case WAKE_UP_PROPERTY_NAME:
                return this.wakeUpPropertySpec();
            case OLD_MBUS_DISCOVERY:
                return this.oldMbusDiscoverySpec();
            case FIX_MBUS_HEX_SHORT_ID:
                return this.fixMbusHexShortIdSpec();
            default:
                return null;
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Arrays.asList(
                this.securityLevelPropertySpec(),
                this.addressingModePropertySpec(),
                this.clientMacAddressPropertySpec(),
                this.serverMacAddressPropertySpec(),
                this.connectionPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.delayAfterErrorPropertySpec(),
                this.informationFieldSizePropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.roundTripCorrectionPropertySpec(),
                this.bulkRequestPropertySpec(),
                this.cipheringTypePropertySpec(),
                this.ntaSimulationToolPropertySpec(),
                this.dataTransportAuthenticationKeyPropertySpec(),
                this.newDataTransportAuthenticationKeyPropertySpec(),
                this.dataTransportEncryptionKeyPropertySpec(),
                this.newDataTransportEncryptionKeyPropertySpec(),
                this.newHlsSecretPropertySpec(),
                this.wakeUpPropertySpec(),
                this.oldMbusDiscoverySpec(),
                this.fixMbusHexShortIdSpec());
    }

}