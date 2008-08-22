/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.tables.EnableTable;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class ObjectFactory {
	
	private int DEBUG = 0;
	
	private ActarisACE4000 		aace;
	private Acknowledge 		acknowledge;
	private FirmwareVersion 	firmwareVersion = null;
	private Serialnumber		serialnumber 	= null;
	private AutoPushConfig		autoPushConfig	= null;
	private FullMeterConfig		fullMeterConfig	= null;
	private Announcement		announcement	= null;
	private CurrentReadings		currentReadings = null;
	private LoadProfile			loadProfile		= null;
	private MBLoadProfile		mbLoadProfile	= null;
	private MBCurrentReadings	mbCurrReadings	= null;
	private Time				time			= null;
	private BillingData			billingData		= null;
	
	private int tempTrackingID = -1;
	/**
	 * 
	 */
	public ObjectFactory(ActarisACE4000 aace) {
		this.aace = aace;
	}
	
	public FullMeterConfig getFullMeterConfig(){
		if(fullMeterConfig == null){
			fullMeterConfig = new FullMeterConfig(this);
		}
		return fullMeterConfig;
	}
	
	
	/** 
	 * Send a request for full meter configuration
	 * @throws IOException
	 */
	public void sendFullMeterConfigRequest() throws IOException{
		getFullMeterConfig().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getFullMeterConfig().prepareXML();
		getFullMeterConfig().request();
	}
	
	public AutoPushConfig getAutoPushConfig(){
		if(autoPushConfig == null){
			autoPushConfig = new AutoPushConfig(this);
		}
		return autoPushConfig;
	}
	
	/**
	 * Send xml with the meters autopush config - Startime, Stoptime pushwindow ...
	 * @throws IOException
	 * @param enabled - enabled daily push
	 * @param start - startTime in minutes after midnight
	 * @param stop - stopTime in minutes after midnight
	 * @param random - true/false if push can start randomly in pushwindow
	 */
	public void setAutoPushConfig(int enabled, int start, int stop, boolean random) throws IOException{
		getAutoPushConfig().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getAutoPushConfig().prepareXML(enabled, start, stop, random);
		getAutoPushConfig().request();
	}
	
	public Announcement getAnnouncement(){
		if(announcement == null){
			announcement = new Announcement(this);
		}
		return announcement;
	}
	
	public CurrentReadings getCurrentReadings(){
		if(currentReadings == null){
			currentReadings = new CurrentReadings(this);
		}
		return currentReadings;
	}
	
	public MBCurrentReadings getMBCurrentReadings(){
		if(mbCurrReadings == null){
			mbCurrReadings = new MBCurrentReadings(this);
		}
		return mbCurrReadings;
	}
	
	public LoadProfile getLoadProfile(){
		if(loadProfile == null){
			loadProfile = new LoadProfile(this);
		}
		return loadProfile;
	}
	
	/**
	 * Request all the loadprofile data
	 * @throws IOException
	 */
	public void sendLoadProfileRequest() throws IOException{
		getLoadProfile().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getLoadProfile().prepareXML();
		getLoadProfile().request();
	}
	
	/**
	 * Request the loadprofile data from a certain point in time
	 * @param from equals the the point in time
	 * @throws IOException
	 */
	public void sendLoadProfileRequest(Date from) throws IOException{
		getLoadProfile().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getLoadProfile().prepareXML(from);
		getLoadProfile().request();
	}
	
	public MBLoadProfile getMBLoadProfile(){
		if(mbLoadProfile == null){
			mbLoadProfile = new MBLoadProfile(this);
		}
		return mbLoadProfile;
	}
	
	/**
	 * Request all the MBus loadprofile data
	 * @throws IOException
	 */
	public void sendMBLoadProfileRequest() throws IOException{
		getMBLoadProfile().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMBSerialNumber().get(0));
		getMBLoadProfile().prepareXML();
		getMBLoadProfile().request();
	}
	
	/**
	 * Request the MBus loadprofile data from a certain point in time
	 * @param from equals the point in time
	 * @throws IOException
	 */
	public void sendMBLoadProfileRequest(Date from) throws IOException{
		getMBLoadProfile().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMBSerialNumber().get(0));
		getMBLoadProfile().prepareXML(from);
		getMBLoadProfile().request();
	}
	
	public BillingData getBillingData(){
		if(billingData == null){
			billingData = new BillingData(this);
		}
		return billingData;
	}
	
	/**
	 * Request all billingdata from the E-meter
	 * @throws IOException
	 */
	public void sendBDRequest() throws IOException{
		getBillingData().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getBillingData().prepareXML();
		getBillingData().request();
	}
	
	/**
	 * Request the billingdata from the E-meter from a certain point in time
	 * @param from equals the point in time
	 * @throws IOException
	 */
	public void sendBDRequest(Date from) throws IOException{
		getBillingData().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getBillingData().prepareXML(from);
		getBillingData().request();
	}
	
	/**
	 * Send the E-meters billingdata  configuration
	 * @param enabled - billingdata is enabled/disabled
	 * @param intervals	- interval in seconds between two records
	 * @param numbOfInt - number of records to store
	 * @throws IOException
	 */
	public void sendBDConfig(int enabled, int intervals, int numbOfInt) throws IOException{
		getBillingData().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getBillingData().prepareXMLConfig(enabled, intervals, numbOfInt);
		getBillingData().request();
	}
	
	public Time getTime(){
		if(time == null){
			time = new Time(this);
		}
		return time;
	}
	
	/**
	 * Force the meter time to the system time
	 * @throws IOException
	 */
	public void sendForceTime() throws IOException{
		getTime().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getTime().prepareXML();
		getTime().request();
	}
	
	/**
	 * Sync the meter time to the system time
	 * @throws IOException
	 */
	public void sendSyncTime() throws IOException{
		getTime().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getTime().prepareSyncXML();
		getTime().request();
	}
	
	/**
	 * Send the timesync configuration
	 * @param diff - maximum allowed time difference for timesync to take place (in seconds)
	 * @param trip - maximum SNTP trip time in seconds
	 * @param retry - maximum number of clock sync retries allowed
	 * @throws IOException
	 */
	public void sendTimeConfig(int diff, int trip, int retry) throws IOException{
		getTime().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getTime().prepareXMLConfig(diff, trip, retry);
		getTime().request();
	}
	
	public Acknowledge getAcknowledge(){
		if(acknowledge == null){
			acknowledge = new Acknowledge(this);
		}
		return acknowledge;
	}
	
	/**
	 * Send an acknowledgment with for a certain message with a given tracking number
	 * @param tracker equals the tracking number
	 * @throws IOException
	 */
	public void sendAcknowledge(int tracker) throws IOException{
		getAcknowledge().setTrackingID(tracker);
		getAace().setNecessarySerialNumber(getAcknowledge().getSerialNumber());
		getAcknowledge().prepareXML();
		getAcknowledge().request();
	}
	
	public FirmwareVersion getFirmwareVersion(){
		if(firmwareVersion == null){
			firmwareVersion = new FirmwareVersion(this);
		}
		return firmwareVersion;
	}
	
	/**
	 * Request the firmwareversions of the meter
	 * @throws IOException
	 */
	public void sendFirmwareRequest() throws IOException{
		getFirmwareVersion().setTrackingID(getAace().getTracker());
		getAace().setNecessarySerialNumber(getAace().getMasterSerialNumber());
		getFirmwareVersion().prepareXML();
		getFirmwareVersion().request();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ObjectFactory of = new ObjectFactory(null);
		String testStr = "<Mpush><MD><M>E2G8NRB1D2110D07</M><T>0033</T></MD></Mpush>";
		
//		of.parseXML(testStr);
	}

	public ActarisACE4000 getAace() {
		return aace;
	}

	public void setAace(ActarisACE4000 aace) {
		this.aace = aace;
	}
	
	/**
	 * Parse the received XML to the corresponding object
	 * @param xml - the received MeterXML string
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws BusinessException 
	 */
	public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException, BusinessException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		if(DEBUG >=1)System.out.println(xml);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element topElement = document.getDocumentElement();
			parseElements(topElement);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new ParserConfigurationException("Failed to make a new builder from the documentBuilderfactory" + e.getMessage() + "(Received xml: " + xml + ")");
		} catch (SAXException e) {
			e.printStackTrace();
			throw new SAXException("Could not parse the received xmlString." + e.getMessage() + "(Received xml: " + xml + ")");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage() + "(Received xml: " + xml + ")");
		} catch (DOMException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
		} 
	}
	
	public void parseXML(InputStream is) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			Element topElement = document.getDocumentElement();
			parseElements(topElement);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new ParserConfigurationException("Failed to make a new builder from the documentBuilderfactory");
		} catch (SAXException e) {
			e.printStackTrace();
			throw new SAXException("Could not parse the received xmlString.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("The received xml is not a valid inputSource");
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}
	
	private void parseElements(Element element) throws IOException, SQLException, BusinessException{
		
		String nodeName = element.getNodeName();
		setTempTrackingID(-1);	// clear the tracker again
		
		try {
			if(nodeName.equalsIgnoreCase(XMLTags.mPush)){
				NodeList nodes = element.getElementsByTagName(XMLTags.meterData);
				Element md = (Element)nodes.item(0);
				
				if(md.getNodeName().equalsIgnoreCase(XMLTags.meterData)){
					NodeList mdNodeList = md.getChildNodes();
					
					for(int i = 0; i < mdNodeList.getLength(); i++){
						Element mdElement = (Element)mdNodeList.item(i);
						
						if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.serialNumber)){
							getAace().setSerialnumbers(mdElement.getTextContent());
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.tracker)){
							setTempTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));	// add the radius because we receive hex
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPr)){
							log(Level.INFO, "Received a loadProfile element.");
							getLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
							getLoadProfile().setElement(mdElement);	
						}
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPrAbs)){
							log(Level.INFO, "Received a loadProfile element.");
							getLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
							getLoadProfile().setElement(mdElement);
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.mbusLP)){
							log(Level.INFO, "Received an MBus loadProfile element.");
							getMBLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
							getMBLoadProfile().setElement(mdElement);
						}
						
						// TODO extend this functionality
