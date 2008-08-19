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
	
	private int DEBUG = 1;
	
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
	
	public void sendFullMeterConfigRequest() throws IOException{
		getFullMeterConfig().setTrackingID(getAace().getTracker());
		getFullMeterConfig().prepareXML();
		getFullMeterConfig().request();
	}
	
	// TODO change this to a getautopushconfig and a sendrequest
	public AutoPushConfig setAutoPushConfig() throws IOException{
		if(autoPushConfig == null){
			autoPushConfig = new AutoPushConfig(this);
			autoPushConfig.setTrackingID(getAace().getTracker());
			autoPushConfig.prepareXML(EnableTable.enabled, "A", "78", false);
			autoPushConfig.request();
		}
		return autoPushConfig;
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
	
	public void sendLoadProfileRequest() throws IOException{
		getLoadProfile().setTrackingID(getAace().getTracker());
		getLoadProfile().prepareXML();
		getLoadProfile().request();
	}
	
	public void sendLoadProfileRequest(Date from) throws IOException{
		getLoadProfile().setTrackingID(getAace().getTracker());
		getLoadProfile().prepareXML(from);
		getLoadProfile().request();
	}
	
	public MBLoadProfile getMBLoadProfile(){
		if(mbLoadProfile == null){
			mbLoadProfile = new MBLoadProfile(this);
		}
		return mbLoadProfile;
	}
	
	public void sendMBLoadProfileRequest() throws IOException{
		getMBLoadProfile().setTrackingID(getAace().getTracker());
		getMBLoadProfile().prepareXML();
		getMBLoadProfile().request();
	}
	
	public void sendMBLoadProfileRequest(Date from) throws IOException{
		getMBLoadProfile().setTrackingID(getAace().getTracker());
		getMBLoadProfile().prepareXML(from);
		getMBLoadProfile().request();
	}
	
	public BillingData getBillingData(){
		if(billingData == null){
			billingData = new BillingData(this);
		}
		return billingData;
	}
	
	public void sendBDRequest() throws IOException{
		getBillingData().setTrackingID(getAace().getTracker());
		getBillingData().prepareXML();
		getBillingData().request();
	}
	
	public void sendBDRequest(Date from) throws IOException{
		getBillingData().setTrackingID(getAace().getTracker());
		getBillingData().prepareXML(from);
		getBillingData().request();
	}
	
	public Time getTime(){
		if(time == null){
			time = new Time(this);
		}
		return time;
	}
	
	public void sendForceTime() throws IOException{
		getTime().setTrackingID(getAace().getTracker());
		getTime().prepareXML();
		getTime().request();
	}
	
	public void sendSyncTime() throws IOException{
		getTime().setTrackingID(getAace().getTracker());
		getTime().prepareSyncXML();
		getTime().request();
	}
	
	public Acknowledge getAcknowledge(){
		if(acknowledge == null){
			acknowledge = new Acknowledge(this);
		}
		return acknowledge;
	}
	
	public void sendAcknowledge(int tracker) throws IOException{
		getAcknowledge().setTrackingID(tracker);
		getAcknowledge().prepareXML();
		getAcknowledge().request();
	}
	
	public FirmwareVersion getFirmwareVersion(){
		if(firmwareVersion == null){
			firmwareVersion = new FirmwareVersion(this);
		}
		return firmwareVersion;
	}
	
	public void sendFirmwareRequest() throws IOException{
		getFirmwareVersion().setTrackingID(getAace().getTracker());
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
	
	public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		if(DEBUG >=1)System.out.println(xml);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private void parseElements(Element element) throws DOMException, IOException, SQLException, BusinessException{
		String nodeName = element.getNodeName();
		setTempTrackingID(-1);	// clear the tracker again
		if(nodeName.equalsIgnoreCase(XMLTags.mPush)){
			NodeList nodes = element.getElementsByTagName(XMLTags.meterData);
			Element md = (Element)nodes.item(0);
			
			if(md.getNodeName().equalsIgnoreCase(XMLTags.meterData)){
				NodeList mdNodeList = md.getChildNodes();
				
				for(int i = 0; i < mdNodeList.getLength(); i++){
					Element mdElement = (Element)mdNodeList.item(i);
					
					if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.serialNumber)){
						getAace().setPushedSerialnumber(mdElement.getTextContent());
						log(Level.INFO, "Received data from meter with serialnumber " + getAace().getPushedSerialnumber());
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.tracker)){
						setTempTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));	// add the radius because we receive hex
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPr)){
						getLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
						getLoadProfile().setElement(mdElement);	
					}
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPrAbs)){
						getLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
						getLoadProfile().setElement(mdElement);
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.mbusLP)){
						getMBLoadProfile().setTrackingID(getTempTrackingID());		// need the tracking ID to 'ACK'the UDP packet
						getMBLoadProfile().setElement(mdElement);
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.announce)){
						getAnnouncement().setElement(mdElement);
					}
						
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.curReading)){
						log(Level.INFO, "Received current readings from meter with serialnumber " + getAace().getPushedSerialnumber());
						getCurrentReadings().setElement(mdElement);
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.mbusCReading)){
						log(Level.INFO, "Received current readings from MBus meter with serialnumber " + getAace().getPushedSerialnumber());
						getMBCurrentReadings().setElement(mdElement);
					}
					
					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.resFirmware)){
						getFirmwareVersion().setElement(mdElement);
					}
					
					// TODO uncomment
//					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.billData)){
//						log(Level.INFO, "Received billing data from meter with serialnumber " + getAace().getPushedSerialnumber());
//						getBillingData().setElement(mdElement);
//					}
//					
//					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.billingConf)){
//						getBillingData().setConfig(mdElement);
//					}
					
					// TODO verify timezone Stuff
//					else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.meterTime)){
//						getTime().setElement(mdElement);
//					}
				}
			}
			else
				throw new ApplicationException("Unknown tag found in xml responce: " + nodes.item(0).getNodeName());
		}
		else
			throw new ApplicationException("Unknown tag found in xml responce: " + element.getNodeName());
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
