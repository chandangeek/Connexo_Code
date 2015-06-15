package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Class that holds all DLMS device properties (general, dialect & security related)
 * Based on these properties, a DLMS session and its connection layer can be fully configured.
 * <p/>
 * The list of optional and required properties that is shown in EIServer is held in {@link DlmsConfigurationSupport}
 * <p/>
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class DlmsProperties implements DlmsSessionProperties {

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private String serialNumber = "";
    protected SecurityProvider securityProvider;

    public DlmsProperties() {
        this.properties = TypedProperties.empty();
    }

    public void addProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    public TypedProperties getProperties() {
        return properties;
    }

    /**
     * The device timezone
     */
    @Override
    public TimeZone getTimeZone() {
        TimeZoneInUse timeZoneInUse = properties.<TimeZoneInUse>getTypedProperty(TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZoneInUse.getTimeZone();
        }
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet securityPropertySet) {
        this.securityPropertySet = securityPropertySet;
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    @Override
    public int getAuthenticationSecurityLevel() {
        return securityPropertySet.getAuthenticationDeviceAccessLevel();
    }

    @Override
    public int getDataTransportSecurityLevel() {
        return securityPropertySet.getEncryptionDeviceAccessLevel();
    }

    @Override
    public int getClientMacAddress() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), BigDecimal.ONE);
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(SERVER_UPPER_MAC_ADDRESS, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, DEFAULT_LOWER_SERVER_MAC_ADDRESS);
    }

    @Override
    public int getAddressingMode() {
        return parseBigDecimalProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public String getManufacturer() {
        return properties.<String>getTypedProperty(MANUFACTURER, DEFAULT_MANUFACTURER);
    }

    @Override
    public int getInformationFieldSize() {
        return parseBigDecimalProperty(INFORMATION_FIELD_SIZE, DEFAULT_INFORMATION_FIELD_SIZE);
    }

    @Override
    public boolean isWakeUp() {
        return properties.<Boolean>getTypedProperty(WAKE_UP, DEFAULT_WAKE_UP);
    }

    @Override
    public String getDeviceId() {
        return properties.<String>getTypedProperty(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    @Override
    public CipheringType getCipheringType() {
        return CipheringType.fromValue(parseBigDecimalProperty(CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE));
    }

    @Override
    public boolean isNtaSimulationTool() {
        return properties.<Boolean>getTypedProperty(NTA_SIMULATION_TOOL, DEFAULT_NTA_SIMULATION_TOOL);
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        BigDecimal defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_LN;
        if (getReference().equals(DLMSReference.SN)) {
            defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_SN;
        }
        return new ConformanceBlock(parseBigDecimalProperty(CONFORMANCE_BLOCK_VALUE, defaultValue));
    }

    @Override
    public byte[] getSystemIdentifier() {
        return DEFAULT_SYSTEM_IDENTIFIER.getBytes();
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriority = (byte) (DEFAULT_INVOKE_ID_AND_PRIORITY.intValue());
        if (properties.<Boolean>getTypedProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) {
            return new IncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        } else {
            return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        }
    }

    @Override
    public int getMaxRecPDUSize() {
        return parseBigDecimalProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    @Override
    public int getProposedDLMSVersion() {
        return DEFAULT_PROPOSED_DLMS_VERSION.intValue();
    }

    @Override
    public int getProposedQOS() {
        return DEFAULT_PROPOSED_QOS.intValue();
    }

    @Override
    public boolean isRequestTimeZone() {
        return properties.<Boolean>getTypedProperty(REQUEST_TIMEZONE, DEFAULT_REQUEST_TIMEZONE);
    }

    @Override
    public int getRoundTripCorrection() {
        return parseBigDecimalProperty(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    /**
     * Property that swaps the client and server address in the TCP header.
     * Other protocols can override this value if necessary.
     */
    @Override
    public boolean isSwitchAddresses() {
        return true;
    }

    /**
     * Sets the type of SNRM frame in the HDLC connection.
     * Other protocols can override this value if necessary.
     */
    @Override
    public int getSNRMType() {
        return 0;
    }

    /**
     * Default 5 = 9600 baud
     * Other protocols can override this value if necessary.
     */
    @Override
    public int getHHUSignonBaudRateCode() {
        return 5;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new NTASecurityProvider(properties, securityPropertySet.getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public long getTimeout() {
        return properties.<TimeDuration>getTypedProperty(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT.intValue() / 1000)).getMilliSeconds();
    }

    @Override
    public int getRetries() {
        return parseBigDecimalProperty(RETRIES, DEFAULT_RETRIES);
    }

    @Override
    public long getForcedDelay() {
        return properties.<TimeDuration>getTypedProperty(FORCED_DELAY, new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000)).getMilliSeconds();
    }

    @Override
    public boolean getFixMbusHexShortId() {
        return properties.<Boolean>getTypedProperty(FIX_MBUS_HEX_SHORT_ID, DEFAULT_FIX_MBUS_HEX_SHORT_ID);
    }

    protected int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return properties.<BigDecimal>getTypedProperty(key, defaultValue).intValue();
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return properties.<Boolean>getTypedProperty(USE_GBT, DEFAULT_ENABLE_GBT);
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return properties.getTypedProperty(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE).intValue();
    }

    @Override
    public boolean isUsePolling() {
        return true;
    }
}