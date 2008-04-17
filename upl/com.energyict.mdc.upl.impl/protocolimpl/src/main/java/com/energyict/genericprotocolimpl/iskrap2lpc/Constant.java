package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.text.SimpleDateFormat;
import java.util.*;

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
    
    /** RtuMessage tag for connecting load */
    final static String LOAD_CONTROL_ON = "loadControlOn";
    /** RtuMessage tag for disconnecting load */
    final static String LOAD_CONTROL_OFF = "loadControlOff";
    
    /** RtuMessage tag for reading profile data */
    final static String READ_PROFILE = "<readProfile/>";
    
    /** RtuMessage tag for time sync */
    final static String  TIME_SYNC = "timeSync";
    /** RtuMessage tag for tou schedule */
    final static String  TOU_SCHEDULE = "touSchedule ";
    /** RtuMessage tag for user file id */
    final static String USER_FILE_ID = "userFileId";  

    final static String CHANNEL_MAP = "ChannelMap"; 

    /** */
    final static String RTU_TYPE = "RtuType";
    
    /** ftp related properties */
    
    /** property key for ftp user */
    final static String FTP_USER = "Ftp user";
    /** property key for Group external name */
    final static String FTP_PWD = "Ftp password";
    /** property key for Group external name */
    final static String DIRECTORY = "Directory";

    final static String USER = "User";
    final static String PASSWORD = "Password";
    
    private SimpleDateFormat fixedDateFormat;
    private SimpleDateFormat dateFormat;
    
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
