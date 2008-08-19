/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects.xml;

/**
 * @author gna
 *
 */
public class XMLTags {
	
	public final static String mPull 			= "MPull";
	public final static String mPush 			= "MPush";
	public final static String meterData 		= "MD";
	public final static String acknowledge		= "AK";
	public final static String negAcknowledge	= "NAK";
	public final static String reject			= "Reject";
	public final static String reason			= "Reason";
	public final static String serialNumber 	= "M";
	public final static String tracker 			= "T";
	public final static String configHandling	= "CF";
	public final static String configAck		= "CAK";
	
	// firmware Tags
	public final static String reqFirmware 		= "qV";			// request firmware
	public final static String resFirmware 		= "V";			// Firmware settings sub-schema parent tag
	public final static String metFirmVers		= "MV";			// Metrology firmware version
	public final static String auxFirmVers		= "AV";			// Auxiliary firmware version
	
	// gprs network settings and system communication tags
	public final static String networkSettings	= "NS";			// Network settings sub-schema parent tag
	public final static String dnsIPAddress		= "DNS";		// gprs dns server ip address
	public final static String gprsUsername		= "GUN";		// gprs username
	public final static String gprsPassword		= "GPW";		// gprs password
	public final static String gprsAccessPoint	= "GAPN";		// gprs access point name
	public final static String systemIPPortnr	= "CSPrt";		// System IP port number
	public final static String systemIPAddress	= "IPDef";		// System Ip address
	
	// autoPush configuration tags
	public final static String puschSchedule	= "PushSched";	// Auto-push settings sub-schema parent tag
	public final static String enableState		= "Enable";		// Auto-push enable state
	public final static String timeOpen			= "TMo";		// number of minutes after midnight to open the push window (encoded in hex)
	public final static String timeClose		= "TMc";		// number of minutes after midnight to close the push window (encoded in hex)
	public final static String random			= "Random";		// commands to auto-push to start at a random time between TMo and TMc

	public final static String fullConfig		= "qCF";
	
	// loadProfile tags
	public final static String reqLP			= "qLPDTr";		// request loadProfile within a date range
	public final static String reqLPAll			= "qLPAll";		// request all loadProfile data
	public final static String loadPr			= "LP";			// loadProfile data with incremental values
	public final static String loadPrAbs		= "LPA";		// loadProfile data with absolute values
	public final static String loadPrDinSo		= "LPd";		// loadProfile data for DIN/SO meters
	
	//MBus tags
	public final static String mbusRaw			= "RAW";		// raw MBus meter data
	public final static String mbusLP			= "MBR";		// MBus data sub-schema parent tag
	public final static String mbusCReading		= "MBCR";		// MBus instantaneous register readings
	public final static String reqMBAllData		= "qMBRAll";	// request all raw MBus meter consumption data
	public final static String reqMBrange		= "qMBRDTr";	// Raw MBus meter data within a date range. Start date in UTC format in hex and the stop date in UTC format in hex
	
	// meter installation and removal
	public final static String announce			= "Announce";	// Meter installation & removal sub-schema parent tag
	public final static String lost				= "Lost";		// Used to denote the removal of a meter
	public final static String icid				= "ICID";		// The ICID number of the SIM associated with a meter
	public final static String type				= "Type";		// meter type
	public final static String sStrength		= "SS";			// GSM signal strength
	public final static String bStation			= "BS";			// GSM cell base station ID
	public final static String operatorName		= "OT";			// GSM operator name
	public final static String codString		= "CS";			// meter codification string, if this does not apply then 0 is sent in the data field
	public final static String curReading		= "CR";			// instantaneous register readings
	public final static String readingData		= "RD";			// the actual reading data
	public final static String crAttr			= "R";			// attribute of the current readings
	
	// time set/sync tags
	public final static String syncTime			= "ST";			// SNTP time synchronization sub-schema parent tags
	public final static String forceTime		= "FT";			// Force meter time to that of the system sub-schema parent tags
	public final static String time1			= "T1";			// The time on the meter when the time sync message was sent. In UTC format as hex
	public final static String time2			= "T2";			// The time the system received the time sync message from the meter. In UTC format as hex
	public final static String time3			= "T3";			// The time the system sent out a time sync response to the meter. In UTC format as hex
	
	public final static String timeSync			= "Time";		// Clock sync settings sub-schema parent tags
	public final static String diff				= "Dif";		// Maximum time difference allowed for clock synchronization in seconds
	public final static String trip				= "Trip";		// Maximum SNTP message trip time allowed in seconds
	public final static String retry			= "Retry";		// Number of clock sync retries allowed
	
	public final static String meterTime		= "DT";			// Current meter time in UTC format in hex
	
	// billing data tags
	public final static String billData			= "BD";			// Billing data sub-schema parent tags
	public final static String bdAttr			= "R";			// attribute of the billing data 
	public final static String regData			= "RD";			// Billing register data
	
	public final static String reqAllBD			= "qBRAll";		// Request all billing data from a meter
	public final static String reqBDrange		= "qBRDTr";		// Request billing data from a time range
	
	public final static String billingConf		= "Billing";	// Billing register recording settings sub-schema parent tags
	public final static String billEnable		= "Enable";		// Recording enable state
	public final static String billInt			= "BRInt";		// Time interval between each record
	public final static String billNumb			= "BRNum";		// Maximum number of records to store
	
	
	/**
	 * 
	 */
	public XMLTags() {
		// TODO Auto-generated constructor stub
	}

}
