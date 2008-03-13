package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

/**
 * <pre>
 * An overview of the CewePrometer protocol:
 * 
 * (*) Networking is handled by IEC1107Connection.  
 * 
 * (*) Parsing of general/simple data types is handled by ProRegister itself 
 *     without any separate parsers.  ProRegister does this, it's no beauty, but 
 *     it's short.
 *   
 * (*) Two of the more complex types are handled by 2 special parsers.  
 *
 * 
 * Remarks:
 * 
 * (*) Profile data
 * The meter uses the word "logger" for indicating the load profile.  There are 
 * 2 separate load profiles in the meter that can have a different interval 
 * size.
 * 
 * (*) Maximum demands
 * The meter allows identical maximum demand registers!  For instance you 
 * could have multiple maximum demand registers logging active import.  
 * However they will have the same values!  
 * 
 * (*) Tou
 * The meter has 8 tou registers.  Every tou register logs a _user defined_
 * phenomenon (= unit + ...).  For example Tou register 1 can be configured
 * to log active import.
 * 
 * There are 8 rate registers.  A day is/can be composed of different rate 
 * registers.  
 *
 * (*) DST transitions
 * SimpleDateFormat _always_ assumes wintertime when it does not know
 * the correct timezone.  This results in problems when there is a
 * "Summer to Winter" time transition. In different words: a transition
 * from Daylight savings time to Standard time.
 *
 * For instance Suppose Timezone WET 
 *  -> transition at 1 am
 *  -> WET  = West European (Winter) time (=UTC+0)
 *  -> WEST = West European Summer time   (=UTC+1)
 *
 * MeterTime     Parsed time
 * 0710272330 => Sat Oct 27 22:30:00 GMT 2007 (meter tz= WEST) 
 * 0710280000 => Sat Oct 27 23:00:00 GMT 2007 (meter tz= WEST) 
 * 0710280030 => Sat Oct 27 23:30:00 GMT 2007 (meter tz= WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280130 => Sun Oct 28 01:30:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 * 
 * 
 * When fetching starts on a DST interval, that will be parsed as an 
 * interval in the Standard time zone ...
 * MeterTime     Parsed time
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280130 => Sun Oct 28 01:30:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 * 
 * ProtocoWorker prevents this from happening.  If the TO date should fall in
 * this period (gray zone), ProtocolWorker will change the TO date, in order to 
 * make the protocol fetch before the changes.
 *
 * When fetching starts after transition, the "default wintertime" 
 * parsing is fine:
 * MeterTime     Parsed time
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 *
 * 
 * This problem does not occur for "Winter to Summertime".
 * 
 * MeterTime     Parsed time
 * 0703250000 => Sat Oct 27 00:00:00 GMT 2007  (-> WET winter time)
 * 0703250030 => Sat Oct 27 00:30:00 GMT 2007  (-> WET winter time)
 * 0703250200 => Sun Oct 28 01:00:00 GMT 2007  (-> WEST summer time)
 * 
 * ATTENTION: this is not an airtight check.  When there is a power
 * down on the DST transition there can be missing intervals.  
 * 
 * </pre>
 * 
 * @author FBL
 * 
 * @beginchanges
 * FBL|29012007| initial release
 * FBL|26022007| fix version check bug
 * FBL|26022007| fix scaling & wrap-around bug in profile
 * FBL|27022007| fix profile bug: don't throw away last block of profiledata
 * FBL|03072007| password is no longer a required property, but optional. 
 * FBL|12082007| Minor bug fix: registers that are not supported by
 * the protocol result in a NullPointer.  Now the appropriate exception is 
 * thrown.   
 * FBL|21092007| added supported for unknown event types.
 * FBL|09102007| NullPointer occured when doing timeset with insufficient
 * user rights.
 * @endchanges
 */

public class CewePrometer extends AbstractProtocol  {
    
    private boolean dbg = false;
    
    /** Property keys specific for CewePrometer protocol. */
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_LOGGER = "Logger";

    /** By default the load profile of logger 1 is fetched */
    final static int PD_LOGGER = 0;
    
    /** property for logger that must be fetched (1 or 2) */
    private int pLogger = PD_LOGGER;
    
    /** property for extended logging 
     * null - 0: off
     * 1: on fetch ALL possible obis codes
     * 2: on fetch obis codes supported by connected meter and values */
    private int pExtendedLogging;
    
    /** yyyyMMdd,HHmmss */
    private SimpleDateFormat queryDateFormat;
    /** yyMMddHHmm */
    private SimpleDateFormat shortDateFormat;
    /** yyMMddHHmmss */
    private SimpleDateFormat eventDateFormat;
    /** yyyyMMddHHmmss */
    private SimpleDateFormat longDateFormat;
    /** default formatter with GMT timezone for debugging */ 
    private SimpleDateFormat debugDateFormat;
    
    private IEC1107Connection connection = null;
    private ObisCodeMapper obisCodeMapper = null;
    /** event parser (for event log register)  */
    private EventParser eventParser = null;

    /** TOU-select for every TOU register */
    int[] touMap = null;
    /** nr of meter channels: */ 
    private Integer channelCount = null;
    /** channelInfo registers retrieved from  */
    private List channelInfo = null;
    /** timeDiff: time difference (millis) between system and meter */
    private long timeDiff = 0;
    
