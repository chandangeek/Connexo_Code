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
	
	/**
	 * 
	 */
	public XMLTags() {
		// TODO Auto-generated constructor stub
	}

}
