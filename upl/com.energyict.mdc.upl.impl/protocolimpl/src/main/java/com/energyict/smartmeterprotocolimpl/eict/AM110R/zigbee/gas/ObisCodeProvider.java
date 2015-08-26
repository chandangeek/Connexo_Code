package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 17:09
 */
public final class ObisCodeProvider {

    public static final ObisCode OperationalFirmwareMonlicitic = ObisCode.fromString("7.0.0.2.0.255");
    public static final ObisCode OperationalFirmwareNonMIDApp = ObisCode.fromString("7.2.0.2.0.255");
    public static final ObisCode OperationalFirmwareBootloader = ObisCode.fromString("7.3.0.2.0.255");
    public static final ObisCode OperationalFirmwareZCLVersion = ObisCode.fromString("0.4.0.2.0.255");
    public static final ObisCode OperationalFirmwareStackVersion = ObisCode.fromString("0.5.0.2.0.255");
    public static final ObisCode OperationalFirmwareZigbeeChip = ObisCode.fromString("0.6.0.2.0.255");
    public static final ObisCode OperationalFirmwareHAN = ObisCode.fromString("0.7.0.2.0.255");
    public static final ObisCode OperationalFirmwareWAN = ObisCode.fromString("0.8.0.2.0.255");

    public static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DISCONNECT_CONTROL_EVENT_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode POWER_QUALITY_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode COMM_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode PREPAYMENT_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode COTS_EVENT_LOG = ObisCode.fromString("0.0.99.98.11.255");
    public static final ObisCode MANUFACTURER_EVENT_LOG = ObisCode.fromString("0.0.99.98.31.255");

    public static final ObisCode DeviceId1 = ObisCode.fromString("0.0.96.1.0.255");     // SerialNumber
    public static final ObisCode DeviceId2 = ObisCode.fromString("0.0.96.1.1.255");     // UtilitySpecified EquipmentID
    public static final ObisCode DeviceId3 = ObisCode.fromString("0.0.96.1.2.255");     //E-Function location details, e.g. 48 chars (maybe need removing)
    public static final ObisCode DeviceId4 = ObisCode.fromString("0.0.96.1.3.255");     //E-location information ? 48 chars
    public static final ObisCode DeviceId5 = ObisCode.fromString("0.0.96.1.4.255");     //E-configuration information ? 16 chars
    public static final ObisCode DeviceId6 = ObisCode.fromString("0.0.96.1.5.255");     //Manufacturer Name
    public static final ObisCode DeviceId7 = ObisCode.fromString("0.0.96.1.6.255");     //Manufacture ID (ZigBee MSP ID [SSWG code for Clusters])
    public static final ObisCode DeviceId8 = ObisCode.fromString("0.0.96.1.7.255");     //PAYG ID
    public static final ObisCode DeviceId9 = ObisCode.fromString("0.0.96.1.8.255");
    public static final ObisCode DeviceId10 = ObisCode.fromString("0.0.96.1.9.255");     //Serial Number of Module
    public static final ObisCode MeteringPointId = ObisCode.fromString("0.0.96.1.10.255");     //MPAN or the MPRN
    public static final ObisCode DeviceId50 = ObisCode.fromString("0.0.96.1.50.255");     //hours in operation
    public static final ObisCode DeviceId51 = ObisCode.fromString("0.0.96.1.51.255");     //hours in fault
    public static final ObisCode DeviceId52 = ObisCode.fromString("0.0.96.1.52.255");     //remaining battery life

    public static final ObisCode GENERAL_LP_STATUS_OBISCODE = ObisCode.fromString("0.0.96.10.1.255");

    public static final ObisCode REG_CLOCK_TIME_SHIFT_EVENT_LIMIT = ObisCode.fromString("7.0.0.9.11.255");
    public static final ObisCode REG_CLOCK_SYNC_WINDOW = ObisCode.fromString("7.0.0.9.9.255");
    public static final ObisCode REG_ERROR = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode REG_ALARM = ObisCode.fromString("0.0.97.98.0.255");


    public static final ObisCode FirmwareUpgrade = ObisCode.fromString("0.0.44.0.0.255");
    public static final ObisCode ImageActivationScheduler = ObisCode.fromString("0.0.15.0.2.255");


    public static final ObisCode ChangeOfSupplier = ObisCode.fromString("0.0.65.1.1.255");
    public static final ObisCode ChangeOfTenant = ObisCode.fromString("0.0.65.0.0.255");

    public static final ObisCode CalorificValue = ObisCode.fromString("7.0.54.0.0.255");
    public static final ObisCode ConversionFactor = ObisCode.fromString("7.0.52.0.0.255");

    public static final ObisCode COTSPredefinedScriptTable = ObisCode.fromString("0.0.10.0.64.255");

    public static final ObisCode ActivityCalendarObisCode = ObisCode.fromString("0.0.13.0.0.255");
    public static final ObisCode PassiveSpecialDayObisCode = ObisCode.fromString("0.1.11.0.0.255");
    public static final ObisCode PassiveScriptTableObisCode = ObisCode.fromString("0.0.10.1.100.255");
    public static final ObisCode TariffRateLabelObisCode = ObisCode.fromString("0.0.63.0.1.255");
    public static final ObisCode TariffInformationObisCode = ObisCode.fromString("0.0.63.1.1.255");
    public static final ObisCode BlockTariffObisCode = ObisCode.fromString("7.0.1.60.2.255");

    public static final ObisCode StandingCharge = ObisCode.fromString("0.0.61.2.0.255");
    public static final ObisCode Currency = ObisCode.fromString("0.0.61.1.0.255");
    public static final ObisCode PriceMatrix = ObisCode.fromString("0.0.61.0.1.255");

    /**
     * Util class, so made constructor private
     */
    private ObisCodeProvider() {
        // Nothing to do here
    }
}