    private List billingPoints = null;

    
    /** Meter serial number: 0 to 16 char string */
    ProRegister rSerial = new ProRegister(this, "108700");
    
    /** Meter firmware version: as 3 comma separated ints (major, minor, rev)*/
    ProRegister rFirmwareVersion = new ProRegister(this, "102500");
    
    /** Date and time (yyyymmdd,hhmmss) */
    ProRegister rReadDate = new ProRegister(this, "100C00", false);
    
    /** Slide time (S,P)
     *  
     * S: -28800...28800 
     * P: 1...40
     * 
     * The meter time is adjusted every minute by P percent of a minute until
     * the time has been adjusted S seconds.
     */
    ProRegister rSlideTime = new ProRegister(this, "108A00", false);
    
    /** TOU-registers select.  (8 hex bytes) 
     * Defines what each TOU-register represents. */
    ProRegister rTouRegisterSelect = new ProRegister(this, "10D200");

    /** Time stamp registers */
    ProRegister rTimestamp [] = {
        rReadDate,                        /* 255 */
        new ProRegister(this, "103700"),  /* VZ  */
        new ProRegister(this, "103701"),  /* VZ-1 */
        new ProRegister(this, "103702"),  /* VZ-2 */
        new ProRegister(this, "103703"),  /* VZ-3 */
        new ProRegister(this, "103704"),  /* VZ-4 */
        new ProRegister(this, "103705"),  /* VZ-5 */
        new ProRegister(this, "103706"),  /* VZ-6 */
        new ProRegister(this, "103707"),  /* VZ-7 */
        new ProRegister(this, "103708"),  /* VZ-8 */
        new ProRegister(this, "103709"),  /* VZ-9 */
        new ProRegister(this, "10370A"),  /* VZ-10*/
        new ProRegister(this, "10370B"),  /* VZ-11*/
        new ProRegister(this, "10370C"),  /* VZ-12*/
        new ProRegister(this, "10370D"),  /* VZ-13*/
    };
    
    /** Energy registers */
    ProRegister rEenergy [] = {
        new ProRegister(this, "100800"), /* 255 */
        new ProRegister(this, "103800"), /* VZ  */ 
        new ProRegister(this, "103801"), /* VZ-1 */
        new ProRegister(this, "103802"), /* VZ-2 */
        new ProRegister(this, "103803"), /* VZ-3 */
        new ProRegister(this, "103804"), /* VZ-4 */
        new ProRegister(this, "103805"), /* VZ-5 */
        new ProRegister(this, "103806"), /* VZ-6 */
        new ProRegister(this, "103807"), /* VZ-7 */
        new ProRegister(this, "103808"), /* VZ-8 */
        new ProRegister(this, "103809"), /* VZ-9 */
        new ProRegister(this, "10380A"), /* VZ-10*/
        new ProRegister(this, "10380B"), /* VZ-11*/
        new ProRegister(this, "10380C"), /* VZ-12*/
        new ProRegister(this, "10380D"), /* VZ-13*/
    };
    
    /** Historical external registers (8 floats) */
    ProRegister rExternal [] = {
        new ProRegister(this, "10A100"), /*  255 */
        new ProRegister(this, "103900"), /* VZ  */ 
        new ProRegister(this, "103901"), /* VZ-1 */
        new ProRegister(this, "103902"), /* VZ-2 */
        new ProRegister(this, "103903"), /* VZ-3 */
        new ProRegister(this, "103904"), /* VZ-4 */
        new ProRegister(this, "103905"), /* VZ-5 */
        new ProRegister(this, "103906"), /* VZ-6 */
        new ProRegister(this, "103907"), /* VZ-7 */
        new ProRegister(this, "103908"), /* VZ-8 */
        new ProRegister(this, "103909"), /* VZ-9 */
        new ProRegister(this, "10390A"), /* VZ-10*/
        new ProRegister(this, "10390B"), /* VZ-11*/
        new ProRegister(this, "10390C"), /* VZ-12*/
        new ProRegister(this, "10390D"), /* VZ-13*/
    };
    
