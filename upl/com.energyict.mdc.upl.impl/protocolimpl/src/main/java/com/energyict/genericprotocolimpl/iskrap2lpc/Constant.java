package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.text.SimpleDateFormat;
import java.util.*;

import com.energyict.obis.ObisCode;

class Constant {
    
    private final static Constant instance = new Constant( );
    
    /** property key for Group external name */
    final static String GROUP_EXTERNAL_NAME = "Group name";
    
    /** property key for concentrator's IP address */
    final static String IP_ADDRESS = "IP address";
    
    /** */
    final static String USE_DIAL_UP = "UseDialUp";
    
    /** property key for Group external name */
    final static String SOCKET_TIMEOUT = "Socket Timeout";
    
    /** RtuMessage tag for time sync */
    final static String  TIME_SYNC = "timeSync";
//    /** RtuMessage tag for user file id */
//    final static String USER_FILE_ID = "userFileId";  

    final static String CHANNEL_MAP = "ChannelMap"; 

    /** RtuType */
    final static String RTU_TYPE = "RtuType";
    
    /** MeterCreateFolder ID */
    final static String FOLDER_EXT_NAME = "FolderExtName";
    
    /** Error message for a meter error */
    final static String METER_ERROR = 
        "Meter failed, serialnumber meter: ";
    
    /** Error message for a concentrator error */
    final static String CONCENTRATOR_ERROR = 
        "Concentrator failed, serialnumber concentrator: ";

    final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    
    final static String NO_AUTODISCOVERY =
        "Meter serialnumber is not found in database and no rtuType is configured so no automatic meter creation.";
    
    /** Frequency adjustment xml tags */
    final static String DLC = "DLC";
    final static String mark = "FreqChannelMark";
    final static String space = "FreqChannelSpace";
    final static String band = "FreqBand";
    
    final static String p2lpcFileName = "\\Storage Card\\P2LPC.xml";
    
    final static String restart = "P2LPCrestart";
    final static String restartFileName = "P2LPCrestart.txt";
    final static String upgradeZipName = "\\Storage Card\\P2LPCFiles\\P2LPCUpgrade.zip";
    final static String firmwareBinFile = "\\Storage Card\\P2LPCFiles\\Firmwarebinfile.bin";
//    final static String firmwareBinFile = "\\Storage Card\\P2LPCFiles\\test.bin";
    final static int MAX_UPLOAD = 1024*10;
    
    /** ftp related properties */
    
    /** property key for ftp user */
    final static String FTP_USER = "Ftp user";
    /** property key for Group external name */
    final static String FTP_PWD = "Ftp password";
    /** property key for Group external name */
    final static String DIRECTORY = "Directory";

    final static String USER = "User";
    final static String PASSWORD = "Password";
    final static String TESTLOGGING = "TestLogging";
    final static String DELAY_AFTER_FAIL = "DelayAfterFail";
    final static String READING_FILE = "ReadingsFileType";
    
    
    final static ObisCode mbusSerialObisCode[] = {ObisCode.fromString("0.1.128.50.21.255"),
    												ObisCode.fromString("0.2.128.50.21.255"),
    												ObisCode.fromString("0.3.128.50.21.255"),
    												ObisCode.fromString("0.4.128.50.21.255")};
    
    final static ObisCode mbusAddressObisCode[] = {ObisCode.fromString("0.1.128.50.20.255"),
													ObisCode.fromString("0.2.128.50.20.255"),
													ObisCode.fromString("0.3.128.50.20.255"),
													ObisCode.fromString("0.4.128.50.20.255")};
    
    final static ObisCode mbusVIFObisCode[] = {ObisCode.fromString("0.1.128.50.30.255"),
													ObisCode.fromString("0.2.128.50.30.255"),
													ObisCode.fromString("0.3.128.50.30.255"),
													ObisCode.fromString("0.4.128.50.30.255")};
    
