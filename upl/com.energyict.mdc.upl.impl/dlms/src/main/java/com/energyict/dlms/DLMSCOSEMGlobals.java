/*
 * DLMSCOSEMGlobals.java
 *
 * Created on 16 september 2002, 16:38
 */

package com.energyict.dlms;

/**
 *
 * @author  koen
 */
public interface DLMSCOSEMGlobals {
    // TypeDescription
    final byte TYPEDESC_NULL=0x00;
    final byte TYPEDESC_ARRAY=0x01;
    final byte TYPEDESC_STRUCTURE=0x02;
    final byte TYPEDESC_BOOLEAN=0x03;
    final byte TYPEDESC_BITSTRING=0x04;
    final byte TYPEDESC_DOUBLE_LONG=0x05;
    final byte TYPEDESC_DOUBLE_LONG_UNSIGNED=0x06;
    final byte TYPEDESC_FLOATING_POINT=0x07;
    final byte TYPEDESC_OCTET_STRING=0x09;
    final byte TYPEDESC_VISIBLE_STRING=0x0A; 
    final byte TYPEDESC_TIME=0x0B;
    final byte TYPEDESC_BCD=0x0D;
    final byte TYPEDESC_INTEGER=0x0F;
    final byte TYPEDESC_LONG=0x10; // long in DLMS is 2 bytes
    final byte TYPEDESC_UNSIGNED=0x11;
    final byte TYPEDESC_LONG_UNSIGNED=0x12; // unsigned long in DLMS is 2 bytes
    final byte TYPEDESC_COMPACT_ARRAY=0x13;
    final byte TYPEDESC_LONG64=0x14;
    final byte TYPEDESC_ENUM=0x16;
    
    // DLMS Interface class id's    
    final short ICID_DATA=1;
    final short ICID_REGISTER=3;
    final short ICID_EXTENDED_REGISTER=4; // new KV 03042003
    final short ICID_DEMAND_REGISTER=5;
    final short ICID_CLOCK=0x08;
    final short ICID_PROFILE_GENERIC=7;
    final short ICID_SAP=0x0011;
    final short ICID_LNREG=0x0003;
    
    final byte LN_A=0;
    final byte LN_B=1;
    final byte LN_C=2;
    final byte LN_D=3;
    final byte LN_E=4;
    final byte LN_F=5;
    
    // profileMask consts
    final byte CHANNEL_DATA=0;
    final byte MANUFACTURER_SPECIFIC_DATA=1;
    final byte STATUS_DATA=2;
    
    // OBIS code identifiers
    final byte LN_A_ELECTRICITY_RELATED_OBJECTS=1;
    
    final byte LN_D_CURRENT_AVERAGE_1=4;
    final byte LN_D_TIME_INTEGRAL_1=8;
    final byte LN_D_CURRENT_AVERAGE_5=27; 

    final byte ATTR_DATA_VALUE=2;
    
    final byte ATTR_REGISTER_VALUE=2;
    final byte ATTR_REGISTER_SCALER=3;
    
    final byte ATTR_DEMAND_REGISTER_CURRENT_AVERAGE=2;
    final byte ATTR_DEMAND_REGISTER_LAST_AVERAGE=3;
    final byte ATTR_DEMAND_REGISTER_SCALER=4;
    
    final byte ATTR_CLOCK_TIME=2;
    
    final byte ATTR_PROFILEGENERIC_BUFFER=2;
    final byte ATTR_PROFILEGENERIC_CAPTUREOBJECTS=3;
    final byte ATTR_PROFILEGENERIC_CAPTUREPERIOD=4;
    
    // DLMS PDU offsets
    final byte DL_COSEMPDU_OFFSET=0x03;
    final byte DL_COSEMPDU_LENGTH_OFFSET=0x04;
    final byte DL_COSEMPDU_TAG_OFFSET=0x05; 

    final byte DL_COSEMPDU_OFFSET_CID=0x06;
    final byte DL_COSEMPDU_OFFSET_LN=0x08;
    final byte DL_COSEMPDU_OFFSET_ATTR=14;
    final byte DL_COSEMPDU_OFFSET_ACCESS_SELECTOR=15;
    
    //********************************************************************************************************
    // Fix LN references for the SL7000
    final byte[] ASSOC_LN_OBJECT_LN={0,0,40,0,0,(byte)255};
    final byte[] SAP_OBJECT_LN={0,0,41,0,0,(byte)255};
    final byte[] CLOCK_OBJECT_LN={0,0,1,0,0,(byte)255};
    final byte[] HISTORIC_VALUES_OBJECT_LN={0,1,98,1,0,101};
    final byte[] LOAD_PROFILE_LN={0,0,99,1,0,(byte)255}; //    
    final byte[] LOGBOOK_PROFILE_LN={0,0,99,98,0,(byte)255};
    final byte[] IPV4_SETUP = {0,0,25,1,0,(byte)255};
    
    //********************************************************************************************************
    // Reserved SN logical name constants, should be filled in when requesting the object list first.
    // ASSOC SN OBJECT
    final short ASSOC_SN_OBJECT=(short)0xFA00;
    final short ASSOC_SN_ATTR_OBJ_LST=(short)8;

    final short ASSOC_LN_ATTR_OBJ_LST=(short)8;
    
    // LNREG OBJECT
    final short LNREG_OBJECT_SN=(short)0xFD00;
    final short LNREG_ATTR_VALUE=(short)8;
    