    /* rows: billing points (not in chronological order), 
     * columns: MD registers
     * rMD[BILLING POINT][MAXIMUM DEMAND] 
     */
    String md [][] = 
    { 
        /*    MD 0,     MD 1,     MD 2,     MD 3,     MD 4,     MD 5,     MD 6,     MD 7 */
        { "106500", "106501", "106502", "106503", "106504", "106505", "106506", "106507" }, /* 255 */
        { "104000", "104100", "104200", "104300", "104400", "104500", "104600", "104700" }, /* VZ  */
        { "104001", "104101", "104201", "104301", "104401", "104501", "104601", "104701" }, /* VZ-1 */
        { "104002", "104102", "104202", "104302", "104402", "104502", "104602", "104702" }, /* VZ-2 */
        { "104003", "104103", "104203", "104303", "104403", "104503", "104603", "104703" }, /* VZ-3 */
        { "104004", "104104", "104204", "104304", "104404", "104504", "104604", "104704" }, /* VZ-4 */
        { "104005", "104105", "104205", "104305", "104405", "104505", "104605", "104705" }, /* VZ-5 */
        { "104006", "104106", "104206", "104306", "104406", "104506", "104606", "104706" }, /* VZ-6 */
        { "104007", "104107", "104207", "104307", "104407", "104507", "104607", "104707" }, /* VZ-7 */
        { "104008", "104108", "104208", "104308", "104408", "104508", "104608", "104708" }, /* VZ-8 */
        { "104009", "104109", "104209", "104309", "104409", "104509", "104609", "104709" }, /* VZ-9 */
        { "10400A", "10410A", "10420A", "10430A", "10440A", "10450A", "10460A", "10470A" }, /* VZ-10*/
        { "10400B", "10410B", "10420B", "10430B", "10440B", "10450B", "10460B", "10470B" }, /* VZ-11*/
        { "10400C", "10410C", "10420C", "10430C", "10440C", "10450C", "10460C", "10470C" }, /* VZ-12*/
        { "10400D", "10410D", "10420D", "10430D", "10440D", "10450D", "10460D", "10470D" }  /* VZ-13*/
    };
    
    
    /* rows: billing points (not in chronological order), 
     * columns: TOU registers
     * rMD[BILLING POINT][TOU] 
     */
    String tou [][] = {    
        /*   tou 1,     tou 2,   tou 3,    tou 4,    tou 5,    tou 6,    tou 7,    tou 8            */ 
        { "10D100", "10D101", "10D102", "10D103", "10D104", "10D105", "10D106", "10D107" }, /* 255  */
        { "105000", "105100", "105200", "105300", "105400", "105500", "105600", "105700" }, /* VZ   */
        { "105001", "105101", "105201", "105301", "105401", "105501", "105601", "105701" }, /* VZ-1 */
        { "105002", "105102", "105202", "105302", "105402", "105502", "105602", "105702" }, /* VZ-2 */
        { "105003", "105103", "105203", "105303", "105403", "105503", "105603", "105703" }, /* VZ-3 */
        { "105004", "105105", "105204", "105304", "105404", "105504", "105604", "105704" }, /* VZ-4 */
        { "105005", "105105", "105205", "105305", "105405", "105505", "105605", "105705" }, /* VZ-5 */
        { "105006", "105106", "105206", "105306", "105406", "105506", "105606", "105706" }, /* VZ-6 */
        { "105007", "105107", "105207", "105307", "105407", "105507", "105607", "105707" }, /* VZ-7 */
        { "105008", "105108", "105208", "105308", "105408", "105508", "105608", "105708" }, /* VZ-8 */
        { "105009", "105109", "105209", "105309", "105409", "105509", "105609", "105709" }, /* VZ-9 */
        { "10500A", "10510A", "10520A", "10530A", "10540A", "10550A", "10560A", "10570A" }, /* VZ-10*/
        { "10500B", "10510B", "10520B", "10530B", "10540B", "10550B", "10560B", "10570B" }, /* VZ-11*/
        { "10500C", "10510C", "10520C", "10530C", "10540C", "10550C", "10560C", "10570C" }, /* VZ-12*/
        { "10500D", "10510D", "10520D", "10530D", "10540D", "10550D", "10560D", "10570D" }  /* VZ-13*/
    };

    /** Maximum demand registers */
    ProRegister rMaximumDemand [][];
    
    /* init maximum demand */
    {
        rMaximumDemand = new ProRegister[md.length][];
        for( int row = 0; row < md.length; row ++ ) {
            rMaximumDemand[row] = new ProRegister[md[row].length];
            for( int col = 0; col < md[row].length; col ++ ) {
                rMaximumDemand[row][col] = new ProRegister(this, md[row][col]);
            }
        }    
    }
    
    /** Tou registers*/
    ProRegister rTou [][];
    
    /* init tou */
    {
        rTou = new ProRegister[tou.length][];
        for( int row = 0; row < tou.length; row ++ ) {
            rTou[row] = new ProRegister[tou[row].length];
            for( int col = 0; col < tou[row].length; col ++ ) {
                rTou[row][col] = new ProRegister(this, tou[row][col]);
            }
        }            
        
    }
    
    /*
     * "Load profile" related registers 
     */
    
    /** Logger channel count */
    ProRegister rLogChannelCount[] = {
        new ProRegister(this, "100D00", true),                  // count log 1
        new ProRegister(this, "101300", true)                   // count log 2
    };
    
    /** Logger channel interval (seconds) */
    ProRegister rLogChannelInterval[] = {
        new ProRegister(this, "100E00", true),                  // interval 1
        new ProRegister(this, "101400", true)                   // interval 2
    };

    /** Log Read offset: before fetching the load profile set start date */
    ProRegister rLogOffset[] = {
        new ProRegister(this, "101100", true),                  // offset log 1
        new ProRegister(this, "101700", true)                   // offset log 2
    };
    
    /** what is stored in each channel */
    ProRegister rLogChannelConfig[] =  {
        new ProRegister(this, "100F00"),                        // log 1 config
        new ProRegister(this, "101500")                         // log 2 config 
    };
    
    /** Read next Log record */
    ProRegister rLogNextRecord[] = {
        new ProRegister(this, "101200", false, 16),             // read log 1
        new ProRegister(this, "101800", false, 16)              // read log 2
    };
    
