/*
 * DLMSCOSEMGlobals.java
 *
 * Created on 16 september 2002, 16:38
 */

package com.energyict.dlms;

/**
 * @author koen
 *         KHE 29122010: added the long64-unsigned type (0x15)
 */
public final class DLMSCOSEMGlobals {

    private DLMSCOSEMGlobals() {
        // Hide constructor to prevent instantiation
    }

    // profileMask consts
    public static final byte CHANNEL_DATA = 0;
    public static final byte MANUFACTURER_SPECIFIC_DATA = 1;
    public static final byte STATUS_DATA = 2;

    // OBIS code identifiers
    public static final byte LN_A_ELECTRICITY_RELATED_OBJECTS = 1;

    public static final byte LN_D_CURRENT_AVERAGE_1 = 4;
    public static final byte LN_D_TIME_INTEGRAL_1 = 8;
    public static final byte LN_D_CURRENT_AVERAGE_5 = 27;

    public static final byte ATTR_DATA_VALUE = 2;

    public static final byte ATTR_REGISTER_VALUE = 2;
    public static final byte ATTR_REGISTER_SCALER = 3;

    public static final byte ATTR_DEMAND_REGISTER_CURRENT_AVERAGE = 2;
    public static final byte ATTR_DEMAND_REGISTER_LAST_AVERAGE = 3;
    public static final byte ATTR_DEMAND_REGISTER_SCALER = 4;

    public static final byte ATTR_CLOCK_TIME = 2;

    public static final byte ATTR_PROFILEGENERIC_BUFFER = 2;
    public static final byte ATTR_PROFILEGENERIC_CAPTUREOBJECTS = 3;
    public static final byte ATTR_PROFILEGENERIC_CAPTUREPERIOD = 4;

    // DLMS PDU offsets
    public static final byte DL_COSEMPDU_OFFSET = 0x03;
    public static final byte DL_COSEMPDU_LENGTH_OFFSET = 0x04;
    public static final byte DL_COSEMPDU_TAG_OFFSET = 0x05;

    public static final byte DL_COSEMPDU_OFFSET_CID = 0x06;
    public static final byte DL_COSEMPDU_OFFSET_LN = 0x08;
    public static final byte DL_COSEMPDU_OFFSET_ATTR = 14;
    public static final byte DL_COSEMPDU_OFFSET_ACCESS_SELECTOR = 15;

    // Fix LN references for the SL7000
    public static final byte[] ASSOC_LN_OBJECT_LN = {0, 0, 40, 0, 0, (byte) 255};
    public static final byte[] SAP_OBJECT_LN = {0, 0, 41, 0, 0, (byte) 255};
    public static final byte[] CLOCK_OBJECT_LN = {0, 0, 1, 0, 0, (byte) 255};
    public static final byte[] HISTORIC_VALUES_OBJECT_LN = {0, 1, 98, 1, 0, 101};
    public static final byte[] LOAD_PROFILE_LN = {0, 0, 99, 1, 0, (byte) 255}; //
    public static final byte[] LOGBOOK_PROFILE_LN = {0, 0, 99, 98, 0, (byte) 255};
    public static final byte[] IPV4_SETUP = {0, 0, 25, 1, 0, (byte) 255};
    public static final byte[] P3IMAGE_TRANSFER = {0, 0, 44, 0, 0, (byte) 255};
    public static final byte[] DISCONNECTOR = {0, 0, 96, 3, 10, (byte) 255};
    public static final byte[] LIMITER = {0, 0, 17, 0, 0, (byte) 255};
    public static final byte[] PPPSETUP = {0, 0, 25, 3, 0, (byte) 255};
    public static final byte[] GPRSMODEMSETUP = {0, 0, 25, 4, 0, (byte) 255};

    // Reserved SN logical name constants, should be filled in when requesting the object list first.
    // ASSOC SN OBJECT
    public static final short ASSOC_SN_OBJECT = (short) 0xFA00;
    public static final short ASSOC_SN_ATTR_OBJ_LST = (short) 8;

    short ASSOC_LN_ATTR_OBJ_LST = (short) 8;

    // LNREG OBJECT
    public static final short LNREG_OBJECT_SN = (short) 0xFD00;
    public static final short LNREG_ATTR_VALUE = (short) 8;

    // SAP OBJECT
    public static final short SAP_OBJECT_SN = (short) 0xFC00;
    public static final short SAP_ATTR_ASSIGNMENT_LIST = (short) 8;

    public static final short IMAGE_TRANSFER_SN = (short) 0x6FB8;