//					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.announce)){
//						getAnnouncement().setElement(mdElement);
//					}
							
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.curReading)){
							log(Level.INFO, "Received current readings from meter.");
							getCurrentReadings().setElement(mdElement);
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.mbusCReading)){
							log(Level.INFO, "Received current readings from MBus meter.");
							getMBCurrentReadings().setElement(mdElement);
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.resFirmware)){
							log(Level.INFO, "Received firmware versions.");
							getFirmwareVersion().setElement(mdElement);
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.billData)){
							log(Level.INFO, "Received billing data from meter.");
							getBillingData().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
							getBillingData().setElement(mdElement);
						}
						
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.configHandling)){
							log(Level.INFO, "Received configuration from meter.");
							getFullMeterConfig().setElement(mdElement);
						}
						
						// TODO verify timezone Stuff
						else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.meterTime)){
							log(Level.INFO, "Received timing parameters.");
							getTime().setElement(mdElement);
						}
					}
				}
				else
					throw new ApplicationException("Unknown tag found in xml responce: " + nodes.item(0).getNodeName());
			}
			else
				throw new ApplicationException("Unknown tag found in xml responce: " + element.getNodeName());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw e;
		} catch (DOMException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (BusinessException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	protected void log(Level level, String msg){
		getAace().getLogger().log(level, msg);
	}

	protected int getTempTrackingID() {
		return tempTrackingID;
	}

	protected void setTempTrackingID(int tempTrackingID) {
		this.tempTrackingID = tempTrackingID;
	}

}
