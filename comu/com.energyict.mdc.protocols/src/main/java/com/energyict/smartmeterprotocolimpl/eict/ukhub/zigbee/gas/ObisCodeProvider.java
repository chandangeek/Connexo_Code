/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;

public final class ObisCodeProvider {


    public static final ObisCode OperationalFirmwareMonlicitic = ObisCode.fromString("7.0.0.2.0.255");
    public static final ObisCode OperationalFirmwareMID = ObisCode.fromString("7.1.0.2.0.255");
    public static final ObisCode OperationalFirmwareNonMIDApp = ObisCode.fromString("7.2.0.2.0.255");
    public static final ObisCode OperationalFirmwareBootloader = ObisCode.fromString("7.3.0.2.0.255");
    public static final ObisCode OperationalFirmwareZCLVersion = ObisCode.fromString("0.4.0.2.0.255");
    public static final ObisCode OperationalFirmwareStackVersion = ObisCode.fromString("0.5.0.2.0.255");
    public static final ObisCode OperationalFirmwareZigbeeChip = ObisCode.fromString("0.6.0.2.0.255");
    public static final ObisCode OperationalFirmwareHAN = ObisCode.fromString("0.7.0.2.0.255");
    public static final ObisCode OperationalFirmwareWAN = ObisCode.fromString("0.8.0.2.0.255");

    public static final ObisCode DeviceId1 = ObisCode.fromString("0.0.96.1.0.255");     // SerialNumber
    public static final ObisCode DeviceId2 = ObisCode.fromString("0.0.96.1.1.255");     // UtilitySpecified EquipmentID
    public static final ObisCode DeviceId3 = ObisCode.fromString("0.0.96.1.2.255");     //E-Function location details, e.g. 48 chars (maybe need removing)
    public static final ObisCode DeviceId4 = ObisCode.fromString("0.0.96.1.3.255");     //E-location information ? 48 chars
    public static final ObisCode DeviceId5 = ObisCode.fromString("0.0.96.1.4.255");     //E-configuration information ? 16 chars
    public static final ObisCode DeviceId6 = ObisCode.fromString("0.0.96.1.5.255");     //Manufacturer Name
    public static final ObisCode DeviceId7 = ObisCode.fromString("0.0.96.1.6.255");     //Manufacture ID (ZigBee MSP ID [SSWG code for Clusters])
    public static final ObisCode DeviceId8 = ObisCode.fromString("0.0.96.1.7.255");     //PAYG ID
    public static final ObisCode DeviceId9 =ObisCode.fromString("0.0.96.1.8.255");
    public static final ObisCode DeviceId10 = ObisCode.fromString("0.0.96.1.9.255");     //Serial Number of Module
    public static final ObisCode MeteringPointId = ObisCode.fromString("0.0.96.1.10.255");     //MPAN or the MPRN
    public static final ObisCode DeviceId50 = ObisCode.fromString("0.0.96.1.50.255");     //hours in operation
    public static final ObisCode DeviceId51 = ObisCode.fromString("0.0.96.1.51.255");     //hours in fault
    public static final ObisCode DeviceId52 = ObisCode.fromString("0.0.96.1.52.255");     //remaining battery life

    public static final ObisCode cotManagement = ObisCode.fromString("0.0.35.10.0.255");    // COT Management

    public static final ObisCode GENERAL_LOAD_PROFILE = ObisCode.fromString("0.0.96.10.1.255");
    public static final ObisCode GENERAL_LP_STATUS_OBISCODE = ObisCode.fromString("0.0.96.10.1.255");

    public static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DISCONNECT_CONTROL_EVENT_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");

    public static final ObisCode COMM_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode PREPAYMENT_EVENT_LOG = ObisCode.fromString("0.0.99.98.7.255");

    public static final ObisCode NOTIFICATION_FLAGS_EVENT_LOG = ObisCode.fromString("0.0.99.98.8.255");
    public static final ObisCode TARIFF_UPDATES_EVENT_LOG = ObisCode.fromString("0.0.99.98.9.255");
    public static final ObisCode MIRROR_UPDATES_EVENT_LOG = ObisCode.fromString("0.0.99.98.10.255");

