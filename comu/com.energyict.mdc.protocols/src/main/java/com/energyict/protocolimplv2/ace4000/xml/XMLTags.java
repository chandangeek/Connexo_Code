package com.energyict.protocolimplv2.ace4000.xml;

/**
 * Copyrights EnergyICT
 * Date: 31/10/12
 * Time: 10:24
 * Author: khe
 */
public class XMLTags {

    /**
     * Hide the constructor to prevent creation of util class
     */
    private XMLTags() {

    }

    public final static String MPULL 			= "MPull";
    public final static String MPUSH 			= "MPush";
    public final static String METERDATA 		= "MD";
    public final static String ACKNOWLEDGE		= "AK";
    public final static String REJECT			= "Reject";
    public final static String REASON			= "Reason";
    public final static String SERIALNUMBER 	= "M";
    public final static String TRACKER 			= "T";
    public final static String CONFIGURATION    = "CF";
    public final static String CONFIGACK		= "CAK";
    public final static String NACK		        = "NAK";

    //Event tags
    public final static String EVENT            = "EV";
    public final static String EVENTREQUEST     = "qEV";
    public final static String EVENTDATA        = "ED";
    public final static String POWERFAIL        = "PF";

    //Contactor tags
    public final static String CONTACTORCMD     = "CDE";
    public final static String TIME_ATTR        = "t";              //Optional timestamp attribute

    // Firmware tags
    public final static String REQFIRMWARE 		= "qV";			    // request firmware
    public final static String RESFIRMWARE 		= "V";			    // Firmware settings sub-schema parent tag
    public final static String METFIRMVERS		= "MV";			    // Metrology firmware version
    public final static String AUXFIRMVERS		= "AV";			    // Auxiliary firmware version
    public final static String FWUPGRADE		= "OTA";		    // Firmware upgrade
    public final static String FW_PATH	    	= "path";		    // Path (URL) to the firmware file
    public final static String FW_JAR_SIZE		= "jar_file_size";	// Size of the JAR file
    public final static String FW_JAD_SIZE		= "jad_file_size";	// Size of the JAD file

    // gprs network settings and system communication tags
    public final static String NETWORKSETTINGS	= "NS";			// Network settings sub-schema parent tag
    public final static String DNSIPADDRESS		= "DNS";		// gprs dns server ip address
    public final static String GPRSUSERNAME		= "GUN";		// gprs username
    public final static String GPRSPASSWORD		= "GPW";		// gprs password
    public final static String GPRSACCESSPOINT	= "GAPN";		// gprs access point name
    public final static String SYSTEMIPPORTNR	= "CSPrt";		// System IP port number
    public final static String SYSTEMIPADDRESS	= "IPDef";		// System Ip address

    // autoPush configuration tags
    public final static String PUSHSCHEDULE     = "PushSched";	// Auto-push settings sub-schema parent tag
    public final static String ENABLESTATE		= "Enable";		// Auto-push enable state
    public final static String TIMEOPEN			= "TMo";		// number of minutes after midnight to open the push window (encoded in hex)
    public final static String TIMECLOSE		= "TMc";		// number of minutes after midnight to close the push window (encoded in hex)
    public final static String RANDOM			= "Random";		// commands to auto-push to start at a random time between TMo and TMc
    public final static String RWIN			    = "RWin";		// Retry window percentage
    public final static String FULLCONFIG		= "qCF";

    //Max demand configuration tags
    public final static String MAXDEMANDCONFIG  = "MxD";		// Maximum demand parent tag
    public final static String MXDREG           = "Reg";		// Register: Active or reactive
    public final static String MXDSUBI_NUMBER   = "SubI";		// Number of sub-intervals
    public final static String MXDSUBI_DURATION = "SubD";		// Sub-interval duration

    //Consumption limitation configuration
    public final static String EM_CLM           = "CLME";		// Emergency consumption limitation mode parent tag
    public final static String EM_CLM_THRESHOLD = "ThV";		// Threshold Value: the consumption limitation fixed threshold
    public final static String EM_CLM_DURATION  = "DurM";		// Duration of emergency consumption limitation mode (min)

