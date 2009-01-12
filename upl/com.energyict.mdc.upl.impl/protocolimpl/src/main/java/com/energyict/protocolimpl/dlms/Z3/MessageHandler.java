package com.energyict.protocolimpl.dlms.Z3;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.genericprotocolimpl.common.RtuMessageConstant;

public class MessageHandler extends DefaultHandler{
	
	private String result = "";
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
	
	private void handleDisconnectAttributes(Attributes attrbs){
		this.result = attrbs.getValue(RtuMessageConstant.DIGITAL_OUTPUT);
	}
	
	public String getResult(){
		return this.result;
	}
}