   public static final ObisCode MANUFACTURER_EVENT_LOG = ObisCode.fromString("0.0.99.98.31.255");

    public static final ObisCode TEXT_MSG_SEND_EVENT_LOG = ObisCode.fromString("0.0.99.98.20.255");
    public static final ObisCode TEXT_MSG_RESPONSE_EVENT_LOG = ObisCode.fromString("0.0.99.98.21.255");

    public static final UniversalObject[] OBJECT_LIST;

    public static final ObisCode REG_CLOCK_TIME_SHIFT_EVENT_LIMIT = ObisCode.fromString("7.0.0.9.11.255");
    public static final ObisCode REG_CLOCK_SYNC_WINDOW = ObisCode.fromString("7.0.0.9.9.255");
    public static final ObisCode REG_ERROR = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode REG_ALARM = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode REG_TIME_SINCE_LAST_EOB_1 = ObisCode.fromString("7.0.0.9.0.255");

    public static final ObisCode REG_GAS_VOLUME_TOTAL = ObisCode.fromString("7.0.1.8.0.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_1 = ObisCode.fromString("7.0.1.8.1.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_2 = ObisCode.fromString("7.0.1.8.2.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_3 = ObisCode.fromString("7.0.1.8.3.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_4 = ObisCode.fromString("7.0.1.8.4.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_5 = ObisCode.fromString("7.0.1.8.5.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_6 = ObisCode.fromString("7.0.1.8.6.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_7 = ObisCode.fromString("7.0.1.8.7.255");
    public static final ObisCode REG_GAS_VOLUME_TOU_8 = ObisCode.fromString("7.0.1.8.8.255");

    public static final ObisCode REG_PROFILE_DEMAND = ObisCode.fromString("7.0.1.25.0.255");
    public static final ObisCode REG_DEMAND_TOTAL = ObisCode.fromString("7.0.1.4.0.255");
    public static final ObisCode REG_MAXIMUM_DEMAND_ENERGY_IMPORT = ObisCode.fromString("7.0.1.6.1.255");

    public static final ObisCode ACTIVITY_CALENDER = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode SPECIAL_DAY_TABLE = ObisCode.fromString("0.0.11.0.0.255");

    public static final ObisCode FIRMWARE_UPDATE = ObisCode.fromString("0.0.44.0.0.255");
    public static final ObisCode IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");

    static {
        OBJECT_LIST = new UniversalObject[]{
                new UniversalObject(ObisCode.fromString("0.0.41.0.0.255"), DLMSClassId.SAP_ASSIGNMENT),
                new UniversalObject(ObisCode.fromString("0.0.40.0.1.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.2.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.3.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.4.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.5.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.0.255"), DLMSClassId.ASSOCIATION_LN),

                new UniversalObject(ObisCode.fromString("0.0.43.0.1.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.2.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.3.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.4.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.5.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.0.255"), DLMSClassId.SECURITY_SETUP),

                new UniversalObject(REG_CLOCK_TIME_SHIFT_EVENT_LIMIT, DLMSClassId.REGISTER),
                new UniversalObject(REG_CLOCK_SYNC_WINDOW, DLMSClassId.REGISTER),
                new UniversalObject(REG_ERROR, DLMSClassId.REGISTER),
                new UniversalObject(REG_ALARM, DLMSClassId.REGISTER),
                new UniversalObject(REG_TIME_SINCE_LAST_EOB_1, DLMSClassId.REGISTER),

                new UniversalObject(REG_GAS_VOLUME_TOTAL, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_1, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_2, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_3, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_4, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_5, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_6, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_7, DLMSClassId.REGISTER),
                new UniversalObject(REG_GAS_VOLUME_TOU_8, DLMSClassId.REGISTER),

                new UniversalObject(REG_PROFILE_DEMAND, DLMSClassId.REGISTER),
                new UniversalObject(REG_DEMAND_TOTAL, DLMSClassId.DEMAND_REGISTER),
                new UniversalObject(REG_MAXIMUM_DEMAND_ENERGY_IMPORT, DLMSClassId.EXTENDED_REGISTER),

                new UniversalObject(ObisCode.fromString("7.0.1.4.0.255"), DLMSClassId.DEMAND_REGISTER),
                new UniversalObject(ObisCode.fromString("7.0.1.6.1.255"), DLMSClassId.EXTENDED_REGISTER),

                new UniversalObject(ObisCode.fromString("0.0.99.98.0.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.1.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.2.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.3.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.97.6.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.97.7.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.97.20.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.97.21.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.98.1.0.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.98.1.1.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.2.0.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.2.1.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("7.0.99.1.0.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.8.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.9.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.99.98.10.255"), DLMSClassId.PROFILE_GENERIC),

                new UniversalObject(ObisCode.fromString("0.0.10.0.100.255"), DLMSClassId.SCRIPT_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.10.1.100.255"), DLMSClassId.SCRIPT_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.10.0.106.255"), DLMSClassId.SCRIPT_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.10.0.1.255"), DLMSClassId.SCRIPT_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.10.0.107.255"), DLMSClassId.SCRIPT_TABLE),

                new UniversalObject(ObisCode.fromString("0.0.15.0.1.255"), DLMSClassId.SINGLE_ACTION_SCHEDULE),
                new UniversalObject(ObisCode.fromString("0.0.15.0.0.255"), DLMSClassId.SINGLE_ACTION_SCHEDULE),
                new UniversalObject(ObisCode.fromString("0.0.15.0.2.255"), DLMSClassId.SINGLE_ACTION_SCHEDULE),

                new UniversalObject(ObisCode.fromString("0.0.14.0.1.255"), DLMSClassId.REGISTER_ACTIVATION),
                new UniversalObject(ObisCode.fromString("0.0.14.0.7.255"), DLMSClassId.REGISTER_ACTIVATION),

                new UniversalObject(ACTIVITY_CALENDER, DLMSClassId.ACTIVITY_CALENDAR),
                new UniversalObject(SPECIAL_DAY_TABLE, DLMSClassId.SPECIAL_DAYS_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.96.3.10.255"), DLMSClassId.DISCONNECT_CONTROL),
                new UniversalObject(ObisCode.fromString("0.0.44.0.0.255"), DLMSClassId.IMAGE_TRANSFER),
                new UniversalObject(ObisCode.fromString("0.0.1.0.0.255"), DLMSClassId.CLOCK),

                new UniversalObject(ObisCode.fromString("0.0.42.0.0.255"), DLMSClassId.DATA),

                new UniversalObject(DeviceId1, DLMSClassId.DATA),
                new UniversalObject(DeviceId2, DLMSClassId.DATA),
                new UniversalObject(DeviceId3, DLMSClassId.DATA),
                new UniversalObject(DeviceId4, DLMSClassId.DATA),
                new UniversalObject(DeviceId5, DLMSClassId.DATA),
                new UniversalObject(DeviceId6, DLMSClassId.DATA),
                new UniversalObject(DeviceId7, DLMSClassId.DATA),
                new UniversalObject(DeviceId8, DLMSClassId.DATA),
                new UniversalObject(DeviceId10, DLMSClassId.DATA),
                new UniversalObject(MeteringPointId, DLMSClassId.DATA),
                new UniversalObject(DeviceId50, DLMSClassId.DATA),
                new UniversalObject(DeviceId51, DLMSClassId.DATA),
                new UniversalObject(DeviceId52, DLMSClassId.DATA),

                new UniversalObject(OperationalFirmwareMonlicitic, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareMID, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareNonMIDApp, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareBootloader, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareZCLVersion, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareStackVersion, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareZigbeeChip, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareHAN, DLMSClassId.DATA),
                new UniversalObject(OperationalFirmwareWAN, DLMSClassId.DATA),

                new UniversalObject(ObisCode.fromString("7.0.0.2.8.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("7.0.0.9.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("7.0.0.9.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.10.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.11.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.14.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.97.98.10.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.7.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.7.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.7.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.13.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.13.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.13.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.10.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.10.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("1.0.1.65.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("7.0.1.65.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.60.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.8.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.8.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.8.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.9.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.93.44.9.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.9.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.35.6.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.10.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.10.255"), DLMSClassId.DATA)
        };
    }


    /**
     * Util class, so made constructor private
     */
    private ObisCodeProvider() {
        // Nothing to do here
    }

}