    // SAP OBJECT
    final short SAP_OBJECT_SN=(short)0xFC00;
    final short SAP_ATTR_ASSIGNMENT_LIST=(short)8;
    
    // **********************************************************************************************
    // Offsets for interface classes
    // The LN reference attributes = ((sn attr offset)/8 + 1)
    
    // GENERAL
    final short ALL_CLASSES_LOGICAL_NAME=(short)0; // attr1 = 0/8+1 = 1 (for ln)
    
    // CLOCK
    final short TIME_TIME=(short)8; // attr2  
    final short TIME_TIME_ZONE=(short)16; // attr3
    final short TIME_STATUS=(short)24;
    final short TIME_DS_BEGIN=(short)32;
    final short TIME_DS_END=(short)40;
    final short TIME_DS_DEVIATION=(short)48;
    final short TIME_DAYLIGHT_SAVING=(short)56; // attr8
    
    // PROFILE_GENERIC
    final short PROFILE_GENERIC_CAPTURE_PERIOD=(short)24; // attr4
    final short PROFILE_GENERIC_PROFILE_ENTRIES=(short)56; // attr8
    final short PROFILE_GENERIC_CAPTURE_OBJECTS=(short)16; // attr3
    final short PROFILE_GENERIC_BUFFER=(short)8; // attr2
    final short PROFILE_ENTRIES_IN_USE=(short)48; // attr7


    // DATA
    final short DATA_VALUE=(short)8; // attr2
    
    // REGISTER
    final short REGISTER_VALUE=(short)8; // attr2
    final short REGISTER_SCALER_UNIT=(short)16; // attr3
    
    // EXTENDED REGISTER
    final short EXTENDED_REGISTER_VALUE=(short)8; // attr2
    final short EXTENDED_REGISTER_SCALER_UNIT=(short)16; // attr3
    final short EXTENDED_REGISTER_STATUS=(short)24; // attr4
    final short EXTENDED_REGISTER_CAPTURE_TIME=(short)32; // attr5

    // DEMAND REGISTER
    final short DEMAND_REGISTER_CURRENT_AVERAGE_VALUE=(short)8; // attr2
    final short DEMAND_REGISTER_LAST_AVERAGE_VALUE=(short)16; // attr3
    final short DEMAND_REGISTER_SCALER_UNIT=(short)24; // attr4
    final short DEMAND_REGISTER_STATUS=(short)32; // attr5
    final short DEMAND_REGISTER_CAPTURE_TIME=(short)40; // attr6
    final short DEMAND_REGISTER_START_TIME_CURRENT=(short)48; // attr7
    final short DEMAND_REGISTER_PERIOD=(short)56; // attr8
    final short DEMAND_REGISTER_NUMBER_OF_PERIODS=(short)64; // attr9
    
    //********************************************************************************************************
    // DLMS PDU constants
    final byte COSEM_READREQUEST=0x05;
    final byte COSEM_READRESPONSE=0x0C;
    final byte COSEM_WRITEREQUEST=0x06;
    final byte COSEM_WRITERESPONSE=0x0D;
    final byte COSEM_CONFIRMEDSERVICEERROR=0x0E;
    
    // ************************************************************************************************************
    // DLMS possible object instantiations (OBIS naming)
    final short NR_OF_OBIS_LN=20;
    final byte OBJECTS_IC_OFFSET=0x00;
    final byte OBJECTS_A_OFFSET=0x01;
    final byte OBJECTS_B_OFFSET=0x02;
    final byte OBJECTS_C_OFFSET=0x03;
    final byte OBJECTS_D_OFFSET=0x04;
    final byte OBJECTS_E_OFFSET=0x05;
    final byte OBJECTS_F_OFFSET=0x06;

    
    // ************************************************************************************************************
    // SN referencing    
    final byte READREQUEST_DATA_SIZE=8; // Size of readrequest data and offset to selective data
    final byte READREQUEST_SN_MSB=6;
    final byte READREQUEST_SN_LSB=7;
    
    
    // ************************************************************************************************************
    // LN referencing    
    final byte SETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
    final byte ACTIONREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
    final byte GETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
    final byte GETREQUESTNEXT_DATA_SIZE=10; // Size of readrequest data and offset to selective data 
    
    // TAGs & types
    final byte COSEM_GETREQUEST=(byte)0xC0;    
    final byte COSEM_GETREQUEST_NORMAL=1;    
    final byte COSEM_GETREQUEST_NEXT=2;    
    final byte COSEM_SETREQUEST=(byte)0xC1;    
    final byte COSEM_SETREQUEST_NORMAL=1;    
    final byte COSEM_GETRESPONSE=(byte)0xC4;    
    final byte COSEM_GETRESPONSE_NORMAL=1;    
    final byte COSEM_GETRESPONSE_WITH_DATABLOCK=2;    
    final byte COSEM_SETRESPONSE=(byte)0xC5;    
    final byte COSEM_SETRESPONSE_NORMAL=1;    

    final byte COSEM_ACTIONREQUEST=(byte)0xC3;    
    final byte COSEM_ACTIONREQUEST_NORMAL=1;    
    
    final byte COSEM_ACTIONRESPONSE=(byte)0xC7;    
    final byte COSEM_ACTIONRESPONSE_NORMAL=1;       
    
}
