package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.Bus;

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
        return Bus.getPropertySpecService().basicPropertySpec(SECURITY_LEVEL_PROPERTY_NAME, true, new StringFactory());
    }

    private PropertySpec addressingModePropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(ADDRESSING_MODE_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec clientMacAddressPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(CLIENT_MAC_ADDRESS_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec serverMacAddressPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(SERVER_MAC_ADDRESS_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec connectionPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(CONNECTION_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec forcedDelayPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(FORCED_DELAY_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(INFORMATION_FIELD_SIZE_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(MAX_REC_PDU_SIZE_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec retriesPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(RETRIES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec timeoutPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(TIMEOUT_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(ROUND_TRIP_CORRECTION_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec bulkRequestPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(BULK_REQUEST_PROPERTY_NAME, false, new BooleanFactory());
    }

    private PropertySpec cipheringTypePropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(CIPHERING_TYPE_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec ntaSimulationToolPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(NTA_SIMULATION_TOOL_PROPERTY_NAME, false, new BooleanFactory());
    }

    private PropertySpec dataTransportAuthenticationKeyPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(DATATRANSPORT_AUTHENTICATIONKEY_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec newDataTransportAuthenticationKeyPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(NEW_DATATRANSPORT_AUTHENTICATION_KEY_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec dataTransportEncryptionKeyPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(DATATRANSPORT_ENCRYPTIONKEY_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec newDataTransportEncryptionKeyPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(NEW_DATATRANSPORT_ENCRYPTION_KEY_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec newHlsSecretPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(NEW_HLS_SECRET_PROPERTY_NAME, false, new StringFactory());
    }

    private PropertySpec wakeUpPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(WAKE_UP_PROPERTY_NAME, false, new BooleanFactory());
    }

    private PropertySpec oldMbusDiscoverySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(OLD_MBUS_DISCOVERY, false, new BooleanFactory());
    }

    private PropertySpec fixMbusHexShortIdSpec() {
        return Bus.getPropertySpecService().basicPropertySpec(FIX_MBUS_HEX_SHORT_ID, false, new BooleanFactory());
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