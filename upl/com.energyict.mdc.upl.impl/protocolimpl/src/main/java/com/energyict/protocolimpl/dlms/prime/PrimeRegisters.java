package com.energyict.protocolimpl.dlms.prime;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.DLMSStoredValues;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.ActivityCalendarSerializer;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 13:22
 */
public class PrimeRegisters {

    public static final ObisCode DUMMY_OBIS = ObisCode.fromString("0.0.0.0.0.0");
    public static final ObisCode PHASE_OBIS = ObisCode.fromString("1.1.94.34.104.255");
    public static final ObisCode ACTIVE_QUADRANT_OBIS = ObisCode.fromString("1.1.94.34.100.255");
    public static final ObisCode ACTIVE_QUADRANT_L1_OBIS = ObisCode.fromString("1.1.94.34.101.255");
    public static final ObisCode ACTIVE_QUADRANT_L2_OBIS = ObisCode.fromString("1.1.94.34.102.255");
    public static final ObisCode ACTIVE_QUADRANT_L3_OBIS = ObisCode.fromString("1.1.94.34.103.255");
    public static final ObisCode CONTACTOR_STATE_OBIS = ObisCode.fromString("0.0.96.3.10.255");
    public static final ObisCode PREV_CONTACTOR_STATE_OBIS = ObisCode.fromString("0.1.94.34.20.255");
    public static final ObisCode CL432_DEVICE_ADDRESS_OBIS = ObisCode.fromString("0.0.28.0.2.255");
    public static final ObisCode CL432_BASE_NODE_ADDRESS_OBIS = ObisCode.fromString("0.0.28.0.3.255");
    public static final ObisCode CRC_INCORRECT_COUNT = ObisCode.fromString("0.0.28.1.2.255");
    public static final ObisCode CRC_FAIL_COUNT = ObisCode.fromString("0.0.28.1.3.255");
    public static final ObisCode PHY_TX_DROP_COUNT = ObisCode.fromString("0.0.28.1.4.255");
    public static final ObisCode PHY_RX_DROP_COUNT = ObisCode.fromString("0.0.28.1.5.255");
    public static final ObisCode MAC_MIN_SWITCH_SEARCH_TIME = ObisCode.fromString("0.0.28.2.2.255");
    public static final ObisCode MAC_MAX_PROMOTION_PDU_OBIS = ObisCode.fromString("0.0.28.2.3.255");
    public static final ObisCode MAC_PROMOTION_PDU_TX_PERIOD_OBIS = ObisCode.fromString("0.0.28.2.4.255");
    public static final ObisCode MAC_BEACONS_PER_FRAME_OBIS = ObisCode.fromString("0.0.28.2.5.255");
    public static final ObisCode MAC_SCP_MAX_TX_ATTEMPTS_OBIS = ObisCode.fromString("0.0.28.2.6.255");
    public static final ObisCode MAC_CTL_RE_TX_TIMER_OBIS = ObisCode.fromString("0.0.28.2.7.255");
    public static final ObisCode MAC_MAX_CTL_RE_TX_OBIS = ObisCode.fromString("0.0.28.2.8.255");
    public static final ObisCode MAC_LNID = ObisCode.fromString("0.0.28.3.2.255");
    public static final ObisCode MAC_LSID = ObisCode.fromString("0.0.28.3.3.255");
    public static final ObisCode MAC_SID = ObisCode.fromString("0.0.28.3.4.255");
    public static final ObisCode MAC_SNA = ObisCode.fromString("0.0.28.3.5.255");
    public static final ObisCode MAC_STATE = ObisCode.fromString("0.0.28.3.6.255");
    public static final ObisCode MAC_SCP_LENGTH = ObisCode.fromString("0.0.28.3.7.255");
    public static final ObisCode MAC_NODE_HIERARCHY_LEVEL = ObisCode.fromString("0.0.28.3.8.255");
    public static final ObisCode MAC_BEACON_SLOT_COUNT = ObisCode.fromString("0.0.28.3.9.255");
    public static final ObisCode MAC_BEACON_RX_SLOT = ObisCode.fromString("0.0.28.3.10.255");
    public static final ObisCode MAC_BEACON_TX_SLOT = ObisCode.fromString("0.0.28.3.11.255");
    public static final ObisCode MAC_BEACON_RX_FREQUENCY = ObisCode.fromString("0.0.28.3.12.255");
    public static final ObisCode MAC_BEACON_TX_FREQUENCY = ObisCode.fromString("0.0.28.3.13.255");
    public static final ObisCode EQUIPMENT_IDENTIFIER = ObisCode.fromString("0.0.96.1.1.255");
    public static final ObisCode DLMS_PROTOCOL_INFO = ObisCode.fromString("0.0.96.1.2.255");
    public static final ObisCode MAPPED_DLMS_PROTOCOL_VERSION = ObisCode.fromString("0.2.96.1.2.255");
    public static final ObisCode MAPPED_EQUIPMENT_TYPE = ObisCode.fromString("0.1.96.1.2.255");
    public static final ObisCode MULTICAST_IDENTIFIER = ObisCode.fromString("0.0.96.1.5.255");
    public static final ObisCode MULTICAST_IDENTIFIER_1 = ObisCode.fromString("0.1.96.1.5.255");
    public static final ObisCode MULTICAST_IDENTIFIER_2 = ObisCode.fromString("0.2.96.1.5.255");
    public static final ObisCode MULTICAST_IDENTIFIER_3 = ObisCode.fromString("0.3.96.1.5.255");
    public static final ObisCode PRIME_DEVICE_SETUP = ObisCode.fromString("0.0.28.6.0.255");
    //Primary voltage does not exist in the companion pdf
    public static final ObisCode SECONDARY_VOLTAGE = ObisCode.fromString("1.0.32.7.0.255");
    //Primary current does not exist in the companion pdf
    public static final ObisCode SECONDARY_CURRENT = ObisCode.fromString("1.0.90.7.0.255");
    public static final ObisCode REFERENCE_VOLTAGE = ObisCode.fromString("1.0.0.6.4.255");
    public static final ObisCode CURRENT_TRANSFORMER_NUMERATOR = ObisCode.fromString("1.0.0.4.2.255");   //Supervision register only
    public static final ObisCode VOLTAGE_TRANSFORMER_NUMERATOR = ObisCode.fromString("1.0.0.4.3.255");   //Supervision register only
    public static final ObisCode CURRENT_TRANSFORMER_DENOMINATOR = ObisCode.fromString("1.0.0.4.5.255"); //Supervision register only
    public static final ObisCode VOLTAGE_TRANSFORMER_DENOMINATOR = ObisCode.fromString("1.0.0.4.6.255"); //Supervision register only
    public static final ObisCode DEMAND_CLOSE_CONTRACTED_POWER = ObisCode.fromString("0.0.94.34.70.255");
    public static final ObisCode VOLTAGE_SAG_THRESHOLD = ObisCode.fromString("1.0.12.31.0.255");
    public static final ObisCode VOLTAGE_SWELL_THRESHOLD = ObisCode.fromString("1.0.12.35.0.255");
    public static final ObisCode LONG_POWER_FAILURE_THRESHOLD = ObisCode.fromString("0.0.96.7.20.255");
    public static final ObisCode NR_OF_LONG_POWER_FAILURES_ALL_PHASES = ObisCode.fromString("0.0.96.7.5.255");
    public static final ObisCode TIMETHRESHOLD_VOLTAGE_SAG = ObisCode.fromString("1.0.12.43.0.255");
    public static final ObisCode TIMETHRESHOLD_VOLTAGE_SWELL = ObisCode.fromString("1.0.12.44.0.255");
    public static final ObisCode PROFILE_PERIOD = ObisCode.fromString("1.0.99.1.4.255");
    public static final ObisCode CONTRACT_DEFINITIONS = ObisCode.fromString("0.0.13.0.0.255");   //Special register, has activity calendar and special days table (xml format) in the description
    public static final ObisCode NR_OF_LONG_POWER_FAILURES_L1 = ObisCode.fromString("0.0.96.7.6.255");
    public static final ObisCode NR_OF_LONG_POWER_FAILURES_L2 = ObisCode.fromString("0.0.96.7.7.255");
    public static final ObisCode NR_OF_LONG_POWER_FAILURES_L3 = ObisCode.fromString("0.0.96.7.8.255");
    public static final ObisCode ERROR_OBJECT = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_OBJECT = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode ALARM_FILTER = ObisCode.fromString("0.0.97.98.10.255");
    public static final ObisCode CURRENTLY_ACTIVE_TARIFF_T1 = ObisCode.fromString("0.0.96.14.1.255");
    public static final ObisCode CURRENTLY_ACTIVE_TARIFF_T2 = ObisCode.fromString("0.0.96.14.2.255");
    public static final ObisCode CURRENTLY_ACTIVE_TARIFF_T3 = ObisCode.fromString("0.0.96.14.3.255");
    public static final ObisCode AMR_PROFILE_STATUS_LP1 = ObisCode.fromString("0.0.96.10.7.255");
    public static final ObisCode AMR_PROFILE_STATUS_LP2 = ObisCode.fromString("0.0.96.10.8.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SAGS_ANY_PHASE = ObisCode.fromString("1.0.12.32.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SAGS_L1 = ObisCode.fromString("1.0.32.32.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SAGS_L2 = ObisCode.fromString("1.0.52.32.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SAGS_L3 = ObisCode.fromString("1.0.72.32.0.255");
    public static final ObisCode NUMBER_OF_ACG_VOLTAGE_SAGS_ALL_PHASE = ObisCode.fromString("1.0.94.34.90.255");
    public static final ObisCode TIMESTAMP_FOR_POWER_QUALITY_LOG = ObisCode.fromString("0.0.94.34.80.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SWELLS_ANY_PHASE = ObisCode.fromString("1.0.12.36.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SWELLS_L1 = ObisCode.fromString("1.0.32.36.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SWELLS_L2 = ObisCode.fromString("1.0.52.36.0.255");
    public static final ObisCode NUMBER_OF_VOLTAGE_SWELLS_L3 = ObisCode.fromString("1.0.72.36.0.255");
    public static final ObisCode NUMBER_OF_ACG_VOLTAGE_SWELLS_ALL_PHASE = ObisCode.fromString("1.0.94.34.92.255");
    public static final ObisCode NUMBER_OF_PHASES = ObisCode.fromString("0.0.96.1.128.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C1 = ObisCode.fromString("1.0.1.6.10.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C1 = ObisCode.fromString("1.0.1.6.11.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C1 = ObisCode.fromString("1.0.1.6.12.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C1 = ObisCode.fromString("1.0.1.6.13.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C1 = ObisCode.fromString("1.0.1.6.14.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C1 = ObisCode.fromString("1.0.1.6.15.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C1 = ObisCode.fromString("1.0.1.6.16.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C2 = ObisCode.fromString("1.0.1.6.20.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C2 = ObisCode.fromString("1.0.1.6.21.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C2 = ObisCode.fromString("1.0.1.6.22.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C2 = ObisCode.fromString("1.0.1.6.23.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C2 = ObisCode.fromString("1.0.1.6.24.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C2 = ObisCode.fromString("1.0.1.6.25.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C2 = ObisCode.fromString("1.0.1.6.26.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C3 = ObisCode.fromString("1.0.1.6.30.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C3 = ObisCode.fromString("1.0.1.6.31.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C3 = ObisCode.fromString("1.0.1.6.32.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C3 = ObisCode.fromString("1.0.1.6.33.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C3 = ObisCode.fromString("1.0.1.6.34.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C3 = ObisCode.fromString("1.0.1.6.35.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C3 = ObisCode.fromString("1.0.1.6.36.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_IMPORT_CURRENT_BILLING_PERIOD = ObisCode.fromString("1.0.1.6.255.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C1 = ObisCode.fromString("1.0.2.6.10.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C1 = ObisCode.fromString("1.0.2.6.11.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C1 = ObisCode.fromString("1.0.2.6.12.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C1 = ObisCode.fromString("1.0.2.6.13.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C1 = ObisCode.fromString("1.0.2.6.14.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C1 = ObisCode.fromString("1.0.2.6.15.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C1 = ObisCode.fromString("1.0.2.6.16.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C2 = ObisCode.fromString("1.0.2.6.20.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C2 = ObisCode.fromString("1.0.2.6.21.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C2 = ObisCode.fromString("1.0.2.6.22.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C2 = ObisCode.fromString("1.0.2.6.23.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C2 = ObisCode.fromString("1.0.2.6.24.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C2 = ObisCode.fromString("1.0.2.6.25.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C2 = ObisCode.fromString("1.0.2.6.26.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C3 = ObisCode.fromString("1.0.2.6.30.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C3 = ObisCode.fromString("1.0.2.6.31.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C3 = ObisCode.fromString("1.0.2.6.32.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C3 = ObisCode.fromString("1.0.2.6.33.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C3 = ObisCode.fromString("1.0.2.6.34.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C3 = ObisCode.fromString("1.0.2.6.35.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C3 = ObisCode.fromString("1.0.2.6.36.255");
    public static final ObisCode MAXIMUM_DEMAND_REGISTER_EXPORT_CURRENT_BILLING_PERIOD = ObisCode.fromString("1.0.2.6.255.255");
    private final DlmsSession session;
    private final PrimeMeterInfo meterInfo;
    private final List<DLMSStoredValues> storedValuesList = new ArrayList<DLMSStoredValues>();
    private PrimeProperties properties;

    public PrimeRegisters(PrimeProperties properties, DlmsSession session, final PrimeMeterInfo meterInfo) {
        this.properties = properties;
        this.session = session;
        this.meterInfo = meterInfo;

        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.DAILY_CONTRACT1_PROFILE));
        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.DAILY_CONTRACT2_PROFILE));
        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.DAILY_CONTRACT3_PROFILE));

        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.MONTHLY_CONTRACT1_PROFILE));
        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.MONTHLY_CONTRACT2_PROFILE));
        storedValuesList.add(new DLMSStoredValues(this.session.getCosemObjectFactory(), PrimeProfile.MONTHLY_CONTRACT3_PROFILE));
    }

    public static RegisterInfo translateRegister(ObisCode obisCode) {
        return obisCode == null ? null : new RegisterInfo(obisCode.getDescription());
    }

    /**
     * Translates the data in the given byte array to ASCII.
     *
     * @param data The data to translate.
     * @return The ASII string.
     */
    private static final String translateToASCII(final byte[] data) {
        final StringBuilder builder = new StringBuilder();

        for (final byte b : data) {
            builder.append((char) (b & 0xFF));
        }

        return builder.toString().trim();
    }

    /**
     * Translates the given byte array to a hex string.
     *
     * @param data The data.
     * @return The resulting hex string.
     */
    private static final String translateToHexString(final byte[] data) {
        final StringBuilder formatBuilder = new StringBuilder();
        final Integer[] intData = new Integer[data.length];

        for (int i = 0; i < data.length; i++) {
            intData[i] = data[i] & 0xFF;
            formatBuilder.append("%02X");
        }

        return String.format(formatBuilder.toString(), (Object[]) intData);
    }

    public final RegisterValue readRegister(ObisCode obisCode) throws IOException {

        if (DUMMY_OBIS.equals(obisCode)) {
            throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] is a dummy register and doesn't exist in the meter.");
        }

        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = getStoredValues(obisCode).getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        final CosemObjectFactory cof = session.getCosemObjectFactory();

        if (PrimeMeterInfo.DLMS_FIRMWARE.equalsIgnoreBChannel(obisCode)) {
            return new RegisterValue(obisCode, this.meterInfo.getDLMSFirmware());
        }

        if (PrimeMeterInfo.PRIME_FW_OBIS.equalsIgnoreBChannel(obisCode)) {
            return new RegisterValue(obisCode, this.meterInfo.getPrimeFirmware());
        }

        if (TIMESTAMP_FOR_POWER_QUALITY_LOG.equals(obisCode)) {
            AbstractDataType valueAttr = cof.getData(obisCode).getValueAttr();
            long epoch;
            if (valueAttr.isOctetString()) {
                DateTime dateTime = valueAttr.getOctetString().getDateTime(this.session.getTimeZone());
                epoch = dateTime.getValue().getTime().getTime();
            } else {
                epoch = valueAttr.longValue();
            }
            return new RegisterValue(obisCode, new Quantity(epoch, Unit.getUndefined()), null, null, new Date(), new Date(), 0, new Date(epoch).toString());
        }

        if (EQUIPMENT_IDENTIFIER.equalsIgnoreBChannel(obisCode)) {
            final AbstractDataType abstractValue = cof.getData(obisCode).getValueAttr();
            if (abstractValue == null) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            if (!(abstractValue instanceof OctetString)) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            byte[] value = abstractValue.getOctetString().getOctetStr();
            return new RegisterValue(obisCode, ProtocolTools.getHexStringFromBytes(value, ""));
        }

        if (MAPPED_DLMS_PROTOCOL_VERSION.equals(obisCode)) {
            final AbstractDataType abstractValue = cof.getData(DLMS_PROTOCOL_INFO).getValueAttr();
            if (abstractValue == null) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            if (!(abstractValue instanceof OctetString)) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            byte[] value = abstractValue.getOctetString().getOctetStr();
            return new RegisterValue(obisCode, translateToASCII(ProtocolTools.getSubArray(value, 10)));  //Skip first 10 bytes, only last 8 bytes contain info about the DLMS protocol
        }

        if (MAPPED_EQUIPMENT_TYPE.equals(obisCode)) {
            final AbstractDataType abstractValue = cof.getData(DLMS_PROTOCOL_INFO).getValueAttr();
            if (abstractValue == null) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            if (!(abstractValue instanceof OctetString)) {
                throw new NoSuchRegisterException("Data value attribute for obis [" + obisCode + "] returned 'null'!");
            }
            byte[] value = abstractValue.getOctetString().getOctetStr();
            return new RegisterValue(obisCode, translateToASCII(ProtocolTools.getSubArray(value, 0, 10)));
        }

        if (MULTICAST_IDENTIFIER.equals(obisCode)) {
            final AbstractDataType value = cof.getData(MULTICAST_IDENTIFIER).getValueAttr();

            if (value == null) {
                throw new NoSuchRegisterException("Can not obtain value for register with OBIS code [" + MULTICAST_IDENTIFIER + "]");
            }

            if (!(value instanceof OctetString)) {
                throw new NoSuchRegisterException("Could obtain value for register with OBIS code [" + MULTICAST_IDENTIFIER + "], but it returns [" + value.getClass() + "] instead of an octetstring !");
            }

            return new RegisterValue(obisCode, translateToHexString(value.getOctetString().getOctetStr()));
        }

        if (MULTICAST_IDENTIFIER_1.equals(obisCode)) {
            return this.getMulticastIdentifier(cof, 0);
        }

        if (MULTICAST_IDENTIFIER_2.equals(obisCode)) {
            return this.getMulticastIdentifier(cof, 1);
        }

        if (MULTICAST_IDENTIFIER_3.equals(obisCode)) {
            return this.getMulticastIdentifier(cof, 2);
        }

        if (PRIME_DEVICE_SETUP.equals(obisCode)) {
            final MacAddressSetup macSetup = cof.getMacAddressSetup(PRIME_DEVICE_SETUP);
            final OctetString macAddress = macSetup.readMacAddress();

            return new RegisterValue(PRIME_DEVICE_SETUP, translateToHexString(macAddress.getContentBytes()));
        }

        if (PROFILE_PERIOD.equalsIgnoreBChannel(obisCode)) {
            int capturePeriod = cof.getProfileGeneric(properties.getLoadProfileObiscode()).getCapturePeriod();
            return new RegisterValue(obisCode, new Quantity(capturePeriod, Unit.get(BaseUnit.SECOND)));
        }

        if (ObisCode.fromString("1.1.1.1.1.255").equalsIgnoreBChannel(obisCode)) {
            try {
                int intervalTime = 60 * 5;
                session.getLogger().severe("Changing interval time to [" + intervalTime + " seconds] for profile with obis [" + PrimeProfile.BASIC_PROFILE + "]");
                ProfileGeneric basicProfile = cof.getProfileGeneric(PrimeProfile.BASIC_PROFILE);
                basicProfile.writeCapturePeriodAttr(new Unsigned32(intervalTime));
            } catch (IOException e) {
                return new RegisterValue(obisCode, "Failed: " + e.getMessage());
            }
            return new RegisterValue(obisCode, "Success!");
        }

        if (ObisCode.fromString("1.1.1.1.2.255").equalsIgnoreBChannel(obisCode)) {
            try {
                session.getLogger().info("Executing firmware update message");
                byte[] imageData = ProtocolTools.readBytesFromFile("/home/jme/Desktop/FW/AS330D/ASP06.01.11-08451.bin");
                final ImageTransfer it = cof.getImageTransfer();
                it.setUsePollingVerifyAndActivate(true);
                it.upgrade(imageData, true);
                it.imageActivation();
            } catch (IOException e) {
                return new RegisterValue(obisCode, "Failed: " + e.getMessage());
            }
            return new RegisterValue(obisCode, "Success!");
        }


        /* CL432 Setup attributes */

        if (obisCode.equals(CL432_DEVICE_ADDRESS_OBIS)) {
            int address = cof.getCL432Setup().getDeviceAddress().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(CL432_BASE_NODE_ADDRESS_OBIS)) {
            int address = cof.getCL432Setup().getBaseNodeAddress().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        /* Prime plc physical layer counter attributes */

        if (obisCode.equals(CRC_INCORRECT_COUNT)) {
            int address = cof.getPrimePlcPhyLayerCounters().getCrcIncorrectCount().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(CRC_FAIL_COUNT)) {
            int address = cof.getPrimePlcPhyLayerCounters().getCrcFailCount().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(PHY_TX_DROP_COUNT)) {
            int address = cof.getPrimePlcPhyLayerCounters().getTxDropCount().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(PHY_RX_DROP_COUNT)) {
            int address = cof.getPrimePlcPhyLayerCounters().getRxDropCount().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        /* PRIME PLC MAC setup attributes */

        if (obisCode.equals(MAC_MIN_SWITCH_SEARCH_TIME)) {
            int address = cof.getPrimePlcMacSetup().getMinSwitchSearchTime().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.get("s")));
        }

        if (obisCode.equals(MAC_MAX_PROMOTION_PDU_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getMaxPromotionPdu().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.get("s")));
        }

        if (obisCode.equals(MAC_PROMOTION_PDU_TX_PERIOD_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getPromotionPduTxPeriod().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.get("s")));
        }

        if (obisCode.equals(MAC_BEACONS_PER_FRAME_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getBeaconsPerFrame().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_SCP_MAX_TX_ATTEMPTS_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getScpMaxTxAttempts().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_CTL_RE_TX_TIMER_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getCtlReTxTimer().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.get("s")));
        }

        if (obisCode.equals(MAC_MAX_CTL_RE_TX_OBIS)) {
            int address = cof.getPrimePlcMacSetup().getMaxCtlReTx().getValue();
            return new RegisterValue(obisCode, new Quantity(address, Unit.getUndefined()));
        }

        /* Prime plc mac functional parameter attributes */

        if (obisCode.equals(MAC_LNID)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getLNID().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_LSID)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getLSID().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_SID)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getSID().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_SNA)) {
            byte[] value = cof.getPrimePlcMacFunctionalParameters().getSNA().getOctetStr();
            return new RegisterValue(obisCode, ProtocolTools.getHexStringFromBytes(value, ""));
        }

        if (obisCode.equals(MAC_STATE)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getState().getValue();
            final String textValue;
            switch (value) {
                case 0:
                    textValue = "Disconnected [0]";
                    break;
                case 1:
                    textValue = "Terminal [1]";
                    break;
                case 2:
                    textValue = "Switch [2]";
                    break;
                case 3:
                    textValue = "Base [3]";
                    break;
                default:
                    textValue = "Unknown [" + value + "]";
                    break;
            }
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), null, null, new Date(), new Date(), 0, textValue);
        }

        if (obisCode.equals(MAC_SCP_LENGTH)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getScpLength().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_NODE_HIERARCHY_LEVEL)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getNodeHierarchyLevel().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_BEACON_SLOT_COUNT)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getBeaconSlotCount().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_BEACON_RX_SLOT)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getBeaconRxSlot().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_BEACON_TX_SLOT)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getBeaconTxSlot().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_BEACON_RX_FREQUENCY)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getBeaconRxFrequency().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        if (obisCode.equals(MAC_BEACON_TX_FREQUENCY)) {
            int value = cof.getPrimePlcMacFunctionalParameters().getBeaconTxFrequency().getValue();
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
        }

        /* Other attributes */

        if (isActiveQuadrant(obisCode)) {
            Data data = cof.getData(obisCode);
            long value = data.getValue();
            String text = "Active Quadrant: " + (value != 0 ? value : "No quadrant detected");
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), null, null, null, new Date(), 0, text);
        }

        if (obisCode.equals(PHASE_OBIS)) {
            Data data = cof.getData(obisCode);
            long value = data.getValue();
            String text = "Phase undefined";
            if ((value & 0x01) == 0x01) {
                text = "Phase 1";
            } else if ((value & 0x02) == 0x02) {
                text = "Phase 2";
            } else if ((value & 0x04) == 0x04) {
                text = "Phase 3";
            }
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), null, null, null, new Date(), 0, text);
        }

