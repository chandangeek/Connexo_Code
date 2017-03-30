/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.elster.a1800;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



//import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;

public class MessageHandler extends DefaultHandler{
	
	final static String SETPDIVISOR = "SETLPDIVISOR";   
	private String type = "";
	private int chn;
	private int div;

	public MessageHandler(){
		
	}
	
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
		if (SETPDIVISOR.equals(qName)) {
			setType(SETPDIVISOR);
			handleLPDivisor(attrbs);
		}
		
	}
	
	private void handleLPDivisor(Attributes attrbs) {
		setChn(Integer.parseInt(attrbs.getValue("Channel")));
		setDiv(Integer.parseInt(attrbs.getValue("Divisor")));
	}

	private void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}

	protected void setDiv(int div) {
		this.div = div;
	}

	public int getDivisor() {
		return div;
	}

	protected void setChn(int chn) {
		this.chn = chn;
	}

	public int getChannel() {
		return chn;
	}
	
}
