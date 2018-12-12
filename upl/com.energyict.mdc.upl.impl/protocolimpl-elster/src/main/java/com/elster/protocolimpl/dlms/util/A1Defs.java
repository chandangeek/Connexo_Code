package com.elster.protocolimpl.dlms.util;

import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectDefinition;
import com.elster.dlms.types.basic.ObisCode;

/**
 * User: heuckeg
 * Date: 10.04.13
 * Time: 16:27
 */
public class A1Defs
{

    private A1Defs()
    {
        super(); //for PMD
        //--- no Objects allowed
    }

    public static final ObisCode CURRENT_ASSOCIATION = new ObisCode("0.0.40.0.0.255");

    public static final ObisCode SOFTWARE_VERSION = new ObisCode("7.0.0.2.1.255");
    public static final ObisCode SERIAL_NUMBER = new ObisCode("0.0.96.1.0.255");
    public static final ObisCode CLOCK_OBJECT = new ObisCode("0.0.1.0.0.255");

    public static final ObisCode SYNC_REG = new ObisCode("0.0.94.39.20.255");

    public static final ObisCode VM_TOTAL_CURR = new ObisCode("7.0.12.2.0.255");
    public static final ObisCode VB_TOTAL_CURR = new ObisCode("7.0.13.2.0.255");

    public static final ObisCode BATTERY_VOLTAGE = new ObisCode("0.0.96.6.3.255");
    public static final ObisCode BATTERY_INITAL_ENERGY = new ObisCode("0.0.96.6.4.255");
    public static final ObisCode BATTERY_INSTALL_TIME = new ObisCode("0.0.96.6.5.255");
    public static final ObisCode BATTERY_REMAINING_TIME = new ObisCode("0.0.96.6.6.255");

    public static final ObisCode BATTERY_VOLTAGE_MODEM = new ObisCode("0.128.96.6.3.255");
    public static final ObisCode BATTERY_INITAL_ENERGY_MODEM = new ObisCode("0.128.96.6.4.255");
    public static final ObisCode BATTERY_INSTALL_TIME_MODEM = new ObisCode("0.128.96.6.5.255");
    public static final ObisCode BATTERY_REMAINING_TIME_MODEM = new ObisCode("0.128.96.6.6.255");

    public static final ObisCode TOTAL_OPERATION_TIME = new ObisCode("0.0.96.8.0.255");

    public static final ObisCode DEVICE_CONFIG_INFO_FOR_TOOL = new ObisCode("7.1.96.128.4.255");
    public static final ObisCode DEVICE_CONFIG_INFO_FOR_DCS = new ObisCode("7.2.96.128.4.255");

    public static final ObisCode IMAGE_TRANSFER = new ObisCode("0.0.44.0.0.255");

    public static final ObisCode METERING_POINT_ID = new ObisCode("0.0.96.1.10.255");

    public static final ObisCode SNAPSHOT_PERIOD_COUNTER = new ObisCode("7.0.0.1.0.255");
    public static final ObisCode SNAPSHOT_REASON_CODE = new ObisCode("0.0.96.10.2.255");

    public static final ObisCode UNITS_EVENT_COUNTER = new ObisCode("0.0.96.15.1.255");
    public static final ObisCode UNITS_EVENT_STATUS = new ObisCode("0.0.97.97.1.255");
    public static final ObisCode UNITS_DEVICE_MODE = new ObisCode("7.0.96.5.0.255");
    public static final ObisCode UNITS_DEVICE_DIAGNOSTIC = new ObisCode("7.2.96.5.1.255");

    public static final ObisCode METRO_ALARM_STATUS = new ObisCode("0.0.97.97.3.255");

    public static final ObisCode UMI_ALARM_STATUS = new ObisCode("7.0.0.64.58.255");
    public static final ObisCode UMI_ERROG_REG_EL = new ObisCode("0.0.96.15.7.255");
    public static final ObisCode UMI_ERROG_REG_SEL = new ObisCode("0.0.96.15.6.255");

    public static final ObisCode BEGIN_OF_DAY = new ObisCode("7.0.0.9.23.255");
    public static final ObisCode GSM_RSSI = new ObisCode("0.128.96.12.5.255");
    public static final ObisCode BEGIN_OF_YEAR = new ObisCode("7.0.0.9.24.255");
    public static final ObisCode BILLING_PERIOD = new ObisCode("7.0.0.8.23.255");
    public static final ObisCode BILLING_PERIOD_START = new ObisCode("7.0.94.39.11.255");

