/**
 * 
 */
package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.genericprotocolimpl.common.RtuMessageConstant;

/**
 * @author gna
 *
 */
public class MessageHandler extends DefaultHandler{

	private String type = "";
	
	public MessageHandler(){
		
	}
	
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
		if(RtuMessageConstant.XMLCONFIG.equals(qName)){
			setType(RtuMessageConstant.XMLCONFIG);
		} else if(RtuMessageConstant.FIRMWARE_UPGRADE.equals(qName)){
			setType(RtuMessageConstant.FIRMWARE_UPGRADE);
			handleFirmWareUpgrade(attrbs);
		} else if(RtuMessageConstant.P1CODEMESSAGE.equals(qName)){
			setType(RtuMessageConstant.P1CODEMESSAGE);
			handleP1Code(attrbs);
		} else if(RtuMessageConstant.P1TEXTMESSAGE.equals(qName)){
			setType(RtuMessageConstant.P1TEXTMESSAGE);
			handleP1Text(attrbs);
		} else if(RtuMessageConstant.CONNECT_LOAD.equals(qName)){
			setType(RtuMessageConstant.CONNECT_LOAD);
			handleConnectLoad(attrbs);
		} else if(RtuMessageConstant.DISCONNECT_LOAD.equals(qName)){
			setType(RtuMessageConstant.DISCONNECT_LOAD);
			handleDisconnectLoad(attrbs);
		} else if(RtuMessageConstant.CONNECT_CONTROL_MODE.equals(qName)){
			setType(RtuMessageConstant.CONNECT_CONTROL_MODE);
			handleConnectControlMode(attrbs);
		} else if(RtuMessageConstant.LOAD_LIMIT_CONFIGURE.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
			handleLoadLimitConfiguration(attrbs);
		} else if(RtuMessageConstant.LOAD_LIMIT_DISABLE.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_DISABLE);
		} else if(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
			handleLoadLimitEPGroupIDList(attrbs);
		} else if(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE.equals(qName)){
			handleLoadLimitEmergencyProfile(attrbs);
		} else if(RtuMessageConstant.TOU_ACTIVITY_CAL.equals(qName)){
			setType(RtuMessageConstant.TOU_ACTIVITY_CAL);
			handleTOUMessage(attrbs);
		} else if(RtuMessageConstant.TOU_SPECIAL_DAYS.equals(qName)){
			setType(RtuMessageConstant.TOU_SPECIAL_DAYS);
			handleSpecialDays(attrbs);
		}else if(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE.equals(qName)){
			setType(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
			handleSpecialDaysDelete(attrbs);
		} else if(RtuMessageConstant.MBUS_DECOMMISSION.equals(qName)){
			setType(RtuMessageConstant.MBUS_DECOMMISSION);
		} else if(RtuMessageConstant.MBUS_ENCRYPTION_KEYS.equals(qName)){
			setType(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
			handleMbusEncryptionKeys(attrbs);
		}
	}

	public void characters (char buf [], int offset, int length) throws SAXException {
//    	System.out.println(new String(buf, offset, length));
    }
	
	private void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	/**********************************************
	 * XMLConfig Related messages
	 **********************************************/
	/**********************************************/
	
	
	/**********************************************
	 * FirmwareUpgrade Related messages
	 **********************************************/
	private String userfileId;
	private String activateNow;
	private String activationDate;
	
    private void handleFirmWareUpgrade(Attributes attrbs) {
    	this.userfileId = attrbs.getValue(RtuMessageConstant.FIRMWARE);
    	this.activateNow = attrbs.getValue(RtuMessageConstant.FIRMWARE_ACTIVATE_NOW);
    	this.activationDate = attrbs.getValue(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE);
	}
	
	public String getUserFileId(){
		return this.userfileId;
	}
	
	public String getActivateNow(){
		return this.activateNow;
	}
	
	
	// Need to test again!
	public boolean activateNow(){
		if(this.activateNow != null){
			return this.activateNow.equals("1");
		} else {
			return false;
		}
	}
	
	public String getActivationDate(){
		return this.activationDate;
	}
	
	public Date activationDate() throws IOException{
		return getCalendarFromString(getActivationDate()).getTime();
	}
	
	private Calendar getCalendarFromString(String strDate) throws IOException{
		Calendar cal = null;
		try {
			cal = Calendar.getInstance();
			cal.set(Calendar.DATE, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))));
			cal.set(Calendar.MONTH, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
			cal.set(Calendar.YEAR, Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));
			
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
			cal.set(Calendar.MINUTE, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
			cal.set(Calendar.SECOND, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
			cal.clear(Calendar.MILLISECOND);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Dateformat was not correct.");
		}
		return cal;
	}
	
	/**********************************************/
	
	
	/**********************************************
	 * P1 port Related messages
	 **********************************************/
	private String code;
	private String text;

	private void handleP1Code(Attributes attrbs) {
		this.code = attrbs.getValue(RtuMessageConstant.P1CODE);
	}

	private void handleP1Text(Attributes attrbs) {
		this.text = attrbs.getValue(RtuMessageConstant.P1TEXT);		
	}
	
	public String getP1Code(){
		return this.code;
	}
	
	public String getP1Text(){
		return this.text;
	}
	/**********************************************/
	
	/**********************************************
	 * Disconnect Control Related messages
	 **********************************************/
	private String connectDate;
	private String disconnectDate;
	private String mode;
	
	private void handleConnectLoad(Attributes attrbs){
		this.connectDate = attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE);
	}
	
	private void handleDisconnectLoad(Attributes attrbs){
		this.disconnectDate = attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE);
	}
	
	private void handleConnectControlMode(Attributes attrbs){
		this.mode = attrbs.getValue(RtuMessageConstant.CONNECT_MODE);
	}
	
	public String getConnectControlMode(){
		return this.mode;
	}
	
	public String getConnectDate(){
		if(this.connectDate == null){
			this.connectDate = "";
		}
		return this.connectDate;
	}
	
	public String getDisconnectDate(){
		if(this.disconnectDate == null){
			this.disconnectDate = "";
		}
		return this.disconnectDate;
	}
	/**********************************************/

	
	/**********************************************
	 * LoadLimit Related messages
	 **********************************************/
	private String normalThreshold = "";
	private String emergencyThreshold = "";
	private String overThresholdDurtion = "";
	private String epProfileId = "";
	private String epActivationTime = "";
	private String epDuration = "";
	private String epGroupIdListLookupTableId = "";
	
	private void handleLoadLimitEPGroupIDList(Attributes attrbs) {
		this.epGroupIdListLookupTableId = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID);

	}

	private void handleLoadLimitConfiguration(Attributes attrbs) {
		this.normalThreshold = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD);
		this.emergencyThreshold = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD);
		this.overThresholdDurtion = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION);
	}
	

	private void handleLoadLimitEmergencyProfile(Attributes attrbs) {
		this.epProfileId = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID);
		this.epActivationTime = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME);
		this.epDuration = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_DURATION);
	}
	
	public String getNormalThreshold() {
		return normalThreshold;
	}

	public String getEmergencyThreshold() {
		return emergencyThreshold;
	}

	public String getOverThresholdDurtion() {
		return overThresholdDurtion;
	}

	public String getEpProfileId() {
		return epProfileId;
	}

	public String getEpActivationTime() {
		return epActivationTime;
	}

	public String getEpDuration() {
		return epDuration;
	}

	public String getEpGroupIdListLookupTableId() {
		return epGroupIdListLookupTableId;
	}
	/**********************************************/

	
	/**********************************************
	 * Activity Calendar Related messages
	 **********************************************/
	
	private String touActivationDate = "";
	private String touCalendarName = "";
	private String touCodeTable = "";
	private String touUserFile = "";
	private String touSpecialDaysCodeTable = "";
	private String deleteEntry = "";
	
	private void handleTOUMessage(Attributes attrbs){
		this.touActivationDate = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_DATE);
		this.touCalendarName = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_NAME);
		this.touCodeTable = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE);
		this.touUserFile = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_USER_FILE);
	}

	private void handleSpecialDays(Attributes attrbs) {
		this.touSpecialDaysCodeTable = attrbs.getValue(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE);
	}
	
	private void handleSpecialDaysDelete(Attributes attrbs){
		this.deleteEntry = attrbs.getValue(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE_ENTRY);
	}
	
	public String getTOUActivationDate(){
		return this.touActivationDate;
	}
	
	public String getTOUCalendarName(){
		return this.touCalendarName;
	}
	
	public String getTOUCodeTable(){
		return this.touCodeTable;
	}
	
	public String getTOUUserFile(){
		return this.touUserFile;
	}
	
	public String getSpecialDaysCodeTable(){
		return this.touSpecialDaysCodeTable;
	}
	
	public String getSpecialDayDeleteEntry(){
		return this.deleteEntry;
	}
	/**********************************************/
	
	/**********************************************
	 * Mbus encryption keys Related messages
	 **********************************************/
	private String openKey = "";
	private String transferKey = "";
	
	private void handleMbusEncryptionKeys(Attributes attrbs){
		this.openKey = attrbs.getValue(RtuMessageConstant.MBUS_OPEN_KEY);
		this.transferKey = attrbs.getValue(RtuMessageConstant.MBUS_TRANSFER_KEY);
	}
	
	public String getOpenKey(){
		return this.openKey;
	}
	
	public String getTransferKey(){
		return this.transferKey;
	}
	
	/**********************************************/
}
