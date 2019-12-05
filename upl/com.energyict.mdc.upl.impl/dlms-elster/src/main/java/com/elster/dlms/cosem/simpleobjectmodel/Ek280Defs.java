/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/Ek280Defs.java $
 * Version:
 * $Id: Ek280Defs.java 6380 2013-03-28 14:46:45Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 4:23:08 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.ObisCode;

/**
 * This interface provides definitions for OBIS-Codes used by the EK280.
 *
 * @author osse
 */
public final class Ek280Defs extends CommonDefs
{
  private Ek280Defs()
  {
    super(); //for PMD
    //--- no Objects allowed
  }

  public static final ObisCode EQUIPMENT_CLASS = new ObisCode(7, 0, 0, 0, 9, 255);
  public static final ObisCode BILLING_STATUS_REGISTER = new ObisCode(0, 0, 96, 10, 2, 255);
  public static final ObisCode GSM_SIGNAL_STRENGTH = new ObisCode(0, 3, 96, 12, 5, 255);
  public static final ObisCode GSM_PHONE_NUMBER = new ObisCode(0, 3, 96, 12, 6, 255);
  public static final ObisCode BATTERY_REMAINING = new ObisCode(0, 0, 96, 6, 6, 255);
  public static final ObisCode REMAINING_SHIFT_TIME = new ObisCode(7, 0, 0, 9, 4, 255);
  public static final ObisCode MODEM_BATTERY_VOLTAGE = new ObisCode(0,2,96,6,3,255);
  
  //public static final ObisCode DEVICE_STATUS = new ObisCode(0,2,96,10,1,255 ); //See STATUS_REGISTER_1_2
  public static final ObisCode MOMENTARY_STATUS_TOTAL = new ObisCode(7,129,96,5,1,255 );
  public static final ObisCode STATUS_REGISTER_TOTAL = new ObisCode(7,129,96,5,101,255);
  
  public static final ObisCode DEVICE_CONDITION_ITALY = new ObisCode( 7, 0, 96, 99, 100, 255);

  
  public static final ObisCode INSTALLATION_DATE = new ObisCode(0,0,96,52,0,255);
  public static final ObisCode EQUIPMENT_CONFIG = new ObisCode(7,0,0,2,0,255);
  public static final ObisCode METERING_POINT_ID = new ObisCode(0,0,96,1,10,255);
  public static final ObisCode DLMS_DEVICE_SERIAL= new ObisCode(7,0,0,2,13,255);
  public static final ObisCode INST_METER_TYPE = new ObisCode(7,0,96,99,3,255);
  public static final ObisCode INST_METER_CALIBER = new ObisCode(7,0,96,99,2,255);
  public static final ObisCode INST_METER_SERIAL= new ObisCode(7,0,0,2,14,255);
  public static final ObisCode NUMBER_OF_PRE_DECIMAL_PLACES= new ObisCode(7,0,96,99,5,255);
  
  
  
  public static final ObisCode VM_TOTAL_CURR = new ObisCode(7,0,13,0,0,255);
  public static final ObisCode VB_TOTAL_CURR = new ObisCode(7,0,13,2,0,255);

  public static final ObisCode VM_UNDISTURBED_TOTAL_CURR_PERIOD = new ObisCode(7,0,11,83,0,255);
  public static final ObisCode VM_UNDISTURBED_F1_CURR_PERIOD = new ObisCode(7,0,11,83,1,255);
  public static final ObisCode VM_UNDISTURBED_F2_CURR_PERIOD = new ObisCode(7,0,11,83,2,255);
  public static final ObisCode VM_UNDISTURBED_F3_CURR_PERIOD = new ObisCode(7,0,11,83,3,255);
  public static final ObisCode VM_UNDISTURBED_TOTAL_PREV_PERIOD = new ObisCode(7,0,11,83,0,101);
  public static final ObisCode VM_UNDISTURBED_F1_PREV_PERIOD = new ObisCode(7,0,11,83,1,101);
  public static final ObisCode VM_UNDISTURBED_F2_PREV_PERIOD = new ObisCode(7,0,11,83,2,101);
  public static final ObisCode VM_UNDISTURBED_F3_PREV_PERIOD = new ObisCode(7,0,11,83,3,101);
  