    // Offsets for interface classes
    // The LN reference attributes = ((sn attr offset)/8 + 1)

    // GENERAL
    public static final short ALL_CLASSES_LOGICAL_NAME = (short) 0; // attr1 = 0/8+1 = 1 (for ln)

    // CLOCK
    public static final short TIME_TIME = (short) 8; // attr2
    public static final short TIME_TIME_ZONE = (short) 16; // attr3
    public static final short TIME_STATUS = (short) 24;
    public static final short TIME_DS_BEGIN = (short) 32;
    public static final short TIME_DS_END = (short) 40;
    public static final short TIME_DS_DEVIATION = (short) 48;
    public static final short TIME_DAYLIGHT_SAVING = (short) 56; // attr8

    // PROFILE_GENERIC
    public static final short PROFILE_GENERIC_CAPTURE_PERIOD = (short) 24; // attr4
    public static final short PROFILE_GENERIC_ENTRIES_IN_USE = (short) 48; // attr7
    public static final short PROFILE_GENERIC_PROFILE_ENTRIES = (short) 56; // attr8
    public static final short PROFILE_GENERIC_CAPTURE_OBJECTS = (short) 16; // attr3
    public static final short PROFILE_GENERIC_BUFFER = (short) 8; // attr2
    public static final short PROFILE_ENTRIES_IN_USE = (short) 48; // attr7


    // DATA
    public static final short DATA_VALUE = (short) 8; // attr2

    // REGISTER
    public static final short REGISTER_VALUE = (short) 8; // attr2
    public static final short REGISTER_SCALER_UNIT = (short) 16; // attr3

    // EXTENDED REGISTER
    public static final short EXTENDED_REGISTER_VALUE = (short) 8; // attr2
    public static final short EXTENDED_REGISTER_SCALER_UNIT = (short) 16; // attr3
    public static final short EXTENDED_REGISTER_STATUS = (short) 24; // attr4
    public static final short EXTENDED_REGISTER_CAPTURE_TIME = (short) 32; // attr5

    // DEMAND REGISTER
    public static final short DEMAND_REGISTER_CURRENT_AVERAGE_VALUE = (short) 8; // attr2
    public static final short DEMAND_REGISTER_LAST_AVERAGE_VALUE = (short) 16; // attr3
    public static final short DEMAND_REGISTER_SCALER_UNIT = (short) 24; // attr4
    public static final short DEMAND_REGISTER_STATUS = (short) 32; // attr5
    public static final short DEMAND_REGISTER_CAPTURE_TIME = (short) 40; // attr6
    public static final short DEMAND_REGISTER_START_TIME_CURRENT = (short) 48; // attr7
    public static final short DEMAND_REGISTER_PERIOD = (short) 56; // attr8
    public static final short DEMAND_REGISTER_NUMBER_OF_PERIODS = (short) 64; // attr9

    // DLMS PDU constants
    public static final byte COSEM_READREQUEST = 0x05;
    public static final byte COSEM_READRESPONSE = 0x0C;
    public static final byte COSEM_WRITEREQUEST = 0x06;
    public static final byte COSEM_WRITERESPONSE = 0x0D;
    public static final byte COSEM_CONFIRMEDSERVICEERROR = 0x0E;

    // SN referencing
    public static final byte READREQUEST_DATA_SIZE = 8; // Size of readrequest data and offset to selective data
    public static final byte READREQUEST_SN_MSB = 6;
    public static final byte READREQUEST_SN_LSB = 7;
    public static final byte WRITEREQUEST_NR_OF_OBJECTS = 8;
    public static final byte WRITEREQUEST_DATA_SIZE = 9;

    public static final byte READREQUEST_BLOCKNR_MSB = 6;
    public static final byte READREQUEST_BLOCKNR_LSB = 7;

    // LN referencing
    public static final byte SETREQUEST_DATA_SIZE = 16; // Size of readrequest data and offset to selective data
    public static final byte ACTIONREQUEST_DATA_SIZE = 16; // Size of readrequest data and offset to selective data
    public static final byte GETREQUEST_DATA_SIZE = 16; // Size of readrequest data and offset to selective data
    public static final byte GETREQUESTNEXT_DATA_SIZE = 10; // Size of readrequest data and offset to selective data

