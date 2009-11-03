package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.obis.ObisCode;

public class Constant {
    
    private static final Constant instance = new Constant( );
    
    /** property key for Group external name */
    static final String GROUP_EXTERNAL_NAME = "Group name";
    
    /** property key for concentrator's IP address */
    static final String IP_ADDRESS = "IP address";
    
    /** */
    static final String USE_DIAL_UP = "UseDialUp";
    
    /** property key for Group external name */
    static final String SOCKET_TIMEOUT = "Socket Timeout";
    
    /** RtuMessage tag for time sync */
    static final String  TIME_SYNC = "timeSync";
//    /** RtuMessage tag for user file id */
//    static final String USER_FILE_ID = "userFileId";  

    static final String CHANNEL_MAP = "ChannelMap"; 

    /** RtuType */
    static final String RTU_TYPE = "RtuType";
    
    /** MeterCreateFolder ID */
    static final String FOLDER_EXT_NAME = "FolderExtName";
    
    /** Error message for a meter error */
    static final String METER_ERROR = 
        "Meter failed, serialnumber meter: ";
    
    /** Error message for a concentrator error */
    static final String CONCENTRATOR_ERROR = 
        "Concentrator failed, serialnumber concentrator: ";

    static final String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    
    static final String NO_AUTODISCOVERY =
        "Meter serialnumber is not found in database and no rtuType is configured so no automatic meter creation.";
    
    /** Frequency adjustment xml tags */
    static final String DLC = "DLC";
    static final String mark = "FreqChannelMark";
    static final String space = "FreqChannelSpace";
    static final String band = "FreqBand";
    
    static final String p2lpcTempFileName = "\\Storage Card\\P2LPC.tmp";
    static final String p2lpcCorrectFileName = "\\Storage Card\\P2LPC.xml";
    static final String restart = "P2LPCrestart";
    static final String restartFileName = "P2LPCrestart.txt";
    static final String upgradeZipName = "\\Storage Card\\P2LPCFiles\\P2LPCUpgrade.zip";
    static final String firmwareBinFile = "\\Storage Card\\P2LPCFiles\\Firmwarebinfile.bin";
    static final String defaultDirectory = "\\Storage Card\\P2LPCFiles\\";
//    static final String firmwareBinFile = "\\Storage Card\\P2LPCFiles\\test.bin";
    static final int MAX_UPLOAD = 1024*10;
    
    /** ftp related properties */
    
    /** property key for ftp user */
    static final String FTP_USER = "Ftp user";
    /** property key for Group external name */
    static final String FTP_PWD = "Ftp password";
    /** property key for Group external name */
    static final String DIRECTORY = "Directory";

    static final String USER = "User";
    static final String PASSWORD = "Password";
    static final String TESTLOGGING = "TestLogging";
    static final String DELAY_AFTER_FAIL = "DelayAfterFail";
    static final String READING_FILE = "ReadingsFileType";
    static final String LP_ELECTRICITY = "ElectricityLoadProfile";
    static final String LP_MBUS = "MbusLoadProfile";
    static final String LP_DAILY = "DailyLoadProfile";
    static final String LP_MONTHLY = "MonthlyLoadProfile";
    
    
    static final ObisCode mbusSerialObisCode[] = {ObisCode.fromString("0.1.128.50.21.255"),
    												ObisCode.fromString("0.2.128.50.21.255"),
    												ObisCode.fromString("0.3.128.50.21.255"),
    												ObisCode.fromString("0.4.128.50.21.255")};
    
    static final ObisCode mbusAddressObisCode[] = {ObisCode.fromString("0.1.128.50.20.255"),
													ObisCode.fromString("0.2.128.50.20.255"),
													ObisCode.fromString("0.3.128.50.20.255"),
													ObisCode.fromString("0.4.128.50.20.255")};
    
    static final ObisCode mbusVIFObisCode[] = {ObisCode.fromString("0.1.128.50.30.255"),
													ObisCode.fromString("0.2.128.50.30.255"),
													ObisCode.fromString("0.3.128.50.30.255"),
													ObisCode.fromString("0.4.128.50.30.255")};
    
    static final ObisCode mbusMediumObisCode[] = {ObisCode.fromString("0.1.128.50.23.255"),
													ObisCode.fromString("0.2.128.50.23.255"),
													ObisCode.fromString("0.3.128.50.23.255"),
													ObisCode.fromString("0.4.128.50.23.255")};
    
    
    static final ObisCode powerLimitObisCode = ObisCode.fromString("0.0.128.61.1.255");
    static final ObisCode confChangeObisCode = ObisCode.fromString("0.0.96.2.0.255");
    static final ObisCode coreFirmware = ObisCode.fromString("0.0.128.101.18.255");
    static final ObisCode moduleFirmware = ObisCode.fromString("0.0.128.101.28.255");
    static final ObisCode activeCalendarName = ObisCode.fromString("0.0.13.0.0.255");
    static final ObisCode dlcRepeaterMode = ObisCode.fromString("0.0.128.0.1.255");
    static final ObisCode dlcCarrierFrequency = ObisCode.fromString("0.0.128.0.2.255");
    
