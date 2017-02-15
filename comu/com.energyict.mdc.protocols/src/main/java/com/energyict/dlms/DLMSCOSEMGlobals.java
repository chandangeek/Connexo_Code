/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DLMSCOSEMGlobals.java
 *
 * Created on 16 september 2002, 16:38
 */

package com.energyict.dlms;

/**
 *
 * @author  koen
 * KHE 29122010: added the long64-unsigned type (0x15)
 */
public interface DLMSCOSEMGlobals {
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
	byte TYPEDESC_LONG64_UNSIGNED=0x15;
	byte TYPEDESC_ENUM=0x16;
    byte TYPEDESC_FLOAT32=0x17;
    byte TYPEDESC_FLOAT64 = 0x18;
    byte TYPEDESC_DATE_TIME = 0x19;
    byte TYPEDESC_DATE = 0x1A;

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

	byte ATTR_DATA_VALUE=2;

	byte ATTR_REGISTER_VALUE=2;
	byte ATTR_REGISTER_SCALER=3;

	byte ATTR_DEMAND_REGISTER_CURRENT_AVERAGE=2;
	byte ATTR_DEMAND_REGISTER_LAST_AVERAGE=3;
	byte ATTR_DEMAND_REGISTER_SCALER=4;

	byte ATTR_CLOCK_TIME=2;

	byte ATTR_PROFILEGENERIC_BUFFER=2;
	byte ATTR_PROFILEGENERIC_CAPTUREOBJECTS=3;
	byte ATTR_PROFILEGENERIC_CAPTUREPERIOD=4;

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
	byte[] LOAD_PROFILE_LN={0,0,99,1,0,(byte)255}; //
	byte[] LOGBOOK_PROFILE_LN={0,0,99,98,0,(byte)255};
	byte[] IPV4_SETUP = {0,0,25,1,0,(byte)255};
	byte[] P3IMAGE_TRANSFER = {0,0,44,0,0,(byte)255};
	byte[] DISCONNECTOR = {0,0,96,3,10,(byte)255};
	byte[] LIMITER = {0,0,17,0,0,(byte)255};
	byte[] PPPSETUP = {0,0,25,3,0,(byte)255};
	byte[] GPRSMODEMSETUP = {0,0,25,4,0,(byte)255};
	byte[] USBSETUP = {0,0,(byte)128,0,28,(byte)255};

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

	short IMAGE_TRANSFER_SN = (short)0x6FB8;

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
	short	PROFILE_GENERIC_ENTRIES_IN_USE=(short)48; // attr7
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
	byte WRITEREQUEST_NR_OF_OBJECTS=8;
	byte WRITEREQUEST_DATA_SIZE=9;

	byte READREQUEST_BLOCKNR_MSB=6;
	byte READREQUEST_BLOCKNR_LSB=7;

	// ************************************************************************************************************
	// LN referencing
	byte SETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
	byte ACTIONREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
	byte GETREQUEST_DATA_SIZE=16; // Size of readrequest data and offset to selective data
	byte GETREQUESTNEXT_DATA_SIZE=10; // Size of readrequest data and offset to selective data

	// TAGs & types
	byte COSEM_GETREQUEST=(byte)0xC0;
	byte COSEM_GETREQUEST_NORMAL=1;
	byte COSEM_GETREQUEST_NEXT=2;
    byte COSEM_GETREQUEST_WITH_LIST=3;
	byte COSEM_SETREQUEST=(byte)0xC1;
	byte COSEM_SETREQUEST_NORMAL=1;
	byte COSEM_SETREQUEST_WITH_FIRST_DATABLOCK=2;
	byte COSEM_SETREQUEST_WITH_DATABLOCK=3;
	byte COSEM_GETRESPONSE=(byte)0xC4;
	byte COSEM_GETRESPONSE_NORMAL=1;
	byte COSEM_GETRESPONSE_WITH_DATABLOCK=2;
    byte COSEM_GETRESPONSE_WITH_LIST = 3;
	byte COSEM_SETRESPONSE=(byte)0xC5;
	byte COSEM_SETRESPONSE_NORMAL=1;
    byte COSEM_SETRESPONSE_FOR_DATABLOCK=2;
    byte COSEM_SETRESPONSE_FOR_LAST_DATABLOCK=3;
    byte COSEM_EVENTNOTIFICATIONRESUEST = (byte)0xC2;
	byte COSEM_DATANOTIFICATIONREQUEST = (byte)0x0F;
	byte COSEM_ACTIONREQUEST=(byte)0xC3;
	byte COSEM_ACTIONREQUEST_NORMAL=1;
	byte COSEM_ACTIONRESPONSE=(byte)0xC7;
	byte COSEM_ACTIONRESPONSE_NORMAL=1;
	byte COSEM_ACTIONRESPONSE_WITH_PBLOCK=2;
	byte COSEM_ACTIONRESPONSE_WITH_LIST=3;
	byte COSEM_ACTIONRESPONSE_NEXT_PBLOCK=4;
    byte COSEM_EXCEPTION_RESPONSE = (byte) 0xD8;
	byte COSEM_GENERAL_BLOCK_TRANSFER = (byte) 0xE0;
	byte COSEM_DATA_NOTIFICATION = 15;

