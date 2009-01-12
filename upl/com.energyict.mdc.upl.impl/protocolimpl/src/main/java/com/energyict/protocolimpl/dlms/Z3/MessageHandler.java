package com.energyict.protocolimpl.dlms.Z3;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.genericprotocolimpl.common.RtuMessageConstant;

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
		}
		
		
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		 
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
}