    final static ObisCode mbusMediumObisCode[] = {ObisCode.fromString("0.1.128.50.23.255"),
													ObisCode.fromString("0.2.128.50.23.255"),
													ObisCode.fromString("0.3.128.50.23.255"),
													ObisCode.fromString("0.4.128.50.23.255")};
    
    
    final static ObisCode powerLimitObisCode = ObisCode.fromString("0.0.128.61.1.255");
    final static ObisCode confChangeObisCode = ObisCode.fromString("0.0.96.2.0.255");
    final static ObisCode coreFirmware = ObisCode.fromString("0.0.128.101.18.255");
    final static ObisCode moduleFirmware = ObisCode.fromString("0.0.128.101.28.255");
    final static ObisCode activeCalendarName = ObisCode.fromString("0.0.13.0.0.255");
    final static ObisCode dlcRepeaterMode = ObisCode.fromString("0.0.128.0.1.255");
    final static ObisCode dlcCarrierFrequency = ObisCode.fromString("0.0.128.0.2.255");
    
    final static ObisCode valveState = ObisCode.fromString("0.0.128.30.31.255");
    
    private SimpleDateFormat fixedDateFormat;
    private SimpleDateFormat dateFormat;

	final static int PROFILE_STATUS_DEVICE_DISTURBANCE = 0x1;
	final static int PROFILE_STATUS_RESET_CUMULATION = 0x10;
	final static int PROFILE_STATUS_DEVICE_CLOCK_CHANGED = 0x20;
	final static int PROFILE_STATUS_POWER_RETURNED = 0x40;
	final static int PROFILE_STATUS_POWER_FAILURE = 0x80;
	static final int EVENT_FATAL_ERROR=0x0001;
	static final int EVENT_DEVICE_CLOCK_RESERVE=0x0002;
	static final int EVENT_VALUE_CORRUPT=0x0004;
	static final int EVENT_DAYLIGHT_CHANGE=0x0008;
	static final int EVENT_BILLING_RESET=0x0010;
	static final int EVENT_DEVICE_CLOCK_CHANGED=0x0020;
	static final int EVENT_POWER_RETURNED=0x0040;
	static final int EVENT_POWER_FAILURE=0x0080;
	static final int EVENT_VARIABLE_SET=0x0100;
	static final int EVENT_UNRELIABLE_OPERATING_CONDITIONS=0x0200;
	static final int EVENT_END_OF_UNRELIABLE_OPERATING_CONDITIONS=0x0400;
	static final int EVENT_UNRELIABLE_EXTERNAL_CONTROL=0x0800;
	static final int EVENT_END_OF_UNRELIABLE_EXTERNAL_CONTROL=0x1000;
	static final int EVENT_EVENTLOG_CLEARED=0x2000;
	static final int EVENT_LOADPROFILE_CLEARED=0x4000;
	static final int EVENT_L1_POWER_FAILURE=0x8001;
	static final int EVENT_L2_POWER_FAILURE=0x8002;
	static final int EVENT_L3_POWER_FAILURE=0x8003;
	static final int EVENT_L1_POWER_RETURNED=0x8004;
	static final int EVENT_L2_POWER_RETURNED=0x8005;
	static final int EVENT_L3_POWER_RETURNED=0x8006;
	static final int EVENT_METER_COVER_OPENED=0x8010;
	static final int EVENT_TERMINAL_COVER_OPENED=0x8011;
    