	// Global-ciphering tags (LongName)
	byte GLO_GETREQUEST = (byte)0xC8;
	byte GLO_SETREQUEST = (byte)0xC9;
	byte GLO_EVENTNOTIFICATION_REQUEST= (byte)0xCA;
	byte GLO_ACTIOREQUEST = (byte)0xCB;
	byte GLO_GETRESPONSE = (byte)0xCC;
	byte GLO_SETRESPONSE = (byte)0xCD;
	byte GLO_ACTIONRESPONSE = (byte)0xCF;
	// Global-ciphering tags (ShortName)
	byte GLO_INITIATEREQUEST = (byte)0x21;
	byte GLO_READREQUEST = (byte)0x25;
	byte GLO_WRITEREQUEST = (byte)0x26;
	byte GLO_INITIATERESPONSE = (byte)0x28;
	byte GLO_READRESPONSE = (byte)0x2C;
	byte GLO_WRITERESPONSE = (byte)0x2D;
	byte GLO_CONFIRMEDSERVICEERROR = (byte)0x2E;
	byte GLO_UNCONFIRMEDWRITEREQUEST = (byte)0x36;
	byte GLO_INFORMATIONREPORTREQUEST = (byte)0x38;

	// Dedicated-ciphering tags (LongName)
	byte DED_GETREQUEST = (byte)0xD0;
	byte DED_SETREQUEST = (byte)0xD1;
	byte DED_EVENTNOTIFICATION_REQUEST= (byte)0xD2;
	byte DED_ACTIOREQUEST = (byte)0xD3;
	byte DED_GETRESPONSE = (byte)0xD4;
	byte DED_SETRESPONSE = (byte)0xD5;
	byte DED_ACTIONRESPONSE = (byte)0xD7;
	// Dedicated-ciphering tags (ShortName)
	byte DED_INITIATEREQUEST = (byte)0x41;
	byte DED_READREQUEST = (byte)0x45;
	byte DED_WRITEREQUEST = (byte)0x46;
	byte DED_INITIATERESPONSE = (byte)0x48;
	byte DED_READRESPONSE = (byte)0x4C;
	byte DED_WRITERESPONSE = (byte)0x4D;
	byte DED_CONFIRMEDSERVICEERROR = (byte)0x4E;
	byte DED_UNCONFIRMEDWRITEREQUEST = (byte)0x56;
	byte DED_INFORMATIONREPORTREQUEST = (byte)0x58;

    byte GENERAL_GLOBAL_CIPHERING = (byte) 0xDB;
	byte GENERAL_DEDICATED_CIPTHERING = (byte) 0xDC;
	byte GENERAL_CIPHERING = (byte) 0xDD;
	byte GENERAL_SIGNING = (byte) 0xDF;

	byte COSEM_INITIATEREQUEST = (byte)0x01;
	byte COSEM_INITIATERESPONSE = (byte)0x08;