    public final static String CONSLIMITCONFIG  = "CLM";		// Consumption limitation mode parent tag with optional attribute �t� � activation timestamp.
    public final static String SUBINTERVALS     = "ADCF";		// Concatenation of number of sub intervals and duration of a sub interval
    public final static String CONSRATE         = "OvL";		// Override Limitation: Is consumption limitation override allowed and which rate is applied.
    public final static String CONSTOLERANCE    = "ThT";		// Threshold Tolerance: Allowed excess tolerance.
    public final static String CONSTHRESHOLD    = "ThS";		// Threshold Selection
    public final static String CONS_DP0         = "DP0";		// Day profile 0: Array of 8 switching times
    public final static String CONS_DP1         = "DP1";		// Day profile 1: Array of 8 switching times
    public final static String CONS_WEEKPROFILE = "WP";		    // Week profile: Array of 7 days

    //Tariff configuration
    public final static String TARIFF           = "Tariff";	    // Tariff configuration sub-schema parent tags
    public final static String TARIFF_TYPE      = "type";	    // Tariff configuration sub-schema parent tags
    public final static String TARIFF_NUMBER    = "Num";	    // Tariff number
    public final static String TARIFF_RATES     = "Rts";	    // Number of rates in the tariff
    public final static String TARIFF_SW_DAY    = "SW";	        // Tariff switching days
    public final static String TARIFF_SPEC_DAYS = "SDys";	    // Special days parent tag
    public final static String TARIFF_SPEC_DAY  = "SD";	        // Special day
    public final static String TARIFF_SEASON    = "Season";	    // Season definition

    // loadProfile tags
    public final static String REQLP			= "qLPDTr";		// request loadProfile within a date range
    public final static String REQLPALL			= "qLPAll";		// request all loadProfile data
    public final static String LOADPR			= "LP";			// loadProfile data with incremental values
    public final static String LOADPRABS		= "LPA";		// loadProfile data with absolute values
    public final static String LOADPRDINSO		= "LPd";		// loadProfile data for DIN/SO meters
    public final static String SCALE		    = "S";		    // loadProfile scaler

    public final static String LPCONFIG	        = "LPDef";		// Load profile register recording settings
    public final static String LPENABLE	        = "Enable";		// Recording enable state
    public final static String LPINTERVAL       = "LPInt";		// Time intervals between each record
    public final static String LPMAXNUMBER      = "LPNum";		// Maximum number of records to store

    //MBus tags
    public final static String MBUSRAW			= "RAW";		// raw MBus meter data
    public final static String MBUSBILLINGDATA  = "MBR";		// MBus billing registers
    public final static String MBUSCREADING		= "MBCR";		// MBus instantaneous register readings
    public final static String REQMBUSCR        = "qMBCR";		// request for MBus instantaneous register readings
    public final static String REQMBALLDATA		= "qMBRAll";	// request all raw MBus billing data
    public final static String REQMBRANGE		= "qMBRDTr";	// Raw MBus billing data within a date range. Start date in UTC format in hex and the stop date in UTC format in hex

    // meter installation and removal
    public final static String ANNOUNCE			= "Announce";	// Meter installation & removal sub-schema parent tag
    public final static String LOST				= "Lost";		// Communication with slave was lost
    public final static String ICID				= "ICID";		// The ICID number of the SIM associated with a meter
    public final static String TYPE				= "Type";		// meter type
    public final static String SSTRENGTH		= "SS";			// GSM signal strength
    public final static String BSTATION			= "BS";			// GSM cell base station ID
    public final static String OPERATORNAME		= "OT";			// GSM operator name
    public final static String CODSTRING		= "CS";			// meter codification string, if this does not apply then 0 is sent in the data field