    final static String conSerialFile 	= "/offlineFiles/iskrap2lpc/ConcentratorSerial.bin";
    final static String profileConfig1 	= "/offlineFiles/iskrap2lpc/ObjectDefFile1.xml";
    final static String profileConfig2 	= "/offlineFiles/iskrap2lpc/ObjectDefFile2.xml";
    final static String[] profileFiles1	= {"/offlineFiles/iskrap2lpc/profile0.xml", "/offlineFiles/iskrap2lpc/profile1.xml"};
    final static String[] profileFiles2	= {"/offlineFiles/iskrap2lpc/lp0.xml", "/offlineFiles/iskrap2lpc/lp1.xml"};
    final static String mbusProfile 	= "/offlineFiles/iskrap2lpc/mbus.xml";
    final static String eventsFile 		= "/offlineFiles/iskrap2lpc/events.xml";
    final static String powerDownFile 	= "/offlineFiles/iskrap2lpc/powerFailures.xml";
    final static String dateTimeFile 	= "/offlineFiles/iskrap2lpc/cosemDateTime.xml";
    final static String conEventFile 	= "/offlineFiles/iskrap2lpc/conEvent.xml";
    final static String mbusSerialFile 	= "/offlineFiles/iskrap2lpc/mbusSerial.bin";
    final static String testFile 		= "/offlineFiles/iskrap2lpc/test.xml";
    final static String billingDaily 	= "/offlineFiles/iskrap2lpc/daily.xml";
    final static String billingMonthly  = "/offlineFiles/iskrap2lpc/monthly.xml";
    final static String dailyfrom0509 	= "/offlineFiles/iskrap2lpc/dailyfrom0509.xml";
    final static String dailyto0509 	= "/offlineFiles/iskrap2lpc/dailyto0509.xml";
    final static String monthlyfrom0509 = "/offlineFiles/iskrap2lpc/monthlyfrom0509.xml";
    final static String monthlyto0509 = "/offlineFiles/iskrap2lpc/monthlyto0509.xml";
    final static String nullp1 = "/offlineFiles/iskrap2lpc/nullpointerstuff.xml";
    final static String nullp2 = "/offlineFiles/iskrap2lpc/nullpointerstuff2.xml";
    final static String[] nullPointerProfile = {"/offlineFiles/iskrap2lpc/NullPointer38547358_0.xml", "/offlineFiles/iskrap2lpc/NullPointer38547358_1.xml"};
    final static String dailyResult		= "/offlineFiles/iskrap2lpc/dailyfromPLR.xml";
    final static String monthlyResult	= "/offlineFiles/iskrap2lpc/monthlyfromPLR.xml";
    final static String oneMonthlyValue = "/offlineFiles/iskrap2lpc/oneMonthlyValue.xml";
    
