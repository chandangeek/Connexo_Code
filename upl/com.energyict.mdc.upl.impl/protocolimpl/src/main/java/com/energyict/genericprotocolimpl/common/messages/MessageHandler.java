
package com.energyict.genericprotocolimpl.common.messages;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;


/**
 * Generic messageHandler. The xml-RtuMessage is parsed using the {@link DefaultHandler} and the relative
 * variables are set.
 * 
 * @author gna
 *
 */
public class MessageHandler extends DefaultHandler{

	/** Represents the current Message type */ 
	private String type = "";
	
	/** Helper to indicate whether the RtuMessage content contains xml */
	private boolean isXmlInContent = false;
	
	/**
	 * {@inheritDoc}
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
		if(RtuMessageConstant.XMLCONFIG.equals(qName)){
			setType(RtuMessageConstant.XMLCONFIG);
			isXmlInContent = true;
		} else if(RtuMessageConstant.FIRMWARE_UPGRADE.equals(qName)){
			setType(RtuMessageConstant.FIRMWARE_UPGRADE);
			handleFirmWareUpgrade(attrbs);
		} else if(RtuMessageConstant.RF_FIRMWARE_UPGRADE.equals(qName)){
			setType(RtuMessageConstant.RF_FIRMWARE_UPGRADE);
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
            isXmlInContent = true;  // for certain protocols (ApolloMeter), we put in the xmlParsed CodeTable
		} else if(RtuMessageConstant.TOU_SPECIAL_DAYS.equals(qName)){
			setType(RtuMessageConstant.TOU_SPECIAL_DAYS);
			handleSpecialDays(attrbs);
            isXmlInContent = true;  // for certain protocols (ApolloMeter), we put in the xmlParsed CodeTable
		}else if(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE.equals(qName)){
			setType(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
			handleSpecialDaysDelete(attrbs);
		} else if(RtuMessageConstant.MBUS_DECOMMISSION.equals(qName)){
			setType(RtuMessageConstant.MBUS_DECOMMISSION);
		} else if(RtuMessageConstant.MBUS_ENCRYPTION_KEYS.equals(qName)){
			setType(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
			handleMbusEncryptionKeys(attrbs);
		} else if(RtuMessageConstant.SET_TIME.equals(qName)){
			setType(RtuMessageConstant.SET_TIME);
			handleSetTime(attrbs);
		} else if(RtuMessageConstant.ME_MAKING_ENTRIES.equals(qName)){
			setType(RtuMessageConstant.ME_MAKING_ENTRIES);
			handleMakingEntries(attrbs);
		} else if(RtuMessageConstant.GPRS_MODEM_SETUP.equals(qName)){
			setType(RtuMessageConstant.GPRS_MODEM_SETUP);
			handleGrpsModemSetup(attrbs);
        } else if(RtuMessageConstant.GPRS_MODEM_CREDENTIALS.equals(qName)){
            setType(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
            handleGprsModemCredentials(attrbs);
		} else if(RtuMessageConstant.TEST_MESSAGE.equals(qName)){
			setType(RtuMessageConstant.TEST_MESSAGE);
			handleTestMessage(attrbs);
		} else if(RtuMessageConstant.GLOBAL_METER_RESET.equals(qName)){
			setType(RtuMessageConstant.GLOBAL_METER_RESET);
		} else if(RtuMessageConstant.WAKEUP_ADD_WHITELIST.equals(qName)){
			setType(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
			handleWakeUpWhiteList(attrbs);
		} else if(RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY.equals(qName)){
			setType(RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY);
		} else if(RtuMessageConstant.AEE_CHANGE_HLS_SECRET.equals(qName)){
			setType(RtuMessageConstant.AEE_CHANGE_HLS_SECRET);
		} else if(RtuMessageConstant.AEE_CHANGE_LLS_SECRET.equals(qName)){
			setType(RtuMessageConstant.AEE_CHANGE_LLS_SECRET);
		} else if(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY.equals(qName)){
			setType(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY);
		} else if(RtuMessageConstant.AEE_ACTIVATE_SECURITY.equals(qName)){
			setType(RtuMessageConstant.AEE_ACTIVATE_SECURITY);
			handleActivateSecurityLevel(attrbs);
		} else if(RtuMessageConstant.MBUS_CORRECTED_VALUES.equals(qName)){
			setType(RtuMessageConstant.MBUS_CORRECTED_VALUES);
		} else if(RtuMessageConstant.MBUS_UNCORRECTED_VALUES.equals(qName)){
			setType(RtuMessageConstant.MBUS_UNCORRECTED_VALUES);
        } else if(RtuMessageConstant.MBUS_INSTALL.equals(qName)){
            setType(RtuMessageConstant.MBUS_INSTALL);
            handleMbusInstall(attrbs);
        } else if(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL.equals(qName)){
            setType(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
            handleChangeAuthentication(attrbs);
		} else {
			if(!isXmlInContent){ // If there is XML in the content, then the protocol will parse it himselve ...
				throw new SAXException("Unknown messageContent : " + qName);
			}
		}
	}
	
	/**
	 * Setter for the {@link MessageHandler#type}
	 * @param type - the message
	 */
	private void setType(String type){
		this.type = type;
	}
	