    /*
     * "Event logbook" related registers
     */
    
    ProRegister rEventLogReadOffset = new ProRegister(this, "102100", false);
    ProRegister rEventLogNextEvent = new ProRegister(this, "102200", false);

    /* TOU-registers select */
    
    /** TOU-registers select 00: active energy imp. */
    final static int TOU_ACTIVE_ENERGY_IMP = 0x0;
    
    /** TOU-registers select 01: active energy exp. */
    final static int TOU_ACTIVE_ENERGY_EXP = 0x1;
    
    /** TOU-registers select 02: reactive energy imp. */
    final static int TOU_REACTIVE_ENERGY_IMP = 0x2;
    
    /** TOU-registers select 03: reactive energy exp. */
    final static int TOU_REACTIVE_ENERGY_EXP = 0x3;
    
    /** TOU-registers select 04: reactive energy ind. */
    final static int TOU_REACTIVE_ENERGY_IND = 0x4;
    
    /** TOU-registers select 05: reactive energy cap. */
    final static int TOU_REACTIVE_ENERGY_CAP = 0x5;
    
    /** TOU-registers select 06: reactive energy QI */
    final static int TOU_REACTIVE_ENERGY_QI = 0x6;
    
    /** TOU-registers select 07: reactive energy QII */
    final static int TOU_REACTIVE_ENERGY_QII = 0x7;
    
    /** TOU-registers select 08: reactive energy QIII */
    final static int TOU_REACTIVE_ENERGY_QIII = 0x8;
    
    /** TOU-registers select 09: reactive energy QIV */
    final static int TOU_REACTIVE_ENERGY_QIV = 0x9;
    
    /** TOU-registers select 10: apparent energy imp. */
    final static int TOU_APPARENT_ENERGY_IMP = 0xA;
    
    /** TOU-registers select 11: apparent energy exp. */
    final static int TOU_APPARENT_ENERGY_EXP = 0xB;
    
    /** TOU-registers select 23: active energy imp. */
    final static int TOU_EXTERNAL_REG_1 = 0x17;
    
    /** TOU-registers select 24: active energy imp. */
    final static int TOU_EXTERNAL_REG_2 = 0x18;
    
    /** TOU-registers select 25: active energy imp. */
    final static int TOU_EXTERNAL_REG_3 = 0x19;
    
    /** TOU-registers select 26: active energy imp. */
    final static int TOU_EXTERNAL_REG_4 = 0x1A;
    
    /** TOU-registers select 27: active energy imp. */
    final static int TOU_EXTERNAL_REG_5 = 0x1B;
    
    /** TOU-registers select 28: active energy imp. */
    final static int TOU_EXTERNAL_REG_6 = 0x1C;
    
    /** TOU-registers select 29: active energy imp. */
    final static int TOU_EXTERNAL_REG_7 = 0x1D;
    
    /** TOU-registers select 30: active energy imp. */
    final static int TOU_EXTERNAL_REG_8 = 0x1E;

    
    /** Maximum demand for phenomenon: active power import */
    final static int MD_ACTIVE_POWER_IMP = 0x0;
    
    /** Maximum demand for phenomenon: active power export */
    final static int MD_ACTIVE_POWER_EXP = 0x1;
    
    /** Maximum demand for phenomenon: reactive power import */
    final static int MD_REACTIVE_POWER_IMP = 0x2;
    
    /** Maximum demand for phenomenon: reactive power export */
    final static int MD_REACTIVE_POWER_EXP = 0x3;
    
    /** Maximum demand for phenomenon: reactive power inductive */
    final static int MD_REACTIVE_POWER_IND = 0x4;
    
    /** Maximum demand for phenomenon: reactive power capacitive */
    final static int MD_REACTIVE_POWER_CAP = 0x5;
    
    /** Maximum demand for phenomenon: reactive power QI */
    final static int MD_REACTIVE_POWER_QI = 0x6;
    
    /** Maximum demand for phenomenon: reactive power QII */
    final static int MD_REACTIVE_POWER_QII = 0x7;
    
    /** Maximum demand for phenomenon: reactive power QIII */
    final static int MD_REACTIVE_POWER_QIII = 0x8;
    
    /** Maximum demand for phenomenon: reactive power QIV */
    final static int MD_REACTIVE_POWER_QIV = 0x9;
    
    /** Maximum demand for phenomenon: apparent power import */
    final static int MD_APPARENT_POWER_IMP = 0xA;
    
    /** Maximum demand for phenomenon: apparent power export */
    final static int MD_APPARENT_POWER_EXP = 0xB;
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doInit(java.io.InputStream, java.io.OutputStream, int, int, int, int, int, com.energyict.protocolimpl.base.Encryptor, com.energyict.dialer.core.HalfDuplexController)
     */
    protected ProtocolConnection doInit(
            InputStream iStream, OutputStream oStream, 
            int pTimeout, int pRetries, int pForcedDelay, 
            int pEchoCancelling, int pCompatible, Encryptor encryptor, 
            HalfDuplexController halfDuplexController) throws IOException {

        getLogger().info( 
            "CewePrometer protocol init \n" 
                + " Ext. Logging = " + pExtendedLogging + ","
                + " TimeZone = " + getTimeZone().getID() + ","
                + " Logger " + pLogger );
        
        try {
            
            connection= 
                new IEC1107Connection( iStream,oStream,pTimeout,pRetries,
                        pForcedDelay,pEchoCancelling,pCompatible,"ER:");
            
            obisCodeMapper = new ObisCodeMapper(this);
            eventParser = new EventParser(this);
            
        } catch(ConnectionException e) {
            getLogger().log(Level.SEVERE, "init failed", e);
            throw new NestedIOException(e);
        }

        return connection;
        
    }
    
