package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.G3ProfileType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 15:25
 */
public class G3RegisterMapper {

    /**
     * Billing period mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 2)
     */
    private static final ObisCode ASYNC_EOB_SCHEDULE_ALL = ObisCode.fromString("0.3.15.0.0.255");
    private static final ObisCode ASYNC_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.3.15.0.1.255");
    private static final ObisCode ASYNC_EOB_SCHEDULE_TIME = ObisCode.fromString("0.3.15.0.2.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_ALL = ObisCode.fromString("0.2.15.0.0.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.2.15.0.1.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_TIME = ObisCode.fromString("0.2.15.0.2.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_ALL = ObisCode.fromString("0.1.15.0.0.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.1.15.0.1.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_TIME = ObisCode.fromString("0.1.15.0.2.255");
    /**
     * Breaker management mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 3)
     */

    private static final ObisCode BREAKER_STATE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode DOWNSTREAM_VOLTAGE_MONITORING = ObisCode.fromString("1.0.12.35.3.255");
    private static final ObisCode OVERHEAT_CURRENT_LIMIT = ObisCode.fromString("1.0.0.12.1.255");
    private static final ObisCode OVERHEAT_DURATION_LIMIT = ObisCode.fromString("1.0.0.12.2.255");
    /**
     * Communication (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 4)
     */

    private static final ObisCode CONSUMER_INTERFACE_SETUP = ObisCode.fromString("0.0.94.33.0.255");
    private static final ObisCode EURIDIS_PROTOCOL_SETUP = ObisCode.fromString("0.0.23.1.0.255");
    /**
     * Database mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 5)
     */

    private static final ObisCode FIRMWARE_ENTRIES = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode FIRMWARE_ENTRY_1 = ObisCode.fromString("1.0.0.2.1.255");
    private static final ObisCode FIRMWARE_ENTRY_2 = ObisCode.fromString("1.0.0.2.2.255");
    private static final ObisCode FIRMWARE_ENTRY_3 = ObisCode.fromString("1.0.0.2.3.255");
    private static final ObisCode FIRMWARE_ENTRY_4 = ObisCode.fromString("1.0.0.2.4.255");
    private static final ObisCode FIRMWARE_ENTRY_5 = ObisCode.fromString("1.0.0.2.5.255");
    private static final ObisCode LOGICAL_DEVICE_NAME = ObisCode.fromString("0.0.42.0.0.255");
    private static final ObisCode PRODUCER_CONSUMER_CODE = ObisCode.fromString("1.0.96.63.11.255");
    private static final ObisCode TEST_MODE_CODE = ObisCode.fromString("1.0.96.63.12.255");
    /**
     * Event manager mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 7)
     */

    private static final ObisCode ALARM_FILTER = ObisCode.fromString("0.0.97.98.10.255");
    private static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    private static final ObisCode BREAKER_EVENT_CODE = ObisCode.fromString("0.3.96.11.0.255");
    private static final ObisCode BREAKER_OPENING_COUNTER = ObisCode.fromString("0.0.96.15.1.255");
    private static final ObisCode BREAKER_OPENING_COUNTER_OVER_MAX_OPENING_CURRENT = ObisCode.fromString("0.0.96.15.2.255");
    private static final ObisCode COMMUNICATION_EVENT_CODE = ObisCode.fromString("0.4.96.11.0.255");
    private static final ObisCode COVER_EVENT_CODE = ObisCode.fromString("0.2.96.11.0.255");
    private static final ObisCode PEAK_EVENT_CODE = ObisCode.fromString("0.6.96.11.0.255");
    private static final ObisCode COVER_OPENING_COUNTER = ObisCode.fromString("0.0.96.15.0.255");
    private static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    private static final ObisCode IMAGE_TRANSFER_COUNTER = ObisCode.fromString("0.0.96.63.10.255");
    private static final ObisCode MAIN_EVENT_CODE = ObisCode.fromString("0.1.96.11.0.255");
    private static final ObisCode NUMBER_OF_PROGRAMMING = ObisCode.fromString("0.0.96.2.0.255");
    private static final ObisCode STATUS_REGISTER = ObisCode.fromString("1.0.96.5.1.255");
    private static final ObisCode VOLTAGE_CUT_EVENT_CODE = ObisCode.fromString("0.5.96.11.0.255");
    private static final ObisCode THR_DOWNSTREAM_IMPEDANCE = ObisCode.fromString("1.0.96.31.0.255");
    /**
     * Total energy registry mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 15)
     */

    private static final ObisCode TOTAL_EXPORT_ACTIVE_ENERGY = ObisCode.fromString("1.1.2.8.0.255");
    private static final ObisCode TOTAL_IMPORT_ACTIVE_ENERGY = ObisCode.fromString("1.1.1.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q1_ENERGY = ObisCode.fromString("1.1.5.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q2_ENERGY = ObisCode.fromString("1.1.6.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q3_ENERGY = ObisCode.fromString("1.1.7.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q4_ENERGY = ObisCode.fromString("1.1.8.8.0.255");
    private static final ObisCode ACTIVE_E_CONSIST_CHK_THR = ObisCode.fromString("1.0.11.31.0.255");
    private static final ObisCode DAILY_MAX_POWER = ObisCode.fromString("1.1.9.19.0.255");
    private static final ObisCode TARIFF1_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.1.255");
    private static final ObisCode TARIFF2_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.2.255");
    private static final ObisCode TARIFF3_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.3.255");
    private static final ObisCode TARIFF4_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.4.255");
    private static final ObisCode TARIFF5_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.5.255");
    private static final ObisCode TARIFF6_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.6.255");
    private static final ObisCode TARIFF7_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.7.255");
    private static final ObisCode TARIFF8_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.8.255");
    private static final ObisCode TARIFF9_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.9.255");
    private static final ObisCode TARIFF10_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.10.255");
    private static final ObisCode TARIFF1_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.1.255");
    private static final ObisCode TARIFF2_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.2.255");
    private static final ObisCode TARIFF3_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.3.255");
    private static final ObisCode TARIFF4_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.4.255");
    /**
     * Voltage quality measurement mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 16)
     */

    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH1 = ObisCode.fromString("1.1.32.5.0.255");
    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH2 = ObisCode.fromString("1.1.52.5.0.255");
    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH3 = ObisCode.fromString("1.1.72.5.0.255");
    private static final ObisCode PH_WITH_ABNORMAL_VOLTAGE = ObisCode.fromString("1.0.12.38.0.255");
    private static final ObisCode VOLTAGE_CUT_MINIMUM_DURATION = ObisCode.fromString("1.0.12.45.0.255");
    /**
     * PLC Statistics mappings, B-field indicates the attribute number
     */
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR1 = ObisCode.fromString("0.1.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR2 = ObisCode.fromString("0.2.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR3 = ObisCode.fromString("0.3.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR4 = ObisCode.fromString("0.4.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR5 = ObisCode.fromString("0.5.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR6 = ObisCode.fromString("0.6.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR7 = ObisCode.fromString("0.7.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR8 = ObisCode.fromString("0.8.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR9 = ObisCode.fromString("0.9.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR10 = ObisCode.fromString("0.10.29.0.0.255");
    private static final ObisCode MAC_SETUP_ATTR1 = ObisCode.fromString("0.1.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR2 = ObisCode.fromString("0.2.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR3 = ObisCode.fromString("0.3.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR4 = ObisCode.fromString("0.4.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR7 = ObisCode.fromString("0.7.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR8 = ObisCode.fromString("0.8.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR9 = ObisCode.fromString("0.9.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR10 = ObisCode.fromString("0.10.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR11 = ObisCode.fromString("0.11.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR12 = ObisCode.fromString("0.12.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR13 = ObisCode.fromString("0.13.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR14 = ObisCode.fromString("0.14.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR15 = ObisCode.fromString("0.15.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR16 = ObisCode.fromString("0.16.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR17 = ObisCode.fromString("0.17.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR18 = ObisCode.fromString("0.18.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR20 = ObisCode.fromString("0.20.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR21 = ObisCode.fromString("0.21.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR22 = ObisCode.fromString("0.22.29.1.0.255");

    private static final ObisCode SIXLOWPAN_SETUP_ATTR1 = ObisCode.fromString("0.1.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR2 = ObisCode.fromString("0.2.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR3 = ObisCode.fromString("0.3.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR4 = ObisCode.fromString("0.4.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR5 = ObisCode.fromString("0.5.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR6 = ObisCode.fromString("0.6.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR7 = ObisCode.fromString("0.7.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR8 = ObisCode.fromString("0.8.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR9 = ObisCode.fromString("0.9.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR10 = ObisCode.fromString("0.10.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR11 = ObisCode.fromString("0.11.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR12 = ObisCode.fromString("0.12.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR13 = ObisCode.fromString("0.13.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR14 = ObisCode.fromString("0.14.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR15 = ObisCode.fromString("0.15.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR16 = ObisCode.fromString("0.16.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR17 = ObisCode.fromString("0.17.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR18 = ObisCode.fromString("0.18.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR19 = ObisCode.fromString("0.19.29.2.0.255");

    protected final List<G3Mapping> mappings = new ArrayList<G3Mapping>();
    private final Logger logger;
    private final CosemObjectFactory cosemObjectFactory;
    private final TimeZone deviceTimeZone;
    private G3StoredValues dailyStoredValues;
    private G3StoredValues monthlyStoredValues;

    /**
     * G3 register mapping, used to read ata from the meter as a register value
     */
    public G3RegisterMapper(CosemObjectFactory cosemObjectFactory, TimeZone deviceTimeZone, Logger logger) {
        initializeMappings();

        this.logger = logger;
        this.cosemObjectFactory = cosemObjectFactory;
        this.deviceTimeZone = deviceTimeZone;

        dailyStoredValues = new G3StoredValues(cosemObjectFactory, deviceTimeZone, G3ProfileType.DAILY_PROFILE.getObisCode(), true);
        monthlyStoredValues = new G3StoredValues(cosemObjectFactory, deviceTimeZone, G3ProfileType.MONTHLY_PROFILE.getObisCode(), false);
    }

    protected void initializeMappings() {
        this.mappings.addAll(getClockMappings());
        this.mappings.addAll(getBillingPeriodMappings());
        this.mappings.addAll(getBreakerManagementMappings());
        this.mappings.addAll(getCommunicationMappings());
        this.mappings.addAll(getDataBaseMappings());
        this.mappings.addAll(getEventManagerMappings());
        this.mappings.addAll(getTotalEnergyRegistering());
        this.mappings.addAll(getRatedEnergyRegistering());
        this.mappings.addAll(getVoltageQualityMeasurements());
        this.mappings.addAll(getPLCStatisticsMappings());
    }

    /**
     * Find the proper G3Mapping in the list of supported registers.
     * If not supported, returns null.
     */
    public G3Mapping getG3Mapping(ObisCode obisCode) {
        for (G3Mapping g3Mapping : getMappings()) {
            if (g3Mapping.getObisCode().equals(obisCode)) {
                return g3Mapping;
            }
        }
        return null;
    }

    public List<G3Mapping> getMappings() {
        return mappings;
    }

    /**
     * Lookup the register mapper and use it to fetch the data and create a correct register mapping
     *
     * @param obisCode The obisCode of the data to read
     * @return The register value
     * @throws java.io.IOException
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        logger.fine("Attempting to read out register [" + obisCode.toString() + "]");
        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = getStoredValuesImpl(obisCode).getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        for (G3Mapping mapping : mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                final RegisterValue registerValue = mapping.readRegister(cosemObjectFactory);
                if (registerValue != null) {
                    return registerValue;
                }
            }
        }
        throw new NoSuchRegisterException(obisCode.toString());
    }

    private StoredValues getStoredValuesImpl(ObisCode obisCode) throws NoSuchRegisterException {
        if (isDaily(obisCode)) {
            return dailyStoredValues;
        } else if (isMonthly(obisCode)) {
            return monthlyStoredValues;
        } else {
            throw new NoSuchRegisterException("Unsupported F-field in obis code " + obisCode);
        }
    }

    private boolean isDaily(ObisCode obisCode) {
        return obisCode.getF() > 11;
    }

    private boolean isMonthly(ObisCode obisCode) {
        return obisCode.getF() < 12;
    }

    private final List<G3Mapping> getClockMappings() {
        List<G3Mapping> basicChecks = new ArrayList<G3Mapping>();
        basicChecks.add(new ClockMapping(Clock.getDefaultObisCode()));
        return basicChecks;
    }

    private final List<G3Mapping> getBillingPeriodMappings() {
        List<G3Mapping> billingMappings = new ArrayList<G3Mapping>();
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_TIME));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_TIME));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_TIME));
        return billingMappings;
    }

    private final List<G3Mapping> getBreakerManagementMappings() {
        final List<G3Mapping> breakerMappings = new ArrayList<G3Mapping>();
        breakerMappings.add(new DisconnectControlMapper(BREAKER_STATE));
        breakerMappings.add(new DownstreamVoltageMonitoringMapper(DOWNSTREAM_VOLTAGE_MONITORING, Unit.get("V")));
        breakerMappings.add(new OverHeatCurrentLimitMapper(OVERHEAT_CURRENT_LIMIT, Unit.get("A")));
        breakerMappings.add(new DataValueMapping(OVERHEAT_DURATION_LIMIT, Unit.get("s")));
        return breakerMappings;
    }

    private final List<G3Mapping> getCommunicationMappings() {
        final List<G3Mapping> communcationMappings = new ArrayList<G3Mapping>();
        communcationMappings.add(new DataValueMapping(CONSUMER_INTERFACE_SETUP));
        communcationMappings.add(new DataValueMapping(EURIDIS_PROTOCOL_SETUP));
        return communcationMappings;
    }

    private final List<G3Mapping> getDataBaseMappings() {
        final List<G3Mapping> dataBaseMappings = new ArrayList<G3Mapping>();
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRIES));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_1));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_2));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_3));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_4));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_5));
        dataBaseMappings.add(new LogicalDeviceNameMapping(LOGICAL_DEVICE_NAME));
        dataBaseMappings.add(new ProducerConsumerMapping(PRODUCER_CONSUMER_CODE));
        dataBaseMappings.add(new TestModeMapper(TEST_MODE_CODE));
        return dataBaseMappings;
    }

    private final List<G3Mapping> getEventManagerMappings() {
        final List<G3Mapping> eventMappings = new ArrayList<G3Mapping>();
        eventMappings.add(new DataValueMapping(ALARM_FILTER));
        eventMappings.add(new DataValueMapping(ALARM_REGISTER));
        eventMappings.add(new DataValueMapping(BREAKER_EVENT_CODE));
        eventMappings.add(new DataValueMapping(BREAKER_OPENING_COUNTER));
        eventMappings.add(new DataValueMapping(BREAKER_OPENING_COUNTER_OVER_MAX_OPENING_CURRENT));
        eventMappings.add(new DataValueMapping(COMMUNICATION_EVENT_CODE));
        eventMappings.add(new DataValueMapping(COVER_EVENT_CODE));
        eventMappings.add(new DataValueMapping(PEAK_EVENT_CODE));
        eventMappings.add(new DataValueMapping(COVER_OPENING_COUNTER));
        eventMappings.add(new DataValueMapping(ERROR_REGISTER));
        eventMappings.add(new DataValueMapping(IMAGE_TRANSFER_COUNTER));
        eventMappings.add(new DataValueMapping(MAIN_EVENT_CODE));
        eventMappings.add(new DataValueMapping(NUMBER_OF_PROGRAMMING));
        eventMappings.add(new DataValueMapping(STATUS_REGISTER));
        eventMappings.add(new DataValueMapping(VOLTAGE_CUT_EVENT_CODE));
        eventMappings.add(new DataValueMapping(THR_DOWNSTREAM_IMPEDANCE));
        return eventMappings;
    }

    private final List<G3Mapping> getTotalEnergyRegistering() {
        final List<G3Mapping> energyMappings = new ArrayList<G3Mapping>();
        energyMappings.add(new RegisterMapping(TOTAL_EXPORT_ACTIVE_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_IMPORT_ACTIVE_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q1_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q2_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q3_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q4_ENERGY));
        energyMappings.add(new RegisterMapping(ACTIVE_E_CONSIST_CHK_THR));
        energyMappings.add(new ExtendedRegisterMapping(DAILY_MAX_POWER));
        return energyMappings;
    }

    private final List<G3Mapping> getRatedEnergyRegistering() {
        final List<G3Mapping> ratedEnergyMappings = new ArrayList<G3Mapping>();
        ratedEnergyMappings.add(new RegisterMapping(TARIFF1_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF2_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF3_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF4_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF5_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF6_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF7_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF8_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF9_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF10_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF1_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF2_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF3_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF4_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        return ratedEnergyMappings;
    }

    private final List<G3Mapping> getVoltageQualityMeasurements() {
        final List<G3Mapping> voltageMappings = new ArrayList<G3Mapping>();
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH1));
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH2));
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH3));
        voltageMappings.add(new DataValueMapping(VOLTAGE_CUT_MINIMUM_DURATION));
        voltageMappings.add(new ExtendedRegisterMapping(PH_WITH_ABNORMAL_VOLTAGE));
        return voltageMappings;
    }

    protected final List<G3Mapping> getPLCStatisticsMappings() {
        final List<G3Mapping> plcStatistics = new ArrayList<G3Mapping>();
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR5));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR6));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR10));

        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR10));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR11));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR12));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR13));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR14));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR15));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR16));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR17));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR18));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR20));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR21));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR22));

        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR5));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR6));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR10));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR11));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR12));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR13));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR14));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR15));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR16));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR17));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR18));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR19));

        return plcStatistics;
    }
}