        if (obisCode.equals(CONTACTOR_STATE_OBIS) || obisCode.equals(PREV_CONTACTOR_STATE_OBIS)) {
            final Disconnector disconnector = cof.getDisconnector(obisCode);
            final TypeEnum controlState = disconnector.getControlState();
            final int controlStateValue = controlState.getValue();
            final Quantity quantity = new Quantity(controlStateValue, Unit.getUndefined());
            switch (controlStateValue) {
                case 0:
                    return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "Disconnected");
                case 1:
                    return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "Connected");
                case 2:
                    return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "Ready for Re-connection");
                default:
                    return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "Unknown control state: " + controlStateValue);
            }
        }

        if (obisCode.equals(CONTRACT_DEFINITIONS)) {
            ActivityCalendarSerializer parser = new ActivityCalendarSerializer(cof, session.getTimeZone());
            parser.parseAllContractDefinitions();
            return new RegisterValue(obisCode, parser.getFullXML());
        }

        if (isNumericalDataValue(obisCode)) {
            final Data data = cof.getData(obisCode);
            final Quantity value = data.getQuantityValue();
            return new RegisterValue(obisCode, value);
        }

        if (isExtendedRegister(obisCode)) {
            final ExtendedRegister extendedRegister = cof.getExtendedRegister(obisCode);
            final Quantity value = extendedRegister.getQuantityValue();
            return new RegisterValue(obisCode, value);
        }

        if (NUMBER_OF_PHASES.equals(obisCode)) {
            try {
                cof.getData(ACTIVE_QUADRANT_L3_OBIS).getValueAttr();
                return new RegisterValue(obisCode, new Quantity(3, Unit.getUndefined()));
            } catch (DataAccessResultException e) {
                switch (e.getCode()) {
                    case HARDWARE_FAULT:
                    case OBJECT_UNDEFINED:
                    case OBJECT_UNAVAILABLE:
                    case OBJECTCLASS_INCONSISTENT: // For the ZIV
                    case RW_DENIED: // For the Circutor
                    case OTHER:
                        return new RegisterValue(obisCode, new Quantity(1, Unit.getUndefined()));
                    default:
                        throw e;
                }
            }
        }

        final Register register = cof.getRegister(obisCode);
        final Quantity value = register.getQuantityValue();
        return new RegisterValue(obisCode, value);
    }

    private final boolean isActiveQuadrant(final ObisCode obisCode) {
        boolean isActiveQuadrant = false;
        isActiveQuadrant |= obisCode.equals(ACTIVE_QUADRANT_OBIS);
        isActiveQuadrant |= obisCode.equals(ACTIVE_QUADRANT_L1_OBIS);
        isActiveQuadrant |= obisCode.equals(ACTIVE_QUADRANT_L2_OBIS);
        isActiveQuadrant |= obisCode.equals(ACTIVE_QUADRANT_L3_OBIS);
        return isActiveQuadrant;
    }

    private final boolean isExtendedRegister(final ObisCode obisCode) {
        boolean extendedRegister = false;
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C1);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C2);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_TOT_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R1_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R2_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R3_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R4_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R5_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_R6_C3);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_IMPORT_CURRENT_BILLING_PERIOD);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C1);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C1);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C2);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C2);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_TOT_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R1_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R2_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R3_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R4_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R5_C3);
        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_R6_C3);

        extendedRegister |= obisCode.equals(MAXIMUM_DEMAND_REGISTER_EXPORT_CURRENT_BILLING_PERIOD);
        return extendedRegister;
    }

    private final boolean isNumericalDataValue(final ObisCode obisCode) {
        boolean numericalDataValue = false;
        numericalDataValue |= obisCode.equals(NR_OF_LONG_POWER_FAILURES_ALL_PHASES);
        numericalDataValue |= obisCode.equals(NR_OF_LONG_POWER_FAILURES_L1);
        numericalDataValue |= obisCode.equals(NR_OF_LONG_POWER_FAILURES_L2);
        numericalDataValue |= obisCode.equals(NR_OF_LONG_POWER_FAILURES_L3);
        numericalDataValue |= obisCode.equals(ERROR_OBJECT);
        numericalDataValue |= obisCode.equals(ALARM_OBJECT);
        numericalDataValue |= obisCode.equals(ALARM_FILTER);
        numericalDataValue |= obisCode.equals(CURRENTLY_ACTIVE_TARIFF_T1);
        numericalDataValue |= obisCode.equals(CURRENTLY_ACTIVE_TARIFF_T2);
        numericalDataValue |= obisCode.equals(CURRENTLY_ACTIVE_TARIFF_T3);
        numericalDataValue |= obisCode.equals(AMR_PROFILE_STATUS_LP1);
        numericalDataValue |= obisCode.equals(AMR_PROFILE_STATUS_LP2);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SAGS_ANY_PHASE);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SAGS_L1);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SAGS_L2);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SAGS_L3);
        numericalDataValue |= obisCode.equals(NUMBER_OF_ACG_VOLTAGE_SAGS_ALL_PHASE);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SWELLS_ANY_PHASE);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SWELLS_L1);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SWELLS_L2);
        numericalDataValue |= obisCode.equals(NUMBER_OF_VOLTAGE_SWELLS_L3);
        numericalDataValue |= obisCode.equals(NUMBER_OF_ACG_VOLTAGE_SWELLS_ALL_PHASE);
        numericalDataValue |= obisCode.equals(CURRENT_TRANSFORMER_NUMERATOR);
        numericalDataValue |= obisCode.equals(VOLTAGE_TRANSFORMER_NUMERATOR);
        numericalDataValue |= obisCode.equals(CURRENT_TRANSFORMER_DENOMINATOR);
        numericalDataValue |= obisCode.equals(VOLTAGE_TRANSFORMER_DENOMINATOR);
        return numericalDataValue;
    }

    private final DLMSStoredValues getStoredValues(ObisCode obisCode) throws IOException {
        int[] bounds = new int[2];

        if (Math.abs(obisCode.getF()) < 12) {   //Monthly billing
            bounds[0] = 3;
            bounds[1] = 6;
        } else {                                // Daily billing
            bounds[0] = 0;
            bounds[1] = 3;
        }

        for (int i = bounds[0]; i < bounds[1]; i++) {
            DLMSStoredValues storedValues = storedValuesList.get(i);
            if (storedValues.isObiscodeCaptured(obisCode)) {
                return storedValues;
            }
        }
        throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " not found in the Capture Objects list of the billing profiles.");
    }

    /**
     * Gets the specified multicast identifier.
     *
     * @param index         0, 1 or 2.
     * @param objectFactory The COSEM object factory used to fetch the object.
     * @return The register value.
     * @throws com.energyict.protocol.NoSuchRegisterException If the object could not be obtained or is of the wrong type.
     * @throws java.io.IOException                            If an IO error occurs while fetching the object.
     */
    private final RegisterValue getMulticastIdentifier(final CosemObjectFactory objectFactory, final int index) throws NoSuchRegisterException, IOException {
        final AbstractDataType value = objectFactory.getData(MULTICAST_IDENTIFIER).getValueAttr();

        if (value == null) {
            throw new NoSuchRegisterException("Can not obtain value for register with OBIS code [" + MULTICAST_IDENTIFIER + "]");
        }

        if (!(value instanceof OctetString)) {
            throw new NoSuchRegisterException("Could obtain value for register with OBIS code [" + MULTICAST_IDENTIFIER + "], but it returns [" + value.getClass() + "] instead of an octetstring !");
        }

        final byte[] address = new byte[8];
        System.arraycopy(value.getOctetString().getOctetStr(), 8 * index, address, 0, address.length);

        ObisCode obisCode = null;

        switch (index) {
            case 0: {
                obisCode = MULTICAST_IDENTIFIER_1;
                break;
            }

            case 1: {
                obisCode = MULTICAST_IDENTIFIER_2;
                break;
            }

            case 2: {
                obisCode = MULTICAST_IDENTIFIER_3;
                break;
            }
        }

        return new RegisterValue(obisCode, translateToHexString(address));
    }
}
