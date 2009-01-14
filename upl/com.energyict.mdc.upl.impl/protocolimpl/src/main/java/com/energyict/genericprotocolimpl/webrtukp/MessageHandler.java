/**
 * 
 */
package com.energyict.genericprotocolimpl.webrtukp;

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
	
	
}