	// Confirmed Service error tags
	byte CONFIRMEDSERVICEERROR_INITIATEERROR_TAG=1;
	byte CONFIRMEDSERVICEERROR_GETSTATUS_TAG=2;
	byte CONFIRMEDSERVICEERROR_GETNAMELIST_TAG=3;
	byte CONFIRMEDSERVICEERROR_GETVARIABLEATTRIBUTE_TAG=4;
	byte CONFIRMEDSERVICEERROR_READ_TAG=5;
	byte CONFIRMEDSERVICEERROR_WRITE_TAG=6;
	byte CONFIRMEDSERVICEERROR_GETDATASETATTRIBUTE_TAG=7;
	byte CONFIRMEDSERVICEERROR_GETTIATTRIBUTE_TAG=8;
	byte CONFIRMEDSERVICEERROR_CHANGESCOPE_TAG=9;
	byte CONFIRMEDSERVICEERROR_START_TAG=10;
	byte CONFIRMEDSERVICEERROR_STOP_TAG=11;
	byte CONFIRMEDSERVICEERROR_RESUME_TAG=12;
	byte CONFIRMEDSERVICEERROR_MAKEUSABLE_TAG=13;
	byte CONFIRMEDSERVICEERROR_INITIATELOAD_TAG=14;
	byte CONFIRMEDSERVICEERROR_LOADSEGMENT_TAG=15;
	byte CONFIRMEDSERVICEERROR_TERMINATELOAD_TAG=16;
	byte CONFIRMEDSERVICEERROR_INITIATEUPLOAD_TAG=17;
	byte CONFIRMEDSERVICEERROR_UPLOADSEGMENT_TAG=18;
	byte CONFIRMEDSERVICEERROR_TERMINATEUPLOAD_TAG=19;

	// ACSE tags
	byte AARE_GLOBAL_INITIATE_REQUEST_TAG	=	(byte)0x21;
	byte AARE_GLOBAL_INITIATE_RESPONSE_TAG	=	(byte)0x28;
	byte AARQ_TAG							= 	(byte)0x60;
	byte AARQ_CALLING_AUTHENTICATION_VALUE 	= 	(byte)0xAC;
	byte AARQ_USER_INFORMATION				= 	(byte)0xBE;
	byte AARQ_APPLICATION_CONTEXT_NAME 		= 	(byte)0xA1;
    byte AARQ_CALLED_AP_TITLE               =   (byte)0xA2;
    byte AARQ_CALLED_AE_QUALIFIER           =   (byte)0xA3;
    byte AARQ_CALLING_AP_TITLE              = 	(byte)0xA6;
	byte AARQ_CALLING_AE_QUALIFIER          =   (byte)0xA7;
    byte AARQ_SENDER_ACSE_REQUIREMENTS		= 	(byte)0x8A;
    byte AARQ_MECHANISM_NAME				=	(byte)0x8B;
    byte AARE_TAG							=	(byte)0x61;
    byte AARE_APPLICATION_CONTEXT_NAME 		= 	(byte)0xA1;
    byte AARE_RESULT 						= 	(byte)0xA2;
    byte AARE_RESULT_SOURCE_DIAGNOSTIC 		= 	(byte)0xA3;
    byte AARE_RESPONING_AP_TITLE 			= 	(byte)0xA4;
	byte AARE_RESPONDING_AE_QUALIFIER       =   (byte)0xA5;
    /** @deprecated this tag-name does not exist in DLMS, use the {@link #AARQ_CALLING_AP_TITLE} instead*/
    byte AARE_CALLING_AP_TITLE              = 	(byte)0xA6;
	byte AARE_MECHANISM_NAME				=	(byte)0x89;
	byte AARE_RESPONDING_AUTHENTICATION_VALUE =	(byte)0xAA;
	byte ACSE_SERVICE_USER 					= 	(byte)0xA1;
	byte ACSE_SERVICE_PROVIDER 				= 	(byte)0xA2;
	byte AARE_USER_INFORMATION 				= 	(byte)0xBE;
	byte DLMS_PDU_INITIATE_RESPONSE 		= 	(byte)0x08;
	byte DLMS_PDU_CONFIRMED_SERVICE_ERROR	=	(byte)0x0E;
	byte RLRQ_TAG							= 	(byte)0x62;
	byte RLRQ_USER_INFORMATION 				= 	(byte)0xBE;
	byte RLRE_TAG							=	(byte)0x63;
	/** Not sure it's correct, we assume this is the value by looking at the results*/
	byte RLRE_RELEASE_RESPONSE_REASON		=	(byte)0x80;
}
