package com.elster.us.protocolimplv2.sel;

public class Consts {
  private Consts() {}

  // Constants
  public final static String STR_VQ = "vq"; // Used in SN sign-on command
  // TODO: define the encoding in the properties, or OK to hardcode?
  public final static String ENCODING = "US-ASCII"; // Encoding for String -> byte[]
  public final static String ACC0 = "Invalid Access Level"; // prompt meter sends once serial port communications are established
  public final static String ACC1 = "=>"; // prompt meter sends when meter is in Access Level 1
  public final static String ENTER_PASWD = "Password:"; //prompt to enter password
  public final static String CR = "\r";
  public final static String LF = "\n";

  // Date formats
  public final static String MMDDYY = "MM/dd/yy";
  public final static String DDMMYY = "dd-MM-yy";
  public final static String YYMMDD = "yy-MM-dd";
  public final static String LDP_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

  // Date formats for events
  public final static String MMDDYY_EVENT = "MMddyy";
  public final static String DDMMYY_EVENT = "ddMMyy";
  public final static String YYMMDD_EVENI = "yyMMdd";

  // Commands
  public final static String COMMAND_SN = "SN"; // Sign on
  public final static String COMMAND_SF = "SF"; // Sign off
  public final static String COMMAND_RD = "RD"; // Read single
  public final static String COMMAND_RG = "RG"; // Read group
  public final static String COMMAND_WD = "WD"; // Write data
  public final static String COMMAND_EM = "EM"; // Read events (MiniMax)
  public final static String COMMAND_RE = "RE"; // Read events (other)

  // Response codes
  public final static String RESPONSE_OK = "00";

  // General errors
  public final static String ERROR_TIMEOUT_BEFORE_SIGN_ON = "21";
  public final static String ERROR_UNKNOWN_COMMAND = "28";

  // Communication errors
  public final static String ERROR_FORMAT = "01";
  public final static String ERROR_FRAMING = "22";
  public final static String ERROR_CRC = "23";

  // ASCII control chars
  public final static byte CONTROL_SOH    = 0x01;
  public final static byte CONTROL_STX    = 0x02;
  public final static byte CONTROL_ETX    = 0x03;
  public final static byte CONTROL_EOT    = 0x04;
  public final static byte CONTROL_ENQ    = 0x05;
  public final static byte CONTROL_ACK    = 0x06;
  public final static byte CONTROL_CRC    = 0x43;
  public final static byte CONTROL_LF     = 0x0A;
  public final static byte CONTROL_CR     = 0x0D;
  public final static byte CONTROL_FS     = 0x28;
  public final static byte CONTROL_RS     = 0x30;
  
  // Load Profile (ymodem) Record Types
  public final static byte RECORD_METER_CONFIG  = 0x64;
  public final static byte RECORD_PRESENT_VALUES = 0x65;
  public final static byte RECORD_METER_STATUS  = 0x66;
  public final static byte RECORD_LDP_DATA  = 0x67;
  public final static byte RECORD_SER_DATA = 0x68;
  public final static byte RECORD_LDP_ERROR = 0x69;
  
  // SEL ASCII Commands
  public final static String COMMAND_ID = "ID";
  public final static String COMMAND_TIME = "TIM";
  public final static String COMMAND_DATE = "DAT";
  public final static String COMMAND_ACC = "ACC"; // switch to access level 1
  public final static String COMMAND_REG = "MET E";
  public final static String COMMAND_LP = "ldp_data.bin";

  // Object codes
  public final static String OBJECT_TIME = "203";
  public final static String OBJECT_DATE = "204";
  public final static String OBJECT_DATE_FORMAT = "262";
  public final static String OBJECT_FIRMWARE_VERSION = "122";
  public final static String OBJECT_SERIAL_NUMBER = "062";
  
  //OBIS map
  public final static String OBJECT_KWH_DELIVERED = "kWh";
  public final static String OBJECT_KVARH_DELIVERED = "kvarh";
  public final static String OBJECT_DIRECTION_DELIVERED = "DEL";
  public final static String OBJECT_DIRECTION_RECEIVED = "REC";
  public final static String OBJECT_DIRECTION_IN = "IN";
  public final static String OBJECT_DIRECTION_OUT = "OUT";
  public final static String OBJECT_DIRECTION_NA = "NA"; //not applicable
  //SEL735
  public final static String OBJECT_INTERVAL_KWH = "WH3_DEL";
  public final static String OBJECT_INTERVAL_KVARH = "QH3_DEL";
  //SEL734
  public final static String OBJECT_INTERVAL_MWH3I = "MWH3I";   //MegaWattHours?Delivered
  public final static String OBJECT_INTERVAL_MVRH3I = "MVRH3I"; //MegaVoltAmpereHours?Delivered
  public final static String OBJECT_INTERVAL_MVRH3O = "MVRH3O"; //MegaVoltAmpereHours?Received
  public final static String OBJECT_INTERVAL_MWH3O = "MWH3O";   //MegaWattHours?Received

  // Error strings not related to protocol
  public final static String ERROR_COMMAND_LENGTH = "Command code can only be two bytes in length";

}