    // registers
    public final static String REQCR            = "qCR";	    // request for instantaneous register readings
    public final static String CURREADING		= "CR";			// instantaneous register readings
    public final static String READINGDATA		= "RD";			// the actual reading data
    public final static String CRATTR			= "R";			// attribute of the current readings
    public final static String MAXDEMAND        = "MDR";		// maximum demand register

    // Special data mode
    public final static String SPECIALDATAMODE  = "SDM";		// Special data mode parent tag
    public final static String SPECIAL_BILLING  = "SBilling";	// Special billing register recording
    public final static String SPECIAL_LP       = "SLPDef";		// Special load profile register recording
    public final static String SDM_DURATION     = "DurD";		// SDM duration in days
    public final static String SDM_ACTIV_DATE   = "SAD";		// Special data mode activation date.

    //Display config
    public final static String MESSAGE          = "Message";    // Message parent tag
    public final static String MODE             = "Mode";       // Disabled (0) | Enable Standard (short) (1) | Enable Long (2)
    public final static String LONGMSG          = "LM";         // Long Message: ASCII 1024 characters
    public final static String SHORTMSG         = "SM";         // Short Message: ASCII 8 characters
    public final static String DISPLAYCONFIG    = "LCD";        // LCD settings sub-schema parent tags
    public final static String RESOLUTION       = "DR";         //
    public final static String SEQUENCE         = "DS";         //
    public final static String INTERVAL         = "CTime";      //

    // instantaneous register
    public final static String REQINSTVC        = "qIPR";		// Request instant voltage and current
    public final static String INSTVC           = "IPR";		// Instant voltage and current
    public final static String INSTTIME         = "TOR";		// Time Of Reading
    public final static String PHASE            = "Ph";		    // Phase
    public final static String PHASEATTR        = "N";		    // Phase number attribute
    public final static String VOLTAGE          = "Vo";		    // Instantaneous voltage, expressed as hundredths of volts
    public final static String CURRENT          = "I";	    	// Instantaneous current
    public final static String ACTPOW           = "Pac";		// Instantaneous Active Power
    public final static String REACTPOW         = "Pre";		// Instantaneous Reactive Power
    public final static String APPARPOW         = "Pap";		// Instantaneous Apparent Power
    public final static String POWERFACTOR      = "PwF";		// Power Factor

    // time set/sync tags
    public final static String SYNCTIME			= "ST";			// SNTP time synchronization sub-schema parent tags
    public final static String FORCETIME		= "FT";			// Force meter time to that of the system sub-schema parent tags
    public final static String TIME1			= "T1";			// The time on the meter when the time sync message was sent. In UTC format as hex
    public final static String TIME2			= "T2";			// The time the system received the time sync message from the meter. In UTC format as hex
    public final static String TIME3			= "T3";			// The time the system sent out a time sync response to the meter. In UTC format as hex

    public final static String TIMESYNC			= "Time";		// Clock sync settings sub-schema parent tags
    public final static String DIFF				= "Dif";		// Maximum time difference allowed for clock synchronization in seconds
    public final static String TRIPP			= "Trip";		// Maximum SNTP message trip time allowed in seconds
    public final static String RETRY			= "Retry";		// Number of clock sync retries allowed

    public final static String METERTIME		= "DT";			// Current meter time in UTC format in hex

    // billing data tags
    public final static String BILLDATA			= "BD";			// Billing data sub-schema parent tags
    public final static String BDATTR			= "R";			// attribute of the billing data
    public final static String BDATTR2			= "R2";			// attribute2 of the billing data
    public final static String REGDATA			= "RD";			// Billing register data

    public final static String REQALLBD			= "qBRAll";		// Request all billing data from a meter
    public final static String REQBDRANGE		= "qBRDTr";		// Request billing data from a time range

    public final static String BILLINGCONF		= "Billing";	// Billing register recording settings sub-schema parent tags
    public final static String BILLENABLE		= "Enable";		// Recording enable state
    public final static String BILLINT			= "BRInt";		// Time interval between each record
    public final static String BILLNUMB			= "BRNum";		// Maximum number of records to store

}