  public static final ObisCode VM_DISTURBED_TOTAL_CURR_PERIOD = new ObisCode(7,0,12,81,0,255);
  public static final ObisCode VM_DISTURBED_F1_CURR_PERIOD = new ObisCode(7,0,12,81,1,255);
  public static final ObisCode VM_DISTURBED_F2_CURR_PERIOD = new ObisCode(7,0,12,81,2,255);
  public static final ObisCode VM_DISTURBED_F3_CURR_PERIOD = new ObisCode(7,0,12,81,3,255);
  public static final ObisCode VM_DISTURBED_TOTAL_PREV_PERIOD = new ObisCode(7,0,12,81,0,101);
  public static final ObisCode VM_DISTURBED_F1_PREV_PERIOD = new ObisCode(7,0,12,81,1,101);
  public static final ObisCode VM_DISTURBED_F2_PREV_PERIOD = new ObisCode(7,0,12,81,2,101);
  public static final ObisCode VM_DISTURBED_F3_PREV_PERIOD = new ObisCode(7,0,12,81,3,101);
  
  public static final ObisCode PRESSURE_REFERENCE = new ObisCode(7,0,42,2,0,255);
  public static final ObisCode TEMPERATURE_REFERENCE = new ObisCode(7,0,41,2,0,255);
  public static final ObisCode PRESSURE_ABSOLUTE_CURR = new ObisCode(7,0,42,0,0,255);
  public static final ObisCode TEMPERATURE_CURR = new ObisCode(7,0,41,0,0,255);
  public static final ObisCode FLOWRATE_VM_CURR = new ObisCode(7,0,43,0,0,255);
  public static final ObisCode FLOWRATE_VB_CURR = new ObisCode(7,0,43,2,0,255);
  
  public static final ObisCode FLOWRATE_CONV_MAX_CURR_DAY = new ObisCode(7,0,43,153,0,255);
  public static final ObisCode FLOWRATE_CONV_MAX_PREV_DAY = new ObisCode(7,0,43,153,0,101);

  
  public static final ObisCode COEFFICIENT_C_CURR = new ObisCode(7,0,52,0,0,255);
  public static final ObisCode COEFFICIENT_Z_CURR = new ObisCode(7,0,53,0,0,255);
  public static final ObisCode Z_CALC_METHOD = new ObisCode(7,0,53,12,0,255);
  public static final ObisCode Z_CALC_METHOD_CODE = new ObisCode(7,0,0,4,2,255);
  public static final ObisCode VOLUME_CALC_METHOD = new ObisCode(7,0,0,4,0,255);
  
  public static final ObisCode DENSITY_GAS_BASE_COND = new ObisCode(7,0,0,12,45,255);
  public static final ObisCode DENSITY_RATIO = new ObisCode(7,0,0,12,46,255);
  public static final ObisCode CALORIFIC_VALUE_COMP_CURR = new ObisCode(7,0,0,12,54,255);
  public static final ObisCode GAV_NITROGEN_CONT_CURR = new ObisCode(7,0,0,12,60,255);
  public static final ObisCode GAV_HYDROGEN_CONT_CURR = new ObisCode(7,0,0,12,61,255);
  public static final ObisCode GAV_CARBONOXID_CONT_CURR = new ObisCode(7,0,0,12,65,255);
  public static final ObisCode GAV_CARBONDIOXID_CONT_CURR = new ObisCode(7,0,0,12,66,255);
  public static final ObisCode GAV_METHAN_CONT_CURR = new ObisCode(7,0,0,12,67,255);
  
  
  
  //Tariff data
  public static final ObisCode SPECIAL_DAYS_TABLE = new ObisCode(0, 0, 11, 0, 0, 255);
  public static final ObisCode ACTIVITY_CALENDAR = new ObisCode(0, 0, 13, 0, 0, 255);
  public static final ObisCode DEFAULT_TARIF_REGISTER = new ObisCode(0, 0, 96, 14, 15, 255);
  