    public static final ObisCode VB_TOTAL_QUANTITY = new ObisCode("7.0.13.83.0.255");
    public static final ObisCode VB_F1 = new ObisCode("7.0.13.83.1.255");
    public static final ObisCode VB_F2 = new ObisCode("7.0.13.83.2.255");
    public static final ObisCode VB_F3 = new ObisCode("7.0.13.83.3.255");
    public static final ObisCode VAA = new ObisCode("7.0.12.82.0.255");

    public static final ObisCode TEMPERATURE_REFERENCE = new ObisCode("7.0.41.2.0.255");
    public static final ObisCode PRESSURE_REFERENCE = new ObisCode("7.0.42.2.0.255");
    public static final ObisCode PRESSURE_ABSOLUTE_CURR = new ObisCode("7.0.42.0.0.255");
    public static final ObisCode TEMPERATURE_CURR = new ObisCode("7.0.41.0.0.255");

    public static final ObisCode FLOWRATE_CONV_MAX_CURR_DAY = new ObisCode("7.0.43.41.0.255");
    public static final ObisCode FLOWRATE_CONV_MAX_PREV_DAY = new ObisCode("7.0.43.47.0.101");

    //Tariff data
    public static final ObisCode SPECIAL_DAYS_TABLE = new ObisCode("0.0.11.0.0.255");
    public static final ObisCode ACTIVITY_CALENDAR = new ObisCode("0.0.13.0.0.255");
    public static final ObisCode DEFAULT_TARIF_REGISTER = new ObisCode("0.0.96.14.15.255");

    // profiles
    public static final ObisCode LOAD_PROFILE_60 = new ObisCode("7.0.99.99.2.255");
    public static final ObisCode LOAD_PROFILE_DLY = new ObisCode("7.0.99.99.3.255");
    public static final ObisCode STD_EVENT_LOG = new ObisCode("7.0.99.98.0.255");
    public static final ObisCode EVENT_LOG = new ObisCode("7.0.99.98.1.255");
    public static final ObisCode METROLOGY_EVENT_LOG = new ObisCode("7.0.99.98.3.255");
    public static final ObisCode BILLING_PROFILE = new ObisCode("7.0.98.11.0.126");

    //captured objects in profile 60
    public static final ObisCode VM_PROFILE_OLD = new ObisCode("7.0.12.81.0.255");
    public static final ObisCode HOURLY_LOAD_PROFILE_STATUS = new ObisCode("0.0.96.10.1.255");
    public static final ObisCode HOURLY_LOAD_PROFILE_STATUS_OLD = new ObisCode("0.2.96.10.1.255");

    //captured objects in event log
    public static final ObisCode UNITS_EVENT = new ObisCode("0.0.96.11.1.255");
    public static final ObisCode METROLOGY_ALARM_EVENT = new ObisCode("0.0.96.15.7.255");

    //captured Objects in daily load profile
    public static final ObisCode DLY_UNITS_DEVICE_DIAG = new ObisCode("7.1.96.5.1.255");
    public static final ObisCode MAX_CONVENTIONAL_QB = new ObisCode("7.0.43.47.0.255");

