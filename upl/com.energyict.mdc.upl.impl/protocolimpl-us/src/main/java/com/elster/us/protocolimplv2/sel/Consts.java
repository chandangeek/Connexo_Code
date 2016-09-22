package com.elster.us.protocolimplv2.sel;

public class Consts {
  private Consts() {}

  // Constants
  public final static String ENCODING = "US-ASCII"; // Encoding for String -> byte[]
  public final static String ACC0 = "Invalid Access Level"; // prompt indicating serial port communications are established
  public final static String ACC1 = "=>"; // Access Level 1 meter prompt
  public final static String ENTER_PASWD = "Password:"; //password prompt
  public final static String CR = "\r";

  // Date formats
  public final static String MMDDYY = "MM/dd/yy";
  public final static String DDMMYY = "dd-MM-yy";
  public final static String YYMMDD = "yy-MM-dd";
  public final static String LDP_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
  public final static String LDP_YFILE_FORMAT = "MMddyyyyHHmmss";


  // Commands
  public final static String COMMAND_SN = "SN"; // Sign on
  public final static String COMMAND_SF = "QUI"; // Sign off, return to access 0
  public final static String COMMAND_RD = "RD"; // Read single
  public final static String COMMAND_RG = "RG"; // Read group
  public final static String COMMAND_EM = "EM"; // Read events (SEL)
  public final static String COMMAND_RE = "RE"; // Read events (other)
  
  //SEL ASCII Commands
  public final static String COMMAND_ID = "ID";
  public final static String COMMAND_TIME = "TIM";
  public final static String COMMAND_DATE = "DAT";
  public final static String COMMAND_ACC = "ACC"; // switch to access level 1
  public final static String COMMAND_REG = "MET E";
  public final static String COMMAND_LP = "ldp_data.bin";

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
  
  
  //SEL Events (Meter Word Bits) configured in SER1, SER2, SER3. Returned in SER(sequential event recorder) report
  public final static String EVENT_HALARM = "HALARM";
  public final static String EVENT_SALARM = "SALARM";
  public final static String EVENT_RSTDEM = "RSTDEM";
  public final static String EVENT_RSTENGY = "RSTENGY";
  public final static String EVENT_RSTPKDM = "RSTPKDM";
  public final static String EVENT_TEST = "TEST";
  public final static String EVENT_DSTCH = "DSTCH";
  public final static String EVENT_SSI_EVE = "SSI_EVE";
  public final static String EVENT_FAULT = "FAULT";
  public final static String EVENT_HARM02 = "HARM02";
  public final static String EVENT_HARM03 = "HARM03";
  public final static String EVENT_HARM04 = "HARM04";
  public final static String EVENT_HARM05 = "HARM05";
  public final static String EVENT_HARM06 = "HARM06";
  public final static String EVENT_HARM07 = "HARM07";
  public final static String EVENT_HARM08 = "HARM08";
  public final static String EVENT_HARM09 = "HARM09";
  public final static String EVENT_HARM10 = "HARM10";
  public final static String EVENT_HARM11 = "HARM11";
  public final static String EVENT_HARM12 = "HARM12";
  public final static String EVENT_HARM13 = "HARM13";
  public final static String EVENT_HARM14 = "HARM14";
  public final static String EVENT_HARM15 = "HARM15";

}
