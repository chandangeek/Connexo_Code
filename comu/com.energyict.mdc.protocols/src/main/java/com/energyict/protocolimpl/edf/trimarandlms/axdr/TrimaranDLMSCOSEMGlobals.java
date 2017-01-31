/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DLMSCOSEMGlobals.java
 *
 * Created on 16 september 2002, 16:38
 */

package com.energyict.protocolimpl.edf.trimarandlms.axdr;

/**
 *
 * @author  koen
 */
public interface TrimaranDLMSCOSEMGlobals {
	// TypeDescription
	byte TYPEDESC_NULL=0x00;
	byte TYPEDESC_ARRAY=0x01;
	byte TYPEDESC_STRUCTURE=0x02;
	byte TYPEDESC_BOOLEAN=0x03;
	byte TYPEDESC_BITSTRING=0x04;
	byte TYPEDESC_DOUBLE_LONG=0x05;
	byte TYPEDESC_DOUBLE_LONG_UNSIGNED=0x06;
	byte TYPEDESC_FLOATING_POINT=0x07;
	byte TYPEDESC_OCTET_STRING=0x09;
	byte TYPEDESC_VISIBLE_STRING=0x0A;
	byte TYPEDESC_TIME=0x0B;
	byte TYPEDESC_BCD=0x0D;
	byte TYPEDESC_INTEGER=0x0F;
	byte TYPEDESC_LONG=0x10; // long in DLMS is 2 bytes
	byte TYPEDESC_UNSIGNED=0x11;
	byte TYPEDESC_LONG_UNSIGNED=0x12; // unsigned long in DLMS is 2 bytes
	byte TYPEDESC_COMPACT_ARRAY=0x13;
	byte TYPEDESC_LONG64=0x14;
	byte TYPEDESC_ENUM=0x16;

	// DLMS Interface class id's
	short ICID_DATA=1;
	short ICID_REGISTER=3;
	short ICID_EXTENDED_REGISTER=4; // new KV 03042003
	short ICID_DEMAND_REGISTER=5;
	short ICID_CLOCK=0x08;
	short ICID_PROFILE_GENERIC=7;
	short ICID_SAP=0x0011;
	short ICID_LNREG=0x0003;

	byte LN_A=0;
	byte LN_B=1;
	byte LN_C=2;
	byte LN_D=3;
	byte LN_E=4;
	byte LN_F=5;

	// profileMask consts
	byte CHANNEL_DATA=0;
	byte MANUFACTURER_SPECIFIC_DATA=1;
	byte STATUS_DATA=2;

	// OBIS code identifiers
	byte LN_A_ELECTRICITY_RELATED_OBJECTS=1;

	byte LN_D_CURRENT_AVERAGE_1=4;
	byte LN_D_TIME_INTEGRAL_1=8;
	byte LN_D_CURRENT_AVERAGE_5=27;

	byte ATTR_DEMAND_REGISTER_SCALER=4;
	byte ATTR_REGISTER_SCALER=3;

	// DLMS PDU offsets
	byte DL_COSEMPDU_OFFSET=0x03;
	byte DL_COSEMPDU_LENGTH_OFFSET=0x04;
	byte DL_COSEMPDU_TAG_OFFSET=0x05;

	byte DL_COSEMPDU_OFFSET_CID=0x06;
	byte DL_COSEMPDU_OFFSET_LN=0x08;
	byte DL_COSEMPDU_OFFSET_ATTR=14;
	byte DL_COSEMPDU_OFFSET_ACCESS_SELECTOR=15;

	//********************************************************************************************************
	// Fix LN references for the SL7000
	byte[] ASSOC_LN_OBJECT_LN={0,0,40,0,0,(byte)255};
	byte[] SAP_OBJECT_LN={0,0,41,0,0,(byte)255};
	byte[] CLOCK_OBJECT_LN={0,0,1,0,0,(byte)255};
	byte[] HISTORIC_VALUES_OBJECT_LN={0,1,98,1,0,101};
	byte[] LOAD_PROFILE_LN={0,0,99,1,0,(byte)255};
	byte[] LOGBOOK_PROFILE_LN={0,0,99,98,0,(byte)255};

	//********************************************************************************************************
	// Reserved SN logical name constants, should be filled in when requesting the object list first.
	// ASSOC SN OBJECT
	short ASSOC_SN_OBJECT=(short)0xFA00;
	short ASSOC_SN_ATTR_OBJ_LST=(short)8;

	short ASSOC_LN_ATTR_OBJ_LST=(short)8;

	// LNREG OBJECT
	short LNREG_OBJECT_SN=(short)0xFD00;
	short LNREG_ATTR_VALUE=(short)8;