    /** during connect:
     * 1) check firmware verion
     * 2) trigger extended logging
     * 
     * The minimum firmware version is 1.2.0.  It is (probably) not difficult
     * to support older meter versions.  But just in case throw an exception.
     * 
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doConnect()
     */
    protected void doConnect() throws IOException {   

        int v1 = rFirmwareVersion.asInt(0);
        int v2 = rFirmwareVersion.asInt(1);
        
        double v = Double.parseDouble( v1 + "." + v2 );
        
        if( v < 1.2 ) {
            String fv = getFirmwareVersion();
            String msg = 
                "Meter firmware version " + fv + " is not supported.  " +
                "Minimum version 1.2.0.";
            throw new ApplicationException(msg);
        }
        
        if(pExtendedLogging==1)
            getLogger().info(obisCodeMapper.toString() );
        
        if(pExtendedLogging==2)
            getLogger().info(obisCodeMapper.getExtendedLogging());
        
    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doDisConnect()
     */
    protected void doDisConnect() throws IOException { 
        /* when in doubt, do nothing */  
    }
    
    IEC1107Connection getConnection( ){
        return connection;
    }

    /** @see AbstractProtocol#getRequiredKeys() */
    public List getRequiredKeys() {
        return  new ArrayList();
    }
    
    /** @see AbstractProtocol#doGetOptionalKeys() */
    protected List doGetOptionalKeys() {
        ArrayList result = new ArrayList();
        result.add( PK_LOGGER );
        result.add( MeterProtocol.PASSWORD );
        return result;
    }
    
    /** @see AbstractProtocol#doValidateProperties(java.util.Properties) */
    protected void doValidateProperties(Properties p) 
        throws MissingPropertyException, InvalidPropertyException {

        String v = p.getProperty(PK_EXTENDED_LOGGING);
        pExtendedLogging = (v == null) ? 0 : Integer.parseInt(v);
     
        v = p.getProperty(PK_LOGGER);
        pLogger = (v == null) ? PD_LOGGER : Integer.parseInt(v);
        
    }
    
    /** @see AbstractProtocol#getTime() */
    public Date getTime() throws IOException {
        Date d = rReadDate.readAndFreeze().asDate(getLongDateFormat());
        timeDiff = System.currentTimeMillis() - d.getTime();
        return d;
    }

    /* timeDiff is written to SlideTime register   
     *  @see AbstractProtocol#setTime() */
    public void setTime() throws IOException {
        
        int deltaSeconds = (int)(timeDiff/1000) + ((timeDiff%1000)>500 ? 1 : 0); 
        deltaSeconds = deltaSeconds + getInfoTypeRoundtripCorrection();
        
        if(Math.abs(deltaSeconds) > 28800) {
            String msg = 
                "Time could not be changed, difference is too big (" + 
                deltaSeconds + "s)";
            getLogger().severe(msg);
            return;
        }
        
        try {
            write( toCmd( rSlideTime, "" + deltaSeconds + "," + 40) );
        } catch(WriteException we) {
            String msg = "Time could not be changed, error occured: \"";
            msg += we.getMessage() + "\"";
            throw new IOException(msg);
        }
        
    }
    
    /* @see AbstractProtocol#getNumberOfChannels() */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if( channelCount == null ) {
            channelCount = rLogChannelCount[pLogger].asInteger();
        }
        return channelCount.intValue();
    }

    /** build ChannelInfo based on LogChannelConfig */
    List getChannelInfo( ) throws IOException {
        if( channelInfo == null ){ 
            String rawData = rLogChannelConfig[pLogger].getRawData();
            int nrChn = getNumberOfChannels();
            channelInfo = new ChannelConfigurationParser().toChannelInfo(rawData, nrChn);
        }
        return channelInfo;
    }
    
    /* (non-Javadoc)
     * @see AbstractProtocol#getProtocolVersion()
     */
    public String getProtocolVersion() {
        return "$Revision: 1.27 $";
    }
    