    static final ObisCode valveControl = ObisCode.fromString("0.0.128.30.30.255"); 
    static final ObisCode valveState = ObisCode.fromString("0.0.128.30.31.255");
    
    private SimpleDateFormat fixedDateFormat;
    private SimpleDateFormat dateFormat;

	public static final int PROFILE_STATUS_DEVICE_DISTURBANCE = 0x1;
	public static final int PROFILE_STATUS_RESET_CUMULATION = 0x10;
	public static final int PROFILE_STATUS_DEVICE_CLOCK_CHANGED = 0x20;
	public static final int PROFILE_STATUS_POWER_RETURNED = 0x40;
	public static final int PROFILE_STATUS_POWER_FAILURE = 0x80;
	public static final int EVENT_FATAL_ERROR=0x0001;
	public static final int EVENT_DEVICE_CLOCK_RESERVE=0x0002;
	public static final int EVENT_VALUE_CORRUPT=0x0004;
	public static final int EVENT_DAYLIGHT_CHANGE=0x0008;
	public static final int EVENT_BILLING_RESET=0x0010;
	public static final int EVENT_DEVICE_CLOCK_CHANGED=0x0020;
	public static final int EVENT_POWER_RETURNED=0x0040;
	public static final int EVENT_POWER_FAILURE=0x0080;
	public static final int EVENT_VARIABLE_SET=0x0100;
	public static final int EVENT_UNRELIABLE_OPERATING_CONDITIONS=0x0200;
	public static final int EVENT_END_OF_UNRELIABLE_OPERATING_CONDITIONS=0x0400;
	public static final int EVENT_UNRELIABLE_EXTERNAL_CONTROL=0x0800;
	public static final int EVENT_END_OF_UNRELIABLE_EXTERNAL_CONTROL=0x1000;
	public static final int EVENT_EVENTLOG_CLEARED=0x2000;
	public static final int EVENT_LOADPROFILE_CLEARED=0x4000;
	public static final int EVENT_L1_POWER_FAILURE=0x8001;
	public static final int EVENT_L2_POWER_FAILURE=0x8002;
	public static final int EVENT_L3_POWER_FAILURE=0x8003;
	public static final int EVENT_L1_POWER_RETURNED=0x8004;
	public static final int EVENT_L2_POWER_RETURNED=0x8005;
	public static final int EVENT_L3_POWER_RETURNED=0x8006;
	public static final int EVENT_METER_COVER_OPENED=0x8010;
	public static final int EVENT_TERMINAL_COVER_OPENED=0x8011;
    
    static final String conSerialFile 	= "/offlineFiles/iskrap2lpc/ConcentratorSerial.bin";
    static final String profileConfig1 	= "/offlineFiles/iskrap2lpc/ObjectDefFile1.xml";
    static final String profileConfig2 	= "/offlineFiles/iskrap2lpc/ObjectDefFile2.xml";
    static final String[] profileFiles1	= {"/offlineFiles/iskrap2lpc/profile0.xml", "/offlineFiles/iskrap2lpc/profile1.xml"};
    static final String[] profileFiles2	= {"/offlineFiles/iskrap2lpc/lp0.xml", "/offlineFiles/iskrap2lpc/lp1.xml"};
    static final String mbusProfile 	= "/offlineFiles/iskrap2lpc/mbus.xml";
    static final String eventsFile 		= "/offlineFiles/iskrap2lpc/events.xml";
    static final String powerDownFile 	= "/offlineFiles/iskrap2lpc/powerFailures.xml";
    static final String dateTimeFile 	= "/offlineFiles/iskrap2lpc/cosemDateTime.xml";
    static final String conEventFile 	= "/offlineFiles/iskrap2lpc/conEvent.xml";
    static final String mbusSerialFile 	= "/offlineFiles/iskrap2lpc/mbusSerial.bin";
    static final String testFile 		= "/offlineFiles/iskrap2lpc/test.xml";
    static final String billingDaily 	= "/offlineFiles/iskrap2lpc/daily.xml";
    static final String billingMonthly  = "/offlineFiles/iskrap2lpc/monthly.xml";
    static final String dailyfrom0509 	= "/offlineFiles/iskrap2lpc/dailyfrom0509.xml";
    static final String dailyto0509 	= "/offlineFiles/iskrap2lpc/dailyto0509.xml";
    static final String monthlyfrom0509 = "/offlineFiles/iskrap2lpc/monthlyfrom0509.xml";
    static final String monthlyto0509 = "/offlineFiles/iskrap2lpc/monthlyto0509.xml";
    static final String nullp1 = "/offlineFiles/iskrap2lpc/nullpointerstuff.xml";
    static final String nullp2 = "/offlineFiles/iskrap2lpc/nullpointerstuff2.xml";
    static final String[] nullPointerProfile = {"/offlineFiles/iskrap2lpc/NullPointer38547358_0.xml", "/offlineFiles/iskrap2lpc/NullPointer38547358_1.xml"};
    static final String dailyResult		= "/offlineFiles/iskrap2lpc/dailyfromPLR.xml";
    static final String monthlyResult	= "/offlineFiles/iskrap2lpc/monthlyfromPLR.xml";
    static final String oneMonthlyValue = "/offlineFiles/iskrap2lpc/oneMonthlyValue.xml";
    