    // TAGs & types
    public static final byte COSEM_GETREQUEST = (byte) 0xC0;
    public static final byte COSEM_GETREQUEST_NORMAL = 1;
    public static final byte COSEM_GETREQUEST_NEXT = 2;
    public static final byte COSEM_GETREQUEST_WITH_LIST = 3;
    public static final byte COSEM_SETREQUEST = (byte) 0xC1;
    public static final byte COSEM_SETREQUEST_NORMAL = 1;
    public static final byte COSEM_SETREQUEST_WITH_FIRST_DATABLOCK = 2;
    public static final byte COSEM_SETREQUEST_WITH_DATABLOCK = 3;
    public static final byte COSEM_GETRESPONSE = (byte) 0xC4;
    public static final byte COSEM_GETRESPONSE_NORMAL = 1;
    public static final byte COSEM_GETRESPONSE_WITH_DATABLOCK = 2;
    public static final byte COSEM_GETRESPONSE_WITH_LIST = 3;
    public static final byte COSEM_SETRESPONSE = (byte) 0xC5;
    public static final byte COSEM_SETRESPONSE_NORMAL = 1;
    public static final byte COSEM_SETRESPONSE_FOR_DATABLOCK = 2;
    public static final byte COSEM_SETRESPONSE_FOR_LAST_DATABLOCK = 3;
    public static final byte COSEM_EVENTNOTIFICATIONRESUEST = (byte) 0xC2;
    public static final byte COSEM_ACTIONREQUEST = (byte) 0xC3;
    public static final byte COSEM_ACTIONREQUEST_NORMAL = 1;
    public static final byte COSEM_ACTIONRESPONSE = (byte) 0xC7;
    public static final byte COSEM_ACTIONRESPONSE_NORMAL = 1;
    public static final byte COSEM_ACTIONRESPONSE_WITH_PBLOCK = 2;
    public static final byte COSEM_ACTIONRESPONSE_WITH_LIST = 3;
    public static final byte COSEM_ACTIONRESPONSE_NEXT_PBLOCK = 4;
    public static final byte COSEM_EXCEPTION_RESPONSE = (byte) 0xD8;

    // Global-ciphering tags (LongName)
    public static final byte GLO_GETREQUEST = (byte) 0xC8;
    public static final byte GLO_SETREQUEST = (byte) 0xC9;
    public static final byte GLO_EVENTNOTIFICATION_REQUEST = (byte) 0xCA;
    public static final byte GLO_ACTIOREQUEST = (byte) 0xCB;
    public static final byte GLO_GETRESPONSE = (byte) 0xCC;
    public static final byte GLO_SETRESPONSE = (byte) 0xCD;
    public static final byte GLO_ACTIONRESPONSE = (byte) 0xCF;

    // Global-ciphering tags (ShortName)
    public static final byte GLO_INITIATEREQUEST = (byte) 0x21;
    public static final byte GLO_READREQUEST = (byte) 0x25;
    public static final byte GLO_WRITEREQUEST = (byte) 0x26;
    public static final byte GLO_INITIATERESPONSE = (byte) 0x28;
    public static final byte GLO_READRESPONSE = (byte) 0x2C;
    public static final byte GLO_WRITERESPONSE = (byte) 0x2D;
    public static final byte GLO_CONFIRMEDSERVICEERROR = (byte) 0x2E;
    public static final byte GLO_UNCONFIRMEDWRITEREQUEST = (byte) 0x36;
    public static final byte GLO_INFORMATIONREPORTREQUEST = (byte) 0x38;

    // Dedicated-ciphering tags (LongName)
    public static final byte DED_GETREQUEST = (byte) 0xD0;
    public static final byte DED_SETREQUEST = (byte) 0xD1;
    public static final byte DED_EVENTNOTIFICATION_REQUEST = (byte) 0xD2;
    public static final byte DED_ACTIOREQUEST = (byte) 0xD3;
    public static final byte DED_GETRESPONSE = (byte) 0xD4;
    public static final byte DED_SETRESPONSE = (byte) 0xD5;
    public static final byte DED_ACTIONRESPONSE = (byte) 0xD7;

    // Dedicated-ciphering tags (ShortName)
    public static final byte DED_INITIATEREQUEST = (byte) 0x41;
    public static final byte DED_READREQUEST = (byte) 0x45;
    public static final byte DED_WRITEREQUEST = (byte) 0x46;
    public static final byte DED_INITIATERESPONSE = (byte) 0x48;
    public static final byte DED_READRESPONSE = (byte) 0x4C;
    public static final byte DED_WRITERESPONSE = (byte) 0x4D;
    public static final byte DED_CONFIRMEDSERVICEERROR = (byte) 0x4E;
    public static final byte DED_UNCONFIRMEDWRITEREQUEST = (byte) 0x56;
    public static final byte DED_INFORMATIONREPORTREQUEST = (byte) 0x58;

