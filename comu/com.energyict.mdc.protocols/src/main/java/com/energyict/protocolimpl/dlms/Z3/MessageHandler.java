/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.Z3;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MessageHandler extends DefaultHandler{
	
	private String type = "";

	public MessageHandler(){
		
	}
	
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
		if(RtuMessageConstant.DISCONNECT_LOAD.equals(qName)){
			setType(RtuMessageConstant.DISCONNECT_LOAD);
			handleDisconnectAttributes(attrbs);
		} else if(RtuMessageConstant.CONNECT_LOAD.equals(qName)){
			setType(RtuMessageConstant.CONNECT_LOAD);
			handleDisconnectAttributes(attrbs);
			
		} else if(RtuMessageConstant.PREPAID_CONFIGURED.equals(qName)){
			setType(RtuMessageConstant.PREPAID_CONFIGURED);
			handlePrepaidConfiguration(attrbs);
		} else if(RtuMessageConstant.PREPAID_ADD.equals(qName)){
			setType(RtuMessageConstant.PREPAID_ADD);
			handlePrepaidAdd(attrbs);
		} else if(RtuMessageConstant.PREPAID_DISABLE.equals(qName)){
			setType(RtuMessageConstant.PREPAID_DISABLE);
		} else if(RtuMessageConstant.PREPAID_ENABLE.equals(qName)){
			setType(RtuMessageConstant.PREPAID_ENABLE);
		} else if(RtuMessageConstant.PREPAID_READ.equals(qName)){
			setType(RtuMessageConstant.PREPAID_READ);
			
		} else if(RtuMessageConstant.LOAD_LIMIT_CONFIGURE.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
			handleLoadLimitConfiguration(attrbs);
		} else if(RtuMessageConstant.LOAD_LIMIT_ENABLE.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_ENABLE);
		} else if(RtuMessageConstant.LOAD_LIMIT_DISABLE.equals(qName)){
			setType(RtuMessageConstant.LOAD_LIMIT_DISABLE);
		}
		
	}
	
	private void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	/**********************************************
	 *  Connect and disconnect related methods
	 **********************************************/
	private String result = "";
	
	private void handleDisconnectAttributes(Attributes attrbs){
		this.result = attrbs.getValue(RtuMessageConstant.DIGITAL_OUTPUT);
	}
	
	public String getResult(){
		return this.result;
	}
	
	/***************************************************************************************/
	
	
	/**********************************************
	 * Prepaid related methods
	 **********************************************/
	private String budget = "";
	private String threshold = "";
	private String readFrequency = "";
	private String[] multiplier = new String[8];
	
	private void handlePrepaidConfiguration(Attributes attrbs) {
		this.budget = attrbs.getValue(RtuMessageConstant.PREPAID_BUDGET);
		this.threshold = attrbs.getValue(RtuMessageConstant.PREPAID_THRESHOLD);
		this.readFrequency = attrbs.getValue(RtuMessageConstant.PREPAID_READ_FREQUENCY);
		for(int i = 1; i < 9; i++){
			this.multiplier[i-1] = attrbs.getValue(RtuMessageConstant.PREPAID_MULTIPLIER+i);
		}
	}
	
	private void handlePrepaidAdd(Attributes attrbs){
		this.budget = attrbs.getValue(RtuMessageConstant.PREPAID_BUDGET);
	}
	
	public String getBudget(){
		return this.budget;
	}
	
	public String getThreshold(){
		return this.threshold;
	}
	
	public String getReadFrequency(){
		return this.readFrequency;
	}
	
	public String getMultiplier(int register){
		return this.multiplier[register];
	}
	
	/***************************************************************************************/
	
	
	/**********************************************
	 * LoadLimit related methods
	 **********************************************/
	private String llreadFrequency = "";
	private String llThreshold = "";
	private String llDuration = "";
	private String llD1Invert = "";
	private String llD2Invert = "";
	private String llActivateNow = "";
	
	private void handleLoadLimitConfiguration(Attributes attrbs){
		this.llreadFrequency = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_READ_FREQUENCY);
		this.llThreshold = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_THRESHOLD);
		this.llDuration = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_DURATION);
		this.llD1Invert = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_D1_INVERT);
		this.llD2Invert = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_D2_INVERT);
		this.llActivateNow = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_ACTIVATE_NOW);
	}
	
	public String getLLReadFrequency(){
		return this.llreadFrequency;
	}
	
	public String getLLThreshold(){
		return this.llThreshold;
	}
	
	public String getLLDuration(){
		return this.llDuration;
	}
	
	public String getLLD1Invert(){
		return this.llD1Invert;
	}
	
	public String getLLD2Invert(){
		return this.llD2Invert;
	}
	
	public String getActivateNow(){
		return this.llActivateNow;
	}
	/***************************************************************************************/
}