  // profiles
  public static final ObisCode LOAD_PROFILE_60 = new ObisCode(7, 0, 99, 99, 2, 255);
  public static final ObisCode EVENT_LOG = new ObisCode(7, 0, 99, 98, 0, 255);
  public static final ObisCode CERT_DATA_LOG = new ObisCode(7, 0, 99, 99, 0, 255);
  //captured objects in profile 60
  public static final ObisCode AONO_A3 = new ObisCode(0, 128, 96, 8, 67, 255);
  public static final ObisCode VOLUME_AT_BASE_CONDITIONS = new ObisCode(7, 0, 11, 2, 0, 255);
  public static final ObisCode VOLUME_AT_MEASUREMENT_CONDITIONS = new ObisCode(7, 0, 11, 0, 0, 255);
  public static final ObisCode VB_WITHIN_LAST_MEASURING_PERIOD = new ObisCode(7, 0, 11, 17, 0, 255);
  public static final ObisCode VM_WITHIN_LAST_MEASURING_PERIOD = new ObisCode(7, 0, 11, 15, 0, 255);
  public static final ObisCode MEAN_VALUE_P_OF_LAST_MEAS_PERIOD = new ObisCode(7, 0, 42, 42, 0, 255);
  public static final ObisCode MEAN_VALUE_T_OF_LAST_MEAS_PERIOD = new ObisCode(7, 0, 41, 42, 0, 255);
  public static final ObisCode STATUS_REGISTER_1_2 = new ObisCode(0, 2, 96, 10, 1, 255);
  public static final ObisCode TRIGGER_EVENT_A3 = new ObisCode(7, 128, 96, 5, 67, 255);
  //captured objects in event log
  public static final ObisCode AONO_A9 = new ObisCode(0, 128, 96, 8, 74, 255);
  public static final ObisCode EVENT_COUNTER_2 = new ObisCode(0, 0, 96, 15, 2, 255);
  public static final ObisCode STATUS_REGISTER_1_6 = new ObisCode(0, 6, 96, 10, 1, 255);
  public static final ObisCode TRIGGER_EVENT_A9 = new ObisCode(7, 128, 96, 5, 74, 255);
  //captured objects in ertification data log
  public static final ObisCode AONO_A8 = new ObisCode(0, 128, 96, 8, 73, 255);
  public static final ObisCode EVENT_COUNTER_1 = new ObisCode(0, 0, 96, 15, 1, 255);
  public static final ObisCode STATUS_REGISTER_1_7 = new ObisCode(0, 7, 96, 10, 1, 255);
  public static final ObisCode TRIGGER_EVENT_A8 = new ObisCode(7, 128, 96, 5, 73, 255);
  //gprs setup
  public static final ObisCode GPRS_MODEM_SETUP = new ObisCode(0, 0, 25, 4, 0, 255);
  //installation data
  public static final ObisCode CP_VALUE = new ObisCode(7, 1, 0, 7, 2, 255);
  public static final ObisCode GAS_DAY_START_TIME = new ObisCode(7, 0, 0, 9, 3, 255);
  public static final ObisCode COMPRESSIBILITY_FACTOR_BASE_COND = new ObisCode(7, 0, 53, 2, 0, 255);
  public static final ObisCode SUPPORTED_Z_COMP_FORMULA = new ObisCode(7, 0, 0, 4, 1, 255);
  public static final ObisCode CURRENT_Z_COMP_FORMULA = new ObisCode(7, 0, 0, 4, 2, 255);
  public static final ObisCode GROSS_COLORIFIC_VALUE = new ObisCode(7, 0, 0, 12, 54, 255);
  //auto connect
  public static final ObisCode AUTO_CONNECT_1 = new ObisCode(0, 1, 2, 1, 0, 255);
  public static final ObisCode AUTO_CONNECT_2 = new ObisCode(0, 2, 2, 1, 0, 255);
  //auto answer 
  public static final ObisCode AUTO_ANSWER_1 = new ObisCode(0, 1, 2, 2, 0, 255);
  public static final ObisCode AUTO_ANSWER_2 = new ObisCode(0, 2, 2, 2, 0, 255);
  public static final ObisCode AUTO_ANSWER_3 = new ObisCode(0, 3, 2, 2, 0, 255);
  public static final ObisCode AUTO_ANSWER_4 = new ObisCode(0, 4, 2, 2, 0, 255);
  public static final ObisCode AUTO_ANSWER_5 = new ObisCode(0, 5, 2, 2, 0, 255);
  public static final ObisCode AUTO_ANSWER_6 = new ObisCode(0, 6, 2, 2, 0, 255);
  //security setup
  public static final ObisCode SECURITY_SETUP_OBJECT_30 = new ObisCode(0, 0, 43, 0, 30, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_31 = new ObisCode(0, 0, 43, 0, 31, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_32 = new ObisCode(0, 0, 43, 0, 32, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_33 = new ObisCode(0, 0, 43, 0, 33, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_34 = new ObisCode(0, 0, 43, 0, 34, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_35 = new ObisCode(0, 0, 43, 0, 35, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_36 = new ObisCode(0, 0, 43, 0, 36, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_37 = new ObisCode(0, 0, 43, 0, 37, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_38 = new ObisCode(0, 0, 43, 0, 38, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_39 = new ObisCode(0, 0, 43, 0, 39, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_40 = new ObisCode(0, 0, 43, 0, 40, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_41 = new ObisCode(0, 0, 43, 0, 41, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_42 = new ObisCode(0, 0, 43, 0, 42, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_43 = new ObisCode(0, 0, 43, 0, 43, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_44 = new ObisCode(0, 0, 43, 0, 44, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_45 = new ObisCode(0, 0, 43, 0, 45, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_46 = new ObisCode(0, 0, 43, 0, 46, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_47 = new ObisCode(0, 0, 43, 0, 47, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_48 = new ObisCode(0, 0, 43, 0, 48, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_49 = new ObisCode(0, 0, 43, 0, 49, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_50 = new ObisCode(0, 0, 43, 0, 50, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_51 = new ObisCode(0, 0, 43, 0, 51, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_52 = new ObisCode(0, 0, 43, 0, 52, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_53 = new ObisCode(0, 0, 43, 0, 53, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_54 = new ObisCode(0, 0, 43, 0, 54, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_55 = new ObisCode(0, 0, 43, 0, 55, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_56 = new ObisCode(0, 0, 43, 0, 56, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_57 = new ObisCode(0, 0, 43, 0, 57, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_58 = new ObisCode(0, 0, 43, 0, 58, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_59 = new ObisCode(0, 0, 43, 0, 59, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_60 = new ObisCode(0, 0, 43, 0, 60, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_61 = new ObisCode(0, 0, 43, 0, 61, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_62 = new ObisCode(0, 0, 43, 0, 62, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_63 = new ObisCode(0, 0, 43, 0, 63, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_64 = new ObisCode(0, 0, 43, 0, 64, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_65 = new ObisCode(0, 0, 43, 0, 65, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_66 = new ObisCode(0, 0, 43, 0, 66, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_67 = new ObisCode(0, 0, 43, 0, 67, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_68 = new ObisCode(0, 0, 43, 0, 68, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_69 = new ObisCode(0, 0, 43, 0, 69, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_70 = new ObisCode(0, 0, 43, 0, 70, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_71 = new ObisCode(0, 0, 43, 0, 71, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_72 = new ObisCode(0, 0, 43, 0, 72, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_73 = new ObisCode(0, 0, 43, 0, 73, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_74 = new ObisCode(0, 0, 43, 0, 74, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_75 = new ObisCode(0, 0, 43, 0, 75, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_76 = new ObisCode(0, 0, 43, 0, 76, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_77 = new ObisCode(0, 0, 43, 0, 77, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_78 = new ObisCode(0, 0, 43, 0, 78, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_79 = new ObisCode(0, 0, 43, 0, 79, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_80 = new ObisCode(0, 0, 43, 0, 80, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_81 = new ObisCode(0, 0, 43, 0, 81, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_82 = new ObisCode(0, 0, 43, 0, 82, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_83 = new ObisCode(0, 0, 43, 0, 83, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_84 = new ObisCode(0, 0, 43, 0, 84, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_85 = new ObisCode(0, 0, 43, 0, 85, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_86 = new ObisCode(0, 0, 43, 0, 86, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_87 = new ObisCode(0, 0, 43, 0, 87, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_88 = new ObisCode(0, 0, 43, 0, 88, 255);
  public static final ObisCode SECURITY_SETUP_OBJECT_89 = new ObisCode(0, 0, 43, 0, 89, 255);
  public static final SimpleCosemObjectDefinition[] DEFINITIONS =
  {
    // common objects
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, SERIAL_NUMBER),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, DEVICE_TYPE),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, SOFTWARE_VERSION),
    new SimpleCosemObjectDefinition(CosemClassIds.CLOCK, 0, CLOCK_OBJECT),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, DLMS_DEVICE_SERIAL),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, EQUIPMENT_CLASS),
    new SimpleCosemObjectDefinition(CosemClassIds.ASSOCIATION_LN, 1, CURRENT_ASSOCIATION),
    
    // status
    //new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, DEVICE_STATUS), //see Definition of STATUS_REGISTER_1_2
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, DEVICE_CONDITION_ITALY),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, MOMENTARY_STATUS_TOTAL),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, STATUS_REGISTER_TOTAL),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BILLING_STATUS_REGISTER),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GSM_SIGNAL_STRENGTH),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, GSM_PHONE_NUMBER),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, BATTERY_REMAINING),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, REMAINING_SHIFT_TIME),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, MODEM_BATTERY_VOLTAGE),

    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, INSTALLATION_DATE),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, EQUIPMENT_CONFIG),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, METERING_POINT_ID),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, INST_METER_TYPE),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, INST_METER_CALIBER),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, INST_METER_SERIAL),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, NUMBER_OF_PRE_DECIMAL_PLACES),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_TOTAL_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VB_TOTAL_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, CP_VALUE),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_TOTAL_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F1_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F2_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F3_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_TOTAL_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F1_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F2_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_UNDISTURBED_F3_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_TOTAL_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F1_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F2_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F3_CURR_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_TOTAL_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F1_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F2_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VM_DISTURBED_F3_PREV_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, PRESSURE_REFERENCE),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, TEMPERATURE_REFERENCE),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, PRESSURE_ABSOLUTE_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, TEMPERATURE_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, FLOWRATE_VM_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, FLOWRATE_VB_CURR),

    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, FLOWRATE_CONV_MAX_CURR_DAY),
    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, FLOWRATE_CONV_MAX_PREV_DAY),
    
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, COEFFICIENT_C_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, COEFFICIENT_Z_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, Z_CALC_METHOD),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, Z_CALC_METHOD_CODE),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, VOLUME_CALC_METHOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, DENSITY_GAS_BASE_COND),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, DENSITY_RATIO),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, CALORIFIC_VALUE_COMP_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GAV_NITROGEN_CONT_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GAV_HYDROGEN_CONT_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GAV_CARBONOXID_CONT_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GAV_CARBONDIOXID_CONT_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, GAV_METHAN_CONT_CURR),
    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, LOAD_PROFILE_60),
    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, EVENT_LOG),
    new SimpleCosemObjectDefinition(CosemClassIds.PROFILE_GENERIC, 1, CERT_DATA_LOG),
    //captured objects in profile
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, AONO_A3),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VOLUME_AT_BASE_CONDITIONS),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, VOLUME_AT_MEASUREMENT_CONDITIONS),
    //captured objects in event log
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, AONO_A9),
    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, EVENT_COUNTER_2),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, STATUS_REGISTER_1_6),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, TRIGGER_EVENT_A9),
    //captured objects in ertification data log
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, AONO_A8),
    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, EVENT_COUNTER_1),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, STATUS_REGISTER_1_7),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, TRIGGER_EVENT_A8),
    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, VB_WITHIN_LAST_MEASURING_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.EXTENDED_REGISTER, 0, VM_WITHIN_LAST_MEASURING_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, MEAN_VALUE_P_OF_LAST_MEAS_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, MEAN_VALUE_T_OF_LAST_MEAS_PERIOD),
    new SimpleCosemObjectDefinition(CosemClassIds.REGISTER, 0, STATUS_REGISTER_1_2),
    new SimpleCosemObjectDefinition(CosemClassIds.DATA, 0, TRIGGER_EVENT_A3),
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
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_CONNECT, 1, AUTO_CONNECT_2),
    //Auto answer
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_1),
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_2),
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_3),
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_4),
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_5),
    new SimpleCosemObjectDefinition(CosemClassIds.AUTO_ANSWER, 0, AUTO_ANSWER_6),
    //security setup          
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_30),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_31),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_32),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_33),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_34),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_35),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_36),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_37),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_38),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_39),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_40),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_41),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_42),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_43),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_44),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_45),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_46),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_47),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_48),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_49),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_50),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_51),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_52),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_53),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_54),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_55),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_56),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_57),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_58),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_59),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_60),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_61),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_62),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_63),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_64),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_65),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_66),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_67),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_68),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_69),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_70),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_71),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_72),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_73),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_74),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_75),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_76),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_77),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_78),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_79),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_80),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_81),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_82),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_83),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_84),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_85),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_86),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_87),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_88),
    new SimpleCosemObjectDefinition(CosemClassIds.SECURITY_SETUP, 0, SECURITY_SETUP_OBJECT_89),
  };
}