    /** Fetch firware version. 
     * @see AbstractProtocol#getFirmwareVersion()
     */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        String s1 = rFirmwareVersion.asString(0);
        String s2 = rFirmwareVersion.asString(1);
        String s3 = rFirmwareVersion.asString(2);
        return s1 + "." + s2 + "." + s3;
    }
    
    /* (non-Javadoc)
     * @see AbstractProtocol#validateSerialNumber()
     */
    protected void validateSerialNumber() throws IOException {
        
        String configured = getInfoTypeSerialNumber();
        if(configured==null) return;
        
        String meter = rSerial.asString();
        
        if( !configured.equals(meter) ){
            String msg = 
                "SerialNumber mismatch! meter: " + meter + ", " +
                "configured: " + configured;
            throw new IOException(msg);
        }
        
    }
    
    /* (non-Javadoc)
     * @see AbstractProtocol#getProfileInterval()
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return rLogChannelInterval[pLogger].asInt();
    }

    /** Read profile data in 2 (easy) steps:
     *
     * 1) read the profiledata
     * 2) if needed read event log book      
     *  
     * @see AbstractProtocol#getProfileData(Date, Date, boolean)
     */
    public ProfileData getProfileData(Date from, boolean includeEvents) 
        throws IOException, UnsupportedException {
        
        dbg( "getProfile() -> from " + getQueryDateFormat().format(from) );
        
        /* step 1: fetch profile data */
        write(toCmd( rLogOffset[pLogger], getQueryDateFormat().format(from) ));
        
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfo());
        
        boolean eof = false;
        StringBuffer buffer = new StringBuffer();
        while(!eof){
            String result = rLogNextRecord[pLogger].getRawData();
            eof = result.indexOf("(EOF)") != -1;
            buffer.append(result);
        }
        toProfileData(pd, buffer.toString());
        
        /* step 2: fetch logbook  */
        if( includeEvents ) {
            write(toCmd(rEventLogReadOffset, getQueryDateFormat().format(from)));
            do {
                String result = rEventLogNextEvent.getRawData();
                eof = result.indexOf("(EOF)") != -1;
                if( !eof ) pd.addEvent( eventParser.parse(result) );
            } while( !eof );
        }

        pd.applyEvents(getProfileInterval()/60);
        return pd;
    }
    
    /** Turn a response into a ProfileData object.  A response consists of a
     * series of intervals. */
    private ProfileData toProfileData(ProfileData profileData, String buffer) 
        throws IOException {

        ArrayList list = splitIntervals(buffer);

        Date previous = null;
        Iterator i = list.iterator();
        while(i.hasNext()) {
            previous = add(profileData, (ProRegister)i.next(), previous);
        }

        return profileData;

    }
    
    /** Split the complete register consisting of multiple records into 
     * individual/separate records, and objectify them into a list of 
     * ProRegisters.  ProRegisters, because they are easily parseable. */
    private ArrayList splitIntervals(String buffer) {
        ArrayList list = new ArrayList();

        boolean eof = false;
        int openIdx = buffer.indexOf( '(', 0 );
        int closeIdx = buffer.indexOf( ')', 0 );

        while(!eof && (openIdx!=-1) && (closeIdx!=-1)){

            String interval = buffer.substring(openIdx, closeIdx+1);
            eof = interval.indexOf("(EOF)") != -1;
            if( !eof ) {
                ProRegister pr = new ProRegister(interval);
                pr.setCeweProMeter(this); /* hmmm .... */
                list.add(pr);
            }

            openIdx = buffer.indexOf( '(', closeIdx );
            closeIdx = buffer.indexOf( ')', openIdx );

        }
        return list;
    }
    
    /** add a single ProRegister (=interval) to the ProfileData object */
    private Date add(ProfileData profileData, ProRegister register, Date previousDate ) throws IOException {
        
        Date date = register.asShortDate(0);
                
        Date tMinus1Hour = new Date(date.getTime() - 3600L * 1000L);
        Date intervalDate = date;
        
        if ( !isDst(date) && isDst(tMinus1Hour) ) {

            /* in doubt */
            Date tMinusInterval = new Date( date.getTime() - getProfileInterval() * 1000 ); 

            if(previousDate != null && tMinusInterval.after(previousDate))  {
                dbg( "Encountered DST TO Standard time transition (correcting)." );
                intervalDate = tMinus1Hour;
            }

        }
    
        if( dbg ) {
            String in = register.asString(0);
            dbg( in + " => meter time " + intervalDate );
        }
        
        int pStatus = register.asInt(1);
        
        IntervalData id = new IntervalData(intervalDate);
        id.setEiStatus( toIntervalStateBitToEIStatus(pStatus) );
        id.setProtocolStatus(pStatus);
        
        for(int idx = 2; idx < register.size(); idx ++){
            id.addValue( register.asDouble(idx) );
        }
        
        profileData.addInterval(id);
        return intervalDate;
        
    }

    private boolean isDst(Date date) {
        return getTimeZone().inDaylightTime(date);
    }
    
    int toIntervalStateBitToEIStatus(int tag){
        int ei = 0;
        switch(tag){  
            // time set
            case 0x01: ei |= IntervalStateBits.SHORTLONG;           break; 
            // disturbed
            case 0x02: ei |= IntervalStateBits.OTHER;               break; 
            // user alarm
            case 0x04: ei |= IntervalStateBits.OTHER;               break; 
            // parameter data change
            case 0x08: ei |= IntervalStateBits.CONFIGURATIONCHANGE; break; 
            // reverse running
            case 0x0F: ei |= IntervalStateBits.REVERSERUN;          break; 
            // meter clock in DST
            case 0x10: ei |= IntervalStateBits.OTHER;               break; 
            // voltage lost/missing
            case 0x20: ei |= IntervalStateBits.OTHER;               break; 
            // corrupted
            case 0x40: ei |= IntervalStateBits.CORRUPTED;           break; 
        }
        return ei;
    }
    
    /** (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#translateRegister(com.energyict.obis.ObisCode)
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new ObisCodeMapper(this).getRegisterInfo(obisCode);
    }
    
    /** (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#readRegister(com.energyict.obis.ObisCode)
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }
    
    /** Create a meter command in ByteArray form
     * @param register to read
     * @param arg arguments
     */
    private String toCmd(ProRegister register, String arg){
        return new String( register.getId() ) + "(" + arg + ")";
    }

    /** send read command */
    String read(String cmd) throws IOException {
        dbg( "read " + cmd );
        connection.sendRawCommandFrame(IEC1107Connection.READ1, cmd.getBytes());
        return new String(connection.receiveRawData());
    }
    
    
    /** send write command 
     * 
     * The CewePrometer returns and Error code when a write command fails.
     * This is a good/valid response according to the IEC62056-21 spec.  However
     * the IEC1107Connection class does not excpect/handle an ERROR code after a 
     * write command.
     * 
     * This is why this kind of error checking is introduced in this write 
     * method. The error handling/checking could be done in the 
     * IEC1107Connection class, which would be more general/reusable/correct.
     * 
     * But it could potentially introduce errors in other IEC1107 protocols. 
     * Hence CewePrometer does the checking for his own. 
     * fbl 09/10/2007
     * 
     * */
    void write(String cmd) throws IOException {
        dbg( "write " + cmd );
        
        byte [] iecCmd = IEC1107Connection.WRITE1;
        byte [] b = cmd.getBytes();
        String r = connection.sendRawCommandFrameAndReturn(iecCmd, b);
        
        if( ( null!=r ) && ( r.indexOf( "ER" ) != -1 ) ) {
            String id = r.substring(4,7);
            throw new WriteException( getExceptionInfo(id) );  
        }
    }
    
    private void dbg(String msg){
        if( dbg ) System.out.println( msg );        
    }
    
    /** Date format: yyyyMMdd,HHmmss */
    SimpleDateFormat getQueryDateFormat( ){
        if(queryDateFormat==null) {
            queryDateFormat = new SimpleDateFormat("yyyyMMdd,HHmmss");
            queryDateFormat.setTimeZone(getTimeZone());
        }
        return queryDateFormat; 
    }

    /** Date format: yyMMddHHmm */
    SimpleDateFormat getShortDateFormat(){
        if(shortDateFormat== null) {
            shortDateFormat = new SimpleDateFormat( "yyMMddHHmm" );
            shortDateFormat.setTimeZone(getTimeZone());
        } 
        return shortDateFormat;
    }
    
    /** Date format: yyMMddHHmmss */
    SimpleDateFormat getEventDateFormat(){
        if(eventDateFormat== null) {
            eventDateFormat = new SimpleDateFormat( "yyMMddHHmmss" );
            eventDateFormat.setTimeZone(getTimeZone());
        } 
        return eventDateFormat;
    }
    
    /** Date format: yyyyMMddHHmmss */
    SimpleDateFormat getLongDateFormat(){
        if(longDateFormat== null) {
            longDateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );
            longDateFormat.setTimeZone(getTimeZone());
        }
        return longDateFormat;
    }
    
    /** Formatter using GMT for debugging */
    SimpleDateFormat getDebugDateFormat(){
        if(debugDateFormat==null) {
            debugDateFormat = new SimpleDateFormat( );
            debugDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return debugDateFormat;
    }
    
    /** wrap date into a Calendar object (with meter timezone)*/
    Calendar toCalendar(Date date){
        Calendar c = Calendar.getInstance(getTimeZone());
        c.setTime(date);
        return c;
    }
    
    private class BillingPointIndex implements Comparable {
        int index;
        Date date;
        BillingPointIndex(int index, ProRegister register) throws IOException{
            this.index = index;
            this.date = register.asDate(getLongDateFormat());
        }
        public int compareTo(Object o) {
            BillingPointIndex bpi = (BillingPointIndex)o;
            return date.compareTo(bpi.date);
        }
        public String toString(){
            return "BillingPointIndex[" + index + ", " + date + "]";
        }
    }

    /** Translate a billing point into a Register Id.    
     * 
     * For example, a meter contains following "Historical period time stamps"
     * 
     * -> Mon Jan 01 00:00:00 CET 2007      
     * -> Thu Dec 14 16:40:57 CET 2006      
     * -> Sat Jan 01 00:00:02 CET 2000      
     * -> Fri Jun 03 09:04:32 CEST 2005     
     * -> Sat Jan 01 00:00:02 CET 2000      
     * -> Thu Apr 01 14:01:04 CEST 2004     
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     *
     * "Thu Jan 01 00:00:00 CET 1970" actually means 'no billing point' so all
     * values with this date need to be omitted.
     * 
     * "Sat Jan 01 00:00:02 CET 2000" occurs twice, since we can not know 
     * which billing point is the older or newer one, we just sort them like
     * the other billing points.  
     *
     * The resulting billing point list looks like this. 
     *                                                            idx    bp
     * -> BillingPointIndex[1, Mon Jan 01 00:00:00 CET 2007]    -> 0  -> VZ
     * -> BillingPointIndex[2, Thu Dec 14 16:40:57 CET 2006]    -> 1  -> VZ-1
     * -> BillingPointIndex[4, Fri Jun 03 09:04:32 CEST 2005]   -> 2  -> VZ-2
     * -> BillingPointIndex[6, Thu Apr 01 14:01:04 CEST 2004]   -> 3  -> VZ-3
     * -> BillingPointIndex[3, Sat Jan 01 00:00:02 CET 2000]    -> 4  -> VZ-4
     * -> BillingPointIndex[5, Sat Jan 01 00:00:02 CET 2000]    -> 5  -> VZ-5
     * 
     * @param billingPoint to translate/map
     * @return Register Id of the billing point 
     * @throws IOException
     */
    int getRow(int billingPoint) throws IOException {
        
        /* no need to fetch any historical registers, just return 0 */
        if(billingPoint==255) return 0;
        
        /* lazily init the billing points collection */
        if(billingPoints==null){
            billingPoints = new ArrayList();
            for (int i = 1; i < rTimestamp.length; i++) {
                if( ! rTimestamp[i].isNullDate() ) {
                    BillingPointIndex bpi = new BillingPointIndex(i, rTimestamp[i]);
                    billingPoints.add(bpi);
                }
            }
            Collections.sort(billingPoints, Collections.reverseOrder());
        }
        
        int abs = Math.abs(billingPoint);
        if( abs < billingPoints.size()   )
            return ((BillingPointIndex)billingPoints.get(abs)).index;
        
        /* meter does not have data for abs nr of billing points */
        return -1; 
        
    }
    
    /** 
     * @param   source
     * @return  index of source 
     *          -1 if no TOU register configured for source
     * @throws  IOException
     */
    int getTouIndex(int source) throws IOException {
        if( touMap == null ) { // fetch
            touMap = new int[] { -1, -1, -1, -1, -1, -1, -1, -1 };    
            String t = rTouRegisterSelect.asString();
            int ti = 0;
            for(int idx = 0; idx < 16; idx=idx+2 ){
                String v = t.substring(idx, idx+2);
                touMap[ti] = Integer.parseInt(v, 16);
                ti = ti +1;
            }
            dbg( "TOU config:" );
            dbg( touToString() );
        }
        for(int i = 0; i < touMap.length; i++){
            if(touMap[i]==source) return i;
        }
        return -1;  // not found
    }
    
    /** method for displaying TOU-select register */
    private String touToString( ){
        StringBuffer r = new StringBuffer();
        
        for(int i = 0; i<touMap.length; i++ ){
            switch(touMap[i]) {
                case 0x00: r.append( "active energy imp.    \n" ); break;
                case 0x01: r.append( "active energy exp.    \n" ); break;
                case 0x02: r.append( "reactive energy imp.  \n" ); break;
                case 0x03: r.append( "reactive energy exp.  \n" ); break;
                case 0x04: r.append( "reactive energy ind.  \n" ); break;
                case 0x05: r.append( "reactive energy cap.  \n" ); break;
                case 0x06: r.append( "reactive energy QI    \n" ); break;
                case 0x07: r.append( "reactive energy QII   \n" ); break;
                case 0x08: r.append( "reactive energy QIII  \n" ); break;
                case 0x09: r.append( "reactive energy QIV   \n" ); break;
                case 0x0A: r.append( "apparent energy imp.  \n" ); break;
                case 0x0B: r.append( "apparent energy exp.  \n" ); break;
                default: r.append( "unknown: " + touMap[i] +  "\n" );
            }
        }
        
        return r.toString();
    }

 
    public String getExceptionInfo(String id) {
        
        if( "001".equals(id) )
            return "Non existing main id: There is no message with the " +
            		"requested main id. Main id is the first four leading " +
            		"digits in the message id. i.e. 0152 in 015200";
        
        if( "002".equals(id) )
            return "Non existing sub id: There are no message with the " +
                    "requested sub id. Sub id is the last two " +
                    "digits in the message id. i.e. 00 in 015200";
        
        if( "003".equals(id) )
            return "Locations requested exceeds limit: A maximum of 16 " +
            		"locations may be read in a single read command e.g." +
            		"R1<STX>101200(33) is not accepted and the meter " +
            		"returns error message 003";
        
        if( "004".equals(id) )
            return "Data format error: The format set in a data message has " +
            		"not a valid format, e.g. a character string is sent when" +
            		"the meter expects an integer.";
        
        if( "005".equals(id) )
            return "Data content error: The data set in a data message has " +
            		"not a valid content, e.g. a value is given outside valid" +
            		"limits.";
        
        if( "006".equals(id) )
            return "Message read only: Returned when a write command is sent" +
            		"to a read only message.";
        
        if( "007".equals(id) )
            return "Message write only: Returned when a read command is sent" +
            		"to a write only message.";
        
        if( "008".equals(id) )
            return "Reserved";
        
        if( "009".equals(id) )
            return "General error message.";
        
        if( "010".equals(id) )
            return "Access denied: Returned when current access level is not " +
            		"enough for the requested message.";
        
        return "Error: \"" + id + "\" occured ";
        
    }
    
}