    static final String NON_Unknown = "NON.Unknown";
    static final String SYS_Startup = "SYS.Startup";
    static final String SYS_Exit = "SYS.Exit";
    static final String SYS_Restart = "SYS.Restart";
    static final String SYS_DeviceId = "SYS.DeviceId";
    static final String SYS_ParamsOK = "SYS.ParamsOK";
    static final String SYS_ConfigOK = "SYS.ConfigOK";
    static final String SYS_ParamsError = "SYS.ParamsError";
    static final String SYS_ConfigError = "SYS.ConfigError";
    static final String SYS_ReadingError = "SYS.ReadingError";
    static final String SYS_ReadingSessionError = "SYS.ReadingSessionError";
    static final String SYS_ReadingTransError = "SYS.ReadingTransError";
    static final String SYS_DemandReadingError = "SYS.DemandReadingError";
    static final String SYS_DemandReadingSessionError = "SYS.DemandReadingSessionError";
    static final String SYS_DemandReadingTransError = "SYS.DemandReadingTransError";
    static final String SYS_DemandReadingXMLOK = "SYS.DemandReadingXMLOK";
    static final String SYS_DemandReadingXMLError = "SYS.DemandReadingXMLError";
    static final String SYS_TariffXMLOK = "SYS.TariffXMLOK";
    static final String SYS_TariffXMLError = "SYS.TariffXMLError";
    static final String SYS_DLCMetersXMLError = "SYS.DLCMetersXMLError";
    static final String SYS_ThreadStartError = "SYS.ThreadStartError";
    static final String SYS_HDLCError = "SYS.HDLCError";
    static final String SYS_MemoryError = "SYS.MemoryError";
    static final String SYS_SerialMetersXMLError = "SYS.SerialMetersXMLError";
    static final String SYS_SaveThreadError = "SYS.SaveThreadError";
    static final String SYS_TimeSync = "SYS.TimeSync";
    static final String SYS_CodeRed = "SYS.CodeRed";
    static final String SYS_UpgradeStart = "SYS.UpgradeStart";
    static final String SYS_UpgradeStartSection = "SYS.UpgradeStartSection";
    static final String SYS_UpgradeFileError = "SYS.UpgradeFileError";
    static final String SYS_UpgradeStartMissing = "SYS.UpgradeStartMissing";
    static final String SYS_UpgradeFinish = "SYS.UpgradeFinish";
    static final String SYS_UpgradeFinishSection = "SYS.UpgradeFinishSection";
    static final String SYS_UpgradeCompleteOK = "SYS.UpgradeCompleteOK";
    static final String SYS_KeysFileOK = "SYS.KeysFileOK";
    static final String SYS_KeysFileError = "SYS.KeysFileError";
    static final String SYS_ResultsFileError = "SYS.ResultsFileError";
    static final String SYS_UpgradeStartActivate = "SYS.UpgradeStartActivate";
    static final String DLC_Install = "DLC.Install";
    static final String DLC_Deinstall = "sDLC.Deinstall";
    static final String DLC_GlobalDeinstall = "DLC.GlobalDeinstall";
    static final String DLC_DoubleAddress = "DLC.DoubleAddress";
    static final String DLC_AddSubstation = "DLC.AddSubstation";
    static final String DLC_NetworkError = "DLC.NetworkError";
    static final String DLC_NewAddress = "DLC.NewAddress";
    static final String DLC_SlaveLost = "DLC.SlaveLost";
    static final String DLC_SlaveDelete = "DLC.SlaveDelete";
    static final String DLC_SlaveExists = "DLC.SlaveExists";
    static final String COM_OpenPortError = "COM.OpenPortError";
    static final String COM_PhyLayerError = "COM.PhyLayerError";
    static final String COM_GSMModemError = "COM.GSMModemError";
    static final String COM_RASServerError = "COM.RASServerError";
    static final String COM_PPPConnect = "COM.PPPConnect";
    static final String COM_PPPDisconnect = "COM.PPPDisconnect";
    static final String SUB_TariffWriteOK = "SUB.TariffWriteOK";
    static final String SUB_TariffWriteError = "SUB.TariffWriteError";
    static final String SUB_TariffActivateOK = "SUB.TariffActivateOK";
    static final String SUB_TariffActivateError = "SUB.TariffActivateError";
    static final String SUB_SetEncryptionKeyOK = "SUB.SetEncryptionKeyOK";
    static final String SUB_SetEncryptionKeyError = "SUB.SetEncryptionKeyError";

    public static Constant getInstance( ){
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
    
    public SimpleDateFormat getDateFormatFixed( ) {
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
