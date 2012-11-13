package test.com.energyict.mdc.tasks;

import com.energyict.cpo.BigDecimalPropertySpec;
import com.energyict.cpo.BooleanPropertySpec;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.StringPropertySpec;
import com.energyict.mdc.tasks.DeviceProtocolDialectImpl;
import test.com.energyict.dlms.common.DlmsProtocolProperties;
import test.com.energyict.protocolimplV2.nta.abstractnta.NTASecurityProvider;

import java.util.Arrays;
import java.util.List;

/**
* Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for the CTR protocol
*
* @author: sva
* @since: 16/10/12 (113:25)
*/
public class Dsmr23DeviceProtocolDialect extends DeviceProtocolDialectImpl {

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

    private PropertySpec securityLevelPropertySpec() {
        return new StringPropertySpec(SECURITY_LEVEL_PROPERTY_NAME);
    }

    private PropertySpec addressingModePropertySpec() {
        return new BigDecimalPropertySpec(ADDRESSING_MODE_PROPERTY_NAME);
    }

    private PropertySpec clientMacAddressPropertySpec() {
        return new BigDecimalPropertySpec(CLIENT_MAC_ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec serverMacAddressPropertySpec() {
        return new StringPropertySpec(SERVER_MAC_ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec connectionPropertySpec() {
        return new BigDecimalPropertySpec(CONNECTION_PROPERTY_NAME);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return new BigDecimalPropertySpec(FORCED_DELAY_PROPERTY_NAME);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return new BigDecimalPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME);
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return new BigDecimalPropertySpec(INFORMATION_FIELD_SIZE_PROPERTY_NAME);
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return new BigDecimalPropertySpec(MAX_REC_PDU_SIZE_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return new BigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    private PropertySpec timeoutPropertySpec() {
        return new BigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return new BigDecimalPropertySpec(ROUND_TRIP_CORRECTION_PROPERTY_NAME);
    }

    private PropertySpec bulkRequestPropertySpec() {
        return new BooleanPropertySpec(BULK_REQUEST_PROPERTY_NAME);
    }

    private PropertySpec cipheringTypePropertySpec() {
        return new BigDecimalPropertySpec(CIPHERING_TYPE_PROPERTY_NAME);
    }

    private PropertySpec ntaSimulationToolPropertySpec() {
        return new BooleanPropertySpec(NTA_SIMULATION_TOOL_PROPERTY_NAME);
    }

    private PropertySpec dataTransportAuthenticationKeyPropertySpec() {
        return new StringPropertySpec(DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME);
    }

    private PropertySpec newDataTransportAuthenticationKeyPropertySpec() {
        return new StringPropertySpec(NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME);
    }

    private PropertySpec dataTransportEncryptionKeyPropertySpec() {
        return new StringPropertySpec(DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME);
    }

    private PropertySpec newDataTransportEncryptionKeyPropertySpec() {
        return new StringPropertySpec(NEW_DATATRANSPORT_ENCRYPTION_KEY_PROPERTY_NAME);
    }

    private PropertySpec newHlsSecretPropertySpec() {
        return new StringPropertySpec(NEW_HLS_SECRET_PROPERTY_NAME);
    }

    private PropertySpec wakeUpPropertySpec() {
        return new BooleanPropertySpec(WAKE_UP_PROPERTY_NAME);
    }

    private PropertySpec oldMbusDiscoverySpec() {
        return new BooleanPropertySpec(OLD_MBUS_DISCOVERY);
    }

    private PropertySpec fixMbusHexShortIdSpec() {
        return new BooleanPropertySpec(FIX_MBUS_HEX_SHORT_ID);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (SECURITY_LEVEL_PROPERTY_NAME.equals(name)) {
            return this.securityLevelPropertySpec();
        } else if (ADDRESSING_MODE_PROPERTY_NAME.equals(name)) {
            return this.addressingModePropertySpec();
        } else if (CLIENT_MAC_ADDRESS_PROPERTY_NAME.equals(name)) {
            return this.clientMacAddressPropertySpec();
        } else if (SERVER_MAC_ADDRESS_PROPERTY_NAME.equals(name)) {
            return this.serverMacAddressPropertySpec();
        } else if (CONNECTION_PROPERTY_NAME.equals(name)) {
            return this.connectionPropertySpec();
        } else if (FORCED_DELAY_PROPERTY_NAME.equals(name)) {
            return this.forcedDelayPropertySpec();
        } else if (DELAY_AFTER_ERROR_PROPERTY_NAME.equals(name)) {
            return this.delayAfterErrorPropertySpec();
        } else if (INFORMATION_FIELD_SIZE_PROPERTY_NAME.equals(name)) {
            return this.informationFieldSizePropertySpec();
        } else if (MAX_REC_PDU_SIZE_PROPERTY_NAME.equals(name)) {
            return this.maxRecPduSizePropertySpec();
        } else if (RETRIES_PROPERTY_NAME.equals(name)) {
            return this.retriesPropertySpec();
        } else if (TIMEOUT_PROPERTY_NAME.equals(name)) {
            return this.timeoutPropertySpec();
        } else if (ROUND_TRIP_CORRECTION_PROPERTY_NAME.equals(name)) {
            return this.roundTripCorrectionPropertySpec();
        } else if (BULK_REQUEST_PROPERTY_NAME.equals(name)) {
            return this.bulkRequestPropertySpec();
        } else if (CIPHERING_TYPE_PROPERTY_NAME.equals(name)) {
            return this.cipheringTypePropertySpec();
        } else if (NTA_SIMULATION_TOOL_PROPERTY_NAME.equals(name)) {
            return this.ntaSimulationToolPropertySpec();
        } else if (DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME.equals(name)) {
            return this.dataTransportAuthenticationKeyPropertySpec();
        } else if (NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME.equals(name)) {
            return this.newDataTransportAuthenticationKeyPropertySpec();
        } else if (DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME.equals(name)) {
            return this.dataTransportEncryptionKeyPropertySpec();
        } else if (NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME.equals(name)) {
            return this.newDataTransportEncryptionKeyPropertySpec();
        } else if (NEW_HLS_SECRET_PROPERTY_NAME.equals(name)) {
            return this.newHlsSecretPropertySpec();
        } else if (WAKE_UP_PROPERTY_NAME.equals(name)) {
            return this.wakeUpPropertySpec();
        } else if (OLD_MBUS_DISCOVERY.equals(name)) {
            return this.oldMbusDiscoverySpec();
        } else if (FIX_MBUS_HEX_SHORT_ID.equals(name)) {
            return this.fixMbusHexShortIdSpec();
        } else {
            return null;
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(this.securityLevelPropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(this.addressingModePropertySpec(),
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