package com.elster.us.protocolimplv2.mercury.minimax;

/**
 * Constants required for the protool spec
 *
 * @author James Fox
 */
public final class Consts {

    private Consts() {}

    // Constants
    public final static String STR_VQ = "vq"; // Used in SN sign-on command
    public final static String ENCODING = "US-ASCII"; // Encoding for String -> byte[]
    public final static int RECORDS_PER_PACKET = 50;

    // Date formats
    public final static String MMDDYY = "MM-dd-yy";
    public final static String DDMMYY = "dd-MM-yy";
    public final static String YYMMDD = "yy-MM-dd";

    // Date formats for events
    public final static String MMDDYY_EVENT = "MMddyy";
    public final static String DDMMYY_EVENT = "ddMMyy";
    public final static String YYMMDD_EVENI = "yyMMdd";

    // Response codes
    public final static String RESPONSE_OK = "00";

    // General errors
    public final static String ERROR_TIMEOUT_BEFORE_SIGN_ON = "21";
    public final static String ERROR_UNKNOWN_COMMAND = "28";
    public final static String ERROR_INVALID_ENQUIRY = "30";

    // Communication errors
    public final static String ERROR_FORMAT = "01";
    public final static String ERROR_FRAMING = "22";
    public final static String ERROR_CRC = "23";

    // Audit trail errors
    public final static String ERROR_NO_AUDIT_TRAIL_RECORDS_AVAILABLE = "33";

    // ASCII control chars
    public final static byte CONTROL_SOH    = 0x01;
    public final static byte CONTROL_STX    = 0x02;
    public final static byte CONTROL_ETX    = 0x03;
    public final static byte CONTROL_EOT    = 0x04;
    public final static byte CONTROL_ENQ    = 0x05;
    public final static byte CONTROL_ACK    = 0x06;
    public final static byte CONTROL_FS     = 0x1c;
    public final static byte CONTROL_RS     = 0x1e;

    // **************** OBJECT CODES ********************

    // get current time from device
    public final static String OBJECT_TIME = "203";                 // time
    public final static String OBJECT_DATE = "204";                 // date
    public final static String OBJECT_DATE_FORMAT = "262";          // date format
    public final static String OBJECT_FIRMWARE_VERSION = "122";     // firmware version
    public final static String OBJECT_SERIAL_NUMBER = "062";        // EVC serial number
    public final static String OBJECT_CORRECTED_VOLUME = "000";     // corrected volume accumulated
    public final static String OBJECT_UNCORRECTED_VOLUME = "002";   // Uncorrected volume accumulated
    public final static String OBJECT_BATTERY_READING = "048";      // Battery reading
    public final static String OBJECT_MAX_DAY = "253";              // Max day CorVol since last reset
    public final static String OBJECT_MAX_DAY_DATE = "254";         // Max day CorVol date
    public final static String OBJECT_INTERVAL_AVG_TEMP = "207";    // Interval average temp
    public final static String OBJECT_INTERVAL_AVG_PRESS = "206";   // Interval average pressure
    public final static String OBJECT_INTERVAL_UNC_VOL = "226";     // Unc vol for interval
    public final static String OBJECT_INTERVAL_COR_VOL = "225";     // Cor vol for interval
    public final static String OBJECT_INST_FLOW_RATE = "209";       // Inst flow rate (CorVol/hour)

    // Read these out when determining what "channels" are defined in the audit log
    public final static String OBJECT_AUDIT_1 = "258";
    public final static String OBJECT_AUDIT_2 = "259";
    public final static String OBJECT_AUDIT_3 = "260";
    public final static String OBJECT_AUDIT_4 = "261";
    public final static String OBJECT_AUDIT_5 = "229";
    public final static String OBJECT_AUDIT_6 = "230";
    public final static String OBJECT_AUDIT_7 = "231";
    public final static String OBJECT_AUDIT_8 = "232";
    public final static String OBJECT_AUDIT_9 = "233";
    public final static String OBJECT_AUDIT_10 = "234";

    // These object codes store the UOMs
    /*
        087, Pressure Units, 0, PSI, ----, Mini Max
        088, # of Decimals for Press, 2, X X X X X . X X, ----, Mini Max
        089, Temperature Units, 0, F, ----, Mini Max
        090, Cor Volume Units, 7, CCF, ----, Mini Max
        092, Unc Volume Units, 7, CCF, ----, Mini Max
        141, Energy Units, 0, Therms, ----, Mini Max
        262, Date Format, 0, MM-DD-YY, ----, Mini Max
    */
    public final static String OBJECT_UOM_PRESS = "087";            // pressure
    public final static String OBJECT_UOM_PRESS_DECIMALS = "088";   // pressure # of decimals
    public final static String OBJECT_UOM_TEMP = "089";             // Temperature
    public final static String OBJECT_UOM_COR_VOL = "090";          // CorVol
    public final static String OBJECT_UOM_UNC_VOL = "092";          // UncVol
    public final static String OBJECT_UOM_ENERGY = "141";           // Energy
    public final static String OBJECT_UOM_DATE_FORMAT = "262";      // Date format

    // If a channel is undefined in the audit log, it will hold this value
    public final static String CHANNEL_UNDEFINED = "255";
}