    public static final byte COSEM_INITIATEREQUEST = (byte) 0x01;
    public static final byte COSEM_INITIATERESPONSE = (byte) 0x08;

    // Confirmed Service error tags
    public static final byte CONFIRMEDSERVICEERROR_INITIATEERROR_TAG = 1;
    public static final byte CONFIRMEDSERVICEERROR_GETSTATUS_TAG = 2;
    public static final byte CONFIRMEDSERVICEERROR_GETNAMELIST_TAG = 3;
    public static final byte CONFIRMEDSERVICEERROR_GETVARIABLEATTRIBUTE_TAG = 4;
    public static final byte CONFIRMEDSERVICEERROR_READ_TAG = 5;
    public static final byte CONFIRMEDSERVICEERROR_WRITE_TAG = 6;
    public static final byte CONFIRMEDSERVICEERROR_GETDATASETATTRIBUTE_TAG = 7;
    public static final byte CONFIRMEDSERVICEERROR_GETTIATTRIBUTE_TAG = 8;
    public static final byte CONFIRMEDSERVICEERROR_CHANGESCOPE_TAG = 9;
    public static final byte CONFIRMEDSERVICEERROR_START_TAG = 10;
    public static final byte CONFIRMEDSERVICEERROR_STOP_TAG = 11;
    public static final byte CONFIRMEDSERVICEERROR_RESUME_TAG = 12;
    public static final byte CONFIRMEDSERVICEERROR_MAKEUSABLE_TAG = 13;
    public static final byte CONFIRMEDSERVICEERROR_INITIATELOAD_TAG = 14;
    public static final byte CONFIRMEDSERVICEERROR_LOADSEGMENT_TAG = 15;
    public static final byte CONFIRMEDSERVICEERROR_TERMINATELOAD_TAG = 16;
    public static final byte CONFIRMEDSERVICEERROR_INITIATEUPLOAD_TAG = 17;
    public static final byte CONFIRMEDSERVICEERROR_UPLOADSEGMENT_TAG = 18;
    public static final byte CONFIRMEDSERVICEERROR_TERMINATEUPLOAD_TAG = 19;

    // ACSE tags
    public static final byte AARE_GLOBAL_INITIATE_REQUEST_TAG = (byte) 0x21;
    public static final byte AARE_GLOBAL_INITIATE_RESPONSE_TAG = (byte) 0x28;
    public static final byte AARQ_TAG = (byte) 0x60;
    public static final byte AARQ_CALLING_AUTHENTICATION_VALUE = (byte) 0xAC;
    public static final byte AARQ_USER_INFORMATION = (byte) 0xBE;
    public static final byte AARQ_APPLICATION_CONTEXT_NAME = (byte) 0xA1;
    public static final byte AARQ_CALLED_AP_TITLE = (byte) 0xA2;
    public static final byte AARQ_CALLED_AE_QUALIFIER = (byte) 0xA3;
    public static final byte AARQ_CALLING_AP_TITLE = (byte) 0xA6;
    public static final byte AARQ_SENDER_ACSE_REQUIREMENTS = (byte) 0x8A;
    public static final byte AARQ_MECHANISM_NAME = (byte) 0x8B;
    public static final byte AARE_TAG = (byte) 0x61;
    public static final byte AARE_APPLICATION_CONTEXT_NAME = (byte) 0xA1;
    public static final byte AARE_RESULT = (byte) 0xA2;
    public static final byte AARE_RESULT_SOURCE_DIAGNOSTIC = (byte) 0xA3;
    public static final byte AARE_RESPONING_AP_TITLE = (byte) 0xA4;
    public static final byte AARE_MECHANISM_NAME = (byte) 0x89;
    public static final byte AARE_RESPONDING_AUTHENTICATION_VALUE = (byte) 0xAA;
    public static final byte ACSE_SERVICE_USER = (byte) 0xA1;
    public static final byte ACSE_SERVICE_PROVIDER = (byte) 0xA2;
    public static final byte AARE_USER_INFORMATION = (byte) 0xBE;
    public static final byte DLMS_PDU_INITIATE_RESPONSE = (byte) 0x08;
    public static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR = (byte) 0x0E;
    public static final byte RLRQ_TAG = (byte) 0x62;
    public static final byte RLRQ_USER_INFORMATION = (byte) 0xBE;
    public static final byte RLRE_TAG = (byte) 0x63;
    /**
     * Not sure it's correct, we assume this is the value by looking at the results
     */
    public static final byte RLRE_RELEASE_RESPONSE_REASON = (byte) 0x80;
}