	/**
	 * Getter fo the {@link MessageHandler#type}
	 * @return
	 */
	public String getType(){
		return this.type;
	}
	
	
	/* FirmwareUpgrade Related messages 
	/**********************************************/
	private String userfileId;
	private String activationDate;
	
    private void handleFirmWareUpgrade(Attributes attrbs) {
    	this.userfileId = attrbs.getValue(RtuMessageConstant.FIRMWARE);
    	this.activationDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE));
	}
	
	public String getUserFileId(){
		return this.userfileId;
	}
	
	public String getActivationDate(){
		if(this.activationDate == null){
			this.activationDate = "";
		}
		return this.activationDate;
	}
	
	
	/* P1 port Related messages
	/**********************************************/
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
	
	/* Disconnect Control Related messages
	/**********************************************/
	private String connectDate;
	private String disconnectDate;
	private String mode;
    private String outputId;

	private void handleConnectLoad(Attributes attrbs){
		this.connectDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE));
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
	}
	
	private void handleDisconnectLoad(Attributes attrbs){
		this.disconnectDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE));
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
	}
	
	private void handleConnectControlMode(Attributes attrbs){
		this.mode = attrbs.getValue(RtuMessageConstant.CONNECT_MODE);
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
	}
	
	public String getConnectControlMode(){
		return this.mode;
	}
	
    public String getOutputId() {
        if (this.outputId == null) {
            outputId = "";
        }
        return outputId;
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

	
	/* LoadLimit Related messages
	/**********************************************/
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
		this.epActivationTime = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME));
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
	
	
	/* Activity Calendar Related messages
	/**********************************************/
	private String touActivationDate = "";
	private String touCalendarName = "";
	private String touCodeTable = "";
	private String touUserFile = "";
	private String touSpecialDaysCodeTable = "";
	private String deleteEntry = "";
	
	private void handleTOUMessage(Attributes attrbs){
		this.touActivationDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_DATE));
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
	
	
	/* Mbus encryption keys Related messages
 	/**********************************************/
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
	
	
	/* SetTime Related messages
	/**********************************************/
	private String epochTime = "";
	
	private void handleSetTime(Attributes attrbs){
		this.epochTime = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.SET_TIME_VALUE));
	}
	
	public String getEpochTime(){
		return this.epochTime;
	}


	/* Making entries Related messages
	/***********************************************/
	private String startDate = "";
	private String entries = "";
	private String interval = "";
	private String syncClock = "";
	
	private void handleMakingEntries(Attributes attrbs){
		this.startDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.ME_START_DATE));
		this.entries = attrbs.getValue(RtuMessageConstant.ME_NUMBER_OF_ENTRIES);
		this.interval = attrbs.getValue(RtuMessageConstant.ME_INTERVAL);
		this.syncClock = attrbs.getValue(RtuMessageConstant.ME_SET_CLOCK_BACK);
	}
	
	public String getMEStartDate(){
		return this.startDate;
	}
	
	public int getMEEntries() throws IOException{
		try {
			return Integer.parseInt(this.entries);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Number of entries does not contain a non numeric value: " + this.entries);
		}
	}
	
	public String getMEInterval() throws IOException {
		if(!this.interval.equalsIgnoreCase("15") && !this.interval.equalsIgnoreCase("day") && !this.interval.equalsIgnoreCase("month")){
			throw new IOException("Only '15 - day - month' is alowed in the interval field. (value: " + this.interval);
		}
		return this.interval;
	}
	
	public boolean getMESyncAtEnd(){
		if(this.syncClock != null){
			return !this.syncClock.equalsIgnoreCase("0");
		} else {
			return false;
		}
	}


	/* Changing GPRS modem parameters Related messages
	/***********************************************/
	private String gprsApn = "";
	private String gprsUsername = "";
	private String gprsPassword = "";
	
	private void handleGrpsModemSetup(Attributes attrbs){
		this.gprsApn = attrbs.getValue(RtuMessageConstant.GPRS_APN);
		this.gprsUsername = attrbs.getValue(RtuMessageConstant.GPRS_USERNAME);
		this.gprsPassword = attrbs.getValue(RtuMessageConstant.GPRS_PASSWORD);
	}

    private void handleGprsModemCredentials(Attributes attrbs) {
        this.gprsUsername = attrbs.getValue(RtuMessageConstant.GPRS_USERNAME);
        this.gprsPassword = attrbs.getValue(RtuMessageConstant.GPRS_PASSWORD);
    }

    public String getGprsApn(){
		if(this.gprsApn != null){
			return this.gprsApn;
		} else{
			return "";
		}
	}
	
	public String getGprsUsername(){
		if(this.gprsUsername != null){
			return this.gprsUsername;
		} else {
			return "";
		}
	}
	
	public String getGprsPassword(){
		if(this.gprsPassword != null){
			return this.gprsPassword;
		} else {
			return "";
		}
	}


	/* Handle TestMessage Related messages
	/***********************************************/
	private String ufId = "";
	
	private void handleTestMessage(Attributes attrbs){
		this.ufId = attrbs.getValue(RtuMessageConstant.TEST_FILE);
	}
	
	public String getTestUserFileId(){
		return (this.ufId != null)?this.ufId:"";
	}
	
	
	/* WakeUp functionality Related messages
	/**********************************************/
	private String nr1 = "";
	private String nr2 = "";
	private String nr3 = "";
	private String nr4 = "";
	private String nr5 = "";
	
	private void handleWakeUpWhiteList(Attributes attrbs){
		this.nr1 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR1);
		this.nr2 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR2);
		this.nr3 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR3);
		this.nr4 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR4);
		this.nr5 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR5);
	}

	public String getNr1() {
		return (this.nr1 != null)?this.nr1:"";
	}

	public String getNr2() {
		return (this.nr2 != null)?this.nr2:"";
	}

	public String getNr3() {
		return (this.nr3 != null)?this.nr3:"";
	}

	public String getNr4() {
		return (this.nr4 != null)?this.nr4:"";
	}

	public String getNr5() {
		return (this.nr5 != null)?this.nr5:"";
	}
	
	
	/* Authentication and Encryption functionality Related messages
	/***********************************************/
	private String securityLevel = "";
	
	private void handleActivateSecurityLevel(Attributes attrbs){
		this.securityLevel = attrbs.getValue(RtuMessageConstant.AEE_SECURITYLEVEL);
	}
	
	public int getSecurityLevel(){
		return Integer.parseInt(this.securityLevel);
	}

    /* Change the authenticationLevel */
    private String authenticationLevel = "";

    private void handleChangeAuthentication(Attributes attrbs){
        this.authenticationLevel = attrbs.getValue(RtuMessageConstant.AEE_AUTHENTICATIONLEVEL);
    }

    /**
     * Return the authenticationLevel the user gave in.
     * If the value is not a number, then return -1
     *
     * @return the value the user gave in 
     */
    public int getAuthenticationLevel() {
        try {
            return Integer.parseInt(this.authenticationLevel);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* Mbus installation related messages
    */
    private String mbusEquipmentId = "";
    private String mbusChannelToInstall = "";
    private String mbusEncryptionKey = "";
    private void handleMbusInstall(Attributes attrbs){
        this.mbusEquipmentId = attrbs.getValue(RtuMessageConstant.MBUS_EQUIPMENT_ID);
        this.mbusChannelToInstall = attrbs.getValue(RtuMessageConstant.MBUS_INSTALL_CHANNEL);
        this.mbusEncryptionKey = attrbs.getValue(RtuMessageConstant.MBUS_DEFAULT_ENCRYPTION_KEY);
    }

    /**
     * Getter for the MbusInstall EquipmentIdentifier (for the AM100 this is the RF-address of the IZAR module)
     * @return the equipmentId the user gave in
     */
    public String getMbusInstallEquipmentId(){
        return mbusEquipmentId;
    }

    /**
     * Getter for the MbusInstall channel
     * @return the channel the user gave in
     */
    public int getMbusInstallChannel(){
        return Integer.parseInt(mbusChannelToInstall);
    }

    /**
     * Getter for the MbusInstall Encryption Key
     * @return the encryption Key the user gave in
     */
    public String getMbusInstallEncryptionKey(){
        return mbusEncryptionKey;
    }

}