    final static String NON_Unknown = "NON.Unknown";
    final static String SYS_Startup = "SYS.Startup";
    final static String SYS_Exit = "SYS.Exit";
    final static String SYS_Restart = "SYS.Restart";
    final static String SYS_DeviceId = "SYS.DeviceId";
    final static String SYS_ParamsOK = "SYS.ParamsOK";
    final static String SYS_ConfigOK = "SYS.ConfigOK";
    final static String SYS_ParamsError = "SYS.ParamsError";
    final static String SYS_ConfigError = "SYS.ConfigError";
    final static String SYS_ReadingError = "SYS.ReadingError";
    final static String SYS_ReadingSessionError = "SYS.ReadingSessionError";
    final static String SYS_ReadingTransError = "SYS.ReadingTransError";
    final static String SYS_DemandReadingError = "SYS.DemandReadingError";
    final static String SYS_DemandReadingSessionError = "SYS.DemandReadingSessionError";
    final static String SYS_DemandReadingTransError = "SYS.DemandReadingTransError";
    final static String SYS_DemandReadingXMLOK = "SYS.DemandReadingXMLOK";
    final static String SYS_DemandReadingXMLError = "SYS.DemandReadingXMLError";
    final static String SYS_TariffXMLOK = "SYS.TariffXMLOK";
    final static String SYS_TariffXMLError = "SYS.TariffXMLError";
    final static String SYS_DLCMetersXMLError = "SYS.DLCMetersXMLError";
    final static String SYS_ThreadStartError = "SYS.ThreadStartError";
    final static String SYS_HDLCError = "SYS.HDLCError";
    final static String SYS_MemoryError = "SYS.MemoryError";
    final static String SYS_SerialMetersXMLError = "SYS.SerialMetersXMLError";
    final static String SYS_SaveThreadError = "SYS.SaveThreadError";
    final static String SYS_TimeSync = "SYS.TimeSync";
    final static String SYS_CodeRed = "SYS.CodeRed";
    final static String SYS_UpgradeStart = "SYS.UpgradeStart";
    final static String SYS_UpgradeStartSection = "SYS.UpgradeStartSection";
    final static String SYS_UpgradeFileError = "SYS.UpgradeFileError";
    final static String SYS_UpgradeStartMissing = "SYS.UpgradeStartMissing";
    final static String SYS_UpgradeFinish = "SYS.UpgradeFinish";
    final static String SYS_UpgradeFinishSection = "SYS.UpgradeFinishSection";
    final static String SYS_KeysFileOK = "SYS.KeysFileOK";
    final static String SYS_KeysFileError = "SYS.KeysFileError";
    final static String SYS_ResultsFileError = "SYS.ResultsFileError";
    final static String SYS_UpgradeStartActivate = "SYS.UpgradeStartActivate";
    final static String DLC_Install = "DLC.Install";
    final static String DLC_Deinstall = "sDLC.Deinstall";
    final static String DLC_GlobalDeinstall = "DLC.GlobalDeinstall";
    final static String DLC_DoubleAddress = "DLC.DoubleAddress";
    final static String DLC_AddSubstation = "DLC.AddSubstation";
    final static String DLC_NetworkError = "DLC.NetworkError";
    final static String DLC_NewAddress = "DLC.NewAddress";
    final static String DLC_SlaveLost = "DLC.SlaveLost";
    final static String DLC_SlaveDelete = "DLC.SlaveDelete";
    final static String DLC_SlaveExists = "DLC.SlaveExists";
    final static String COM_OpenPortError = "COM.OpenPortError";
    final static String COM_PhyLayerError = "COM.PhyLayerError";
    final static String COM_GSMModemError = "COM.GSMModemError";
    final static String COM_RASServerError = "COM.RASServerError";
    final static String COM_PPPConnect = "COM.PPPConnect";
    final static String COM_PPPDisconnect = "COM.PPPDisconnect";
    final static String SUB_TariffWriteOK = "SUB.TariffWriteOK";
    final static String SUB_TariffWriteError = "SUB.TariffWriteError";
    final static String SUB_TariffActivateOK = "SUB.TariffActivateOK";
    final static String SUB_TariffActivateError = "SUB.TariffActivateError";
    final static String SUB_SetEncryptionKeyOK = "SUB.SetEncryptionKeyOK";
    final static String SUB_SetEncryptionKeyError = "SUB.SetEncryptionKeyError";

    static Constant getInstance( ){
        return instance;
    }

    /* 
     * There are small incompatibilities between the MS date format from the
     * concentrator and the java date format.  More specifically the timezones
     * are slightly incompatible.
     * 
     * I do _NOT_ like this, but I have to use 2 dateformats. 
     * 
     * Registers & profile date:    2007-10-12T09:00:00Z        
     *      -> format fixed
     * Meter date:                  2007-10-12T09:00:00+01:00   
     *      -> remove ":" and use normal date format  
     * 
     * */
    
    SimpleDateFormat getDateFormatFixed( ) {
        if( fixedDateFormat == null ) {
            fixedDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
            fixedDateFormat.setTimeZone( TimeZone.getTimeZone("GMT" ) );
        }
        return fixedDateFormat;
    }
    
    SimpleDateFormat getDateFormat( ) {
        if( dateFormat == null ) {
            dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
            dateFormat.setTimeZone( TimeZone.getTimeZone("GMT" ) );
        }
        return dateFormat;
    }
    
    String format(Date date){
        return getDateFormat().format(date);
    }
    
}