	// SAP OBJECT
	short SAP_OBJECT_SN=(short)0xFC00;
	short SAP_ATTR_ASSIGNMENT_LIST=(short)8;

	// **********************************************************************************************
	// Offsets for interface classes
	// The LN reference attributes = ((sn attr offset)/8 + 1)

	// GENERAL
	short ALL_CLASSES_LOGICAL_NAME=(short)0; // attr1 = 0/8+1 = 1 (for ln)

	// CLOCK
	short TIME_TIME=(short)8; // attr2
	short TIME_TIME_ZONE=(short)16; // attr3
	short TIME_STATUS=(short)24;
	short TIME_DS_BEGIN=(short)32;
	short TIME_DS_END=(short)40;
	short TIME_DS_DEVIATION=(short)48;
	short TIME_DAYLIGHT_SAVING=(short)56; // attr8

	// PROFILE_GENERIC
	short PROFILE_GENERIC_CAPTURE_PERIOD=(short)24; // attr4
	short PROFILE_GENERIC_PROFILE_ENTRIES=(short)56; // attr8
	short PROFILE_GENERIC_CAPTURE_OBJECTS=(short)16; // attr3
	short PROFILE_GENERIC_BUFFER=(short)8; // attr2
	short PROFILE_ENTRIES_IN_USE=(short)48; // attr7


	// DATA
	short DATA_VALUE=(short)8; // attr2

	// REGISTER
	short REGISTER_VALUE=(short)8; // attr2
	short REGISTER_SCALER_UNIT=(short)16; // attr3

	// EXTENDED REGISTER
	short EXTENDED_REGISTER_VALUE=(short)8; // attr2
	short EXTENDED_REGISTER_SCALER_UNIT=(short)16; // attr3
	short EXTENDED_REGISTER_STATUS=(short)24; // attr4
	short EXTENDED_REGISTER_CAPTURE_TIME=(short)32; // attr5

	// DEMAND REGISTER
	short DEMAND_REGISTER_CURRENT_AVERAGE_VALUE=(short)8; // attr2
	short DEMAND_REGISTER_LAST_AVERAGE_VALUE=(short)16; // attr3
	short DEMAND_REGISTER_SCALER_UNIT=(short)24; // attr4
	short DEMAND_REGISTER_STATUS=(short)32; // attr5
	short DEMAND_REGISTER_CAPTURE_TIME=(short)40; // attr6
	short DEMAND_REGISTER_START_TIME_CURRENT=(short)48; // attr7
	short DEMAND_REGISTER_PERIOD=(short)56; // attr8
	short DEMAND_REGISTER_NUMBER_OF_PERIODS=(short)64; // attr9

	//********************************************************************************************************
	// DLMS PDU constants
	byte COSEM_READREQUEST=0x05;
	byte COSEM_READRESPONSE=0x0C;
	byte COSEM_WRITEREQUEST=0x06;
	byte COSEM_WRITERESPONSE=0x0D;
	byte COSEM_CONFIRMEDSERVICEERROR=0x0E;

	// ************************************************************************************************************
	// DLMS possible object instantiations (OBIS naming)
	short NR_OF_OBIS_LN=20;
	byte OBJECTS_IC_OFFSET=0x00;
	byte OBJECTS_A_OFFSET=0x01;
	byte OBJECTS_B_OFFSET=0x02;
	byte OBJECTS_C_OFFSET=0x03;
	byte OBJECTS_D_OFFSET=0x04;
	byte OBJECTS_E_OFFSET=0x05;
	byte OBJECTS_F_OFFSET=0x06;


	// ************************************************************************************************************
	// SN referencing
	byte READREQUEST_DATA_SIZE=8; // Size of readrequest data and offset to selective data
	byte READREQUEST_SN_MSB=6;
	byte READREQUEST_SN_LSB=7;


	// ************************************************************************************************************
	// LN referencing
	byte SETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
	byte GETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
	byte GETREQUESTNEXT_DATA_SIZE=10; // Size of readrequest data and offset to selective data

	// TAGs & types
	byte COSEM_GETREQUEST=(byte)0xC0;
	byte COSEM_GETREQUEST_NORMAL=1;
	byte COSEM_GETREQUEST_NEXT=2;
	byte COSEM_SETREQUEST=(byte)0xC1;
	byte COSEM_SETREQUEST_NORMAL=1;
	byte COSEM_GETRESPONSE=(byte)0xC4;
	byte COSEM_GETRESPONSE_NORMAL=1;
	byte COSEM_GETRESPONSE_WITH_DATABLOCK=2;
	byte COSEM_SETRESPONSE=(byte)0xC5;
	byte COSEM_SETRESPONSE_NORMAL=1;

}
