/**
 *
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects.xml;

/**
 * @author gna
 *
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
	public final static String NEGACKNOWLEDGE	= "NAK";
	public final static String REJECT			= "Reject";
	public final static String REASON			= "Reason";
	public final static String SERIALNUMBER 	= "M";
	public final static String TRACKER 			= "T";
	public final static String CONFIGURATION    = "CF";
	public final static String CONFIGACK		= "CAK";

    //Event tags
	public final static String EVENT            = "EV";
	public final static String EVENTREQUEST     = "qEV";
	public final static String EVENTDATA        = "ED";

	// firmware Tags
	public final static String REQFIRMWARE 		= "qV";			// request firmware
	public final static String RESFIRMWARE 		= "V";			// Firmware settings sub-schema parent tag
	public final static String METFIRMVERS		= "MV";			// Metrology firmware version
	public final static String AUXFIRMVERS		= "AV";			// Auxiliary firmware version

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

	// loadProfile tags
	public final static String REQLP			= "qLPDTr";		// request loadProfile within a date range
	public final static String REQLPALL			= "qLPAll";		// request all loadProfile data
	public final static String LOADPR			= "LP";			// loadProfile data with incremental values
	public final static String LOADPRABS		= "LPA";		// loadProfile data with absolute values
	public final static String LOADPRDINSO		= "LPd";		// loadProfile data for DIN/SO meters
	public final static String SCALE		    = "S";		    // loadProfile scaler

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
