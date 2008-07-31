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
	
	
	/**
	 * 
	 */
	public XMLTags() {
		// TODO Auto-generated constructor stub
	}

}
