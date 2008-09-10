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
    
    /** RtuMessage tag for reading on demand registers */
    final static String ON_DEMAND = "onDemand";
    
    /** RtuMessage tag for connecting load */
    final static String CONNECT_LOAD = "connectLoad";
    /** RtuMessage tag for disconnecting load */
    final static String DISCONNECT_LOAD = "disconnectLoad";
    
    /** RtuMessage tag for threshold parameters */
    final static String THRESHOLD_PARAMETERS = "thresholdParameters";
    final static String THRESHOLD_GROUPID = "Threshold GroupId *";
    final static String THRESHOLD_POWERLIMIT = "Threshold PowerLimit (W)";
    final static String CONTRACT_POWERLIMIT = "Contractual PowerLimit (W)";
    final static String APPLY_THRESHOLD	= "Apply threshold";
    final static String CLEAR_THRESHOLD	= "Clear threshold - groupID";
    final static String THRESHOLD_STARTDT = "StartDate (dd/mm/yyyy HH:MM:SS)";
    final static String THRESHOLD_STOPDT = "EndDate (dd/mm/yyyy HH:MM:SS)";
    
    /** RtuMessage tag for connecting load */
    final static String LOAD_CONTROL_ON = "loadControlOn";
    /** RtuMessage tag for disconnecting load */
    final static String LOAD_CONTROL_OFF = "loadControlOff";
    
    /** RtuMessage tag for reading profile data */
    final static String READ_PROFILE = "<readProfile/>";
    
    /** RtuMessage tag for time sync */
    final static String  TIME_SYNC = "timeSync";
    /** RtuMessage tag for tou schedule */
    final static String  TOU_SCHEDULE = "UserFile ID of tariff program";
//    /** RtuMessage tag for user file id */
//    final static String USER_FILE_ID = "userFileId";  

    final static String CHANNEL_MAP = "ChannelMap"; 

    /** RtuType */
    final static String RTU_TYPE = "RtuType";
    
    /** MeterCreateFolder ID */
    final static String FOLDER_ID = "FolderID";
    
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
    
    /** ftp related properties */
    
    /** property key for ftp user */
    final static String FTP_USER = "Ftp user";
    /** property key for Group external name */
    final static String FTP_PWD = "Ftp password";
    /** property key for Group external name */
    final static String DIRECTORY = "Directory";

    final static String USER = "User";
    final static String PASSWORD = "Password";
    
    final static ObisCode mbusSerialObisCode = ObisCode.fromString("0.1.128.50.21.255");
    final static ObisCode powerLimitObisCode = ObisCode.fromString("0.0.128.61.1.255");
    final static ObisCode confChangeObisCode = ObisCode.fromString("0.0.96.2.0.255");
    final static ObisCode coreFirmware = ObisCode.fromString("0.0.128.101.18.255");
    final static ObisCode moduleFirmware = ObisCode.fromString("0.0.128.101.28.255");
    
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