    //captured objects in certification data log
    public static final ObisCode AONO_A8 = new ObisCode("0.128.96.8.73.255");
    public static final ObisCode USER_PORT = new ObisCode("0.0.96.16.1.255");
    public static final ObisCode STATUS_REGISTER_1_7 = new ObisCode("0.7.96.10.1.255");
    public static final ObisCode TRIGGER_EVENT_A8 = new ObisCode("7.128.96.5.73.255");
    //gprs setup
    public static final ObisCode GPRS_MODEM_SETUP = new ObisCode("0.128.96.194.101.255");
    //installation data
    public static final ObisCode CP_VALUE = new ObisCode("7.1.0.7.2.255");
    //auto connect
    public static final ObisCode AUTO_CONNECT_1 = new ObisCode("0.1.2.1.0.255");
    //security setup
    public static final ObisCode SECURITY_SETUP_OBJECT_30 = new ObisCode("0.0.43.0.30.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_31 = new ObisCode("0.0.43.0.31.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_40 = new ObisCode("0.0.43.0.40.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_41 = new ObisCode("0.0.43.0.41.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_50 = new ObisCode("0.0.43.0.50.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_51 = new ObisCode("0.0.43.0.51.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_60 = new ObisCode("0.0.43.0.60.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_61 = new ObisCode("0.0.43.0.61.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_70 = new ObisCode("0.0.43.0.70.255");
    public static final ObisCode SECURITY_SETUP_OBJECT_80 = new ObisCode("0.0.43.0.80.255");


    public static final SimpleCosemObjectDefinition[] DEFINITIONS =
            {
                    // common objects
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SERIAL_NUMBER),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SOFTWARE_VERSION),
                    new SimpleCosemObjectDefinition(CosemClassIds.CLOCK, 0, CLOCK_OBJECT),
                    new SimpleCosemObjectDefinition(CosemClassIds.ASSOCIATION_LN, 1, CURRENT_ASSOCIATION),

                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SYNC_REG),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_REMAINING_TIME),

                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SNAPSHOT_PERIOD_COUNTER),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SNAPSHOT_REASON_CODE),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, UNITS_DEVICE_DIAGNOSTIC),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_TOTAL_CURR),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_TOTAL_CURR),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_VOLTAGE),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_INITAL_ENERGY),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_INSTALL_TIME),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_REMAINING_TIME),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_VOLTAGE_MODEM),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_INITAL_ENERGY_MODEM),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_INSTALL_TIME_MODEM),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_REMAINING_TIME_MODEM),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, TOTAL_OPERATION_TIME),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_TOTAL_QUANTITY),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_F1),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_F2),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_F3),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VAA),

                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, UMI_ERROG_REG_EL),
                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, UMI_ERROG_REG_SEL),

                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GSM_RSSI),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, BEGIN_OF_DAY),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, BEGIN_OF_YEAR),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BILLING_PERIOD),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, BILLING_PERIOD_START),

                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, UMI_ALARM_STATUS),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, UNITS_EVENT_STATUS),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, UNITS_DEVICE_MODE),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, METRO_ALARM_STATUS),

                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, METERING_POINT_ID),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, CP_VALUE),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, PRESSURE_REFERENCE),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, TEMPERATURE_REFERENCE),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, PRESSURE_ABSOLUTE_CURR),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, TEMPERATURE_CURR),

                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, FLOWRATE_CONV_MAX_CURR_DAY),
                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, FLOWRATE_CONV_MAX_PREV_DAY),

                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, LOAD_PROFILE_60),
                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, LOAD_PROFILE_DLY),
                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, EVENT_LOG),
                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, STD_EVENT_LOG),
                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, BILLING_PROFILE),
                    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, METROLOGY_EVENT_LOG),

                    //captured objects in profile
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, HOURLY_LOAD_PROFILE_STATUS),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, HOURLY_LOAD_PROFILE_STATUS_OLD),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_PROFILE_OLD),

                    //captured Objects in daily load profile
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, DLY_UNITS_DEVICE_DIAG),
                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, MAX_CONVENTIONAL_QB),

                    //captured objects in event log
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, UNITS_EVENT),
                    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, METROLOGY_ALARM_EVENT),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, USER_PORT),

                    //captured objects in certification data log
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, AONO_A8),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, UNITS_EVENT_COUNTER),
                    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, STATUS_REGISTER_1_7),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, TRIGGER_EVENT_A8),

                    //Tariff data
                    new SimpleCosemObjectDefinition(CosemClassIds.SPECIAL_DAYS_TABLE, 0, SPECIAL_DAYS_TABLE),
                    new SimpleCosemObjectDefinition(CosemClassIds.ACTIVITY_CALENDAR, 0, ACTIVITY_CALENDAR),
                    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, DEFAULT_TARIF_REGISTER),

                    //GPRS Modem setup
                    new SimpleCosemObjectDefinition(CosemClassIds.GPRS_MODEM_SETUP, 0, GPRS_MODEM_SETUP),
                    //Image transfer
                    new SimpleCosemObjectDefinition(CosemClassIds.IMAGE_TRANSFER, 0, IMAGE_TRANSFER),
                    //Auto connect
                    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_CONNECT, 1, AUTO_CONNECT_1),
                    //security setup
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_30),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_31),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_40),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_41),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_50),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_51),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_60),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_61),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_70),
                    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_80),
            };


}
