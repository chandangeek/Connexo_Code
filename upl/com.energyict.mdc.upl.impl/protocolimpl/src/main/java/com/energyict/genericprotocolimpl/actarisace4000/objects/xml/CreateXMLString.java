/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects.xml;

import org.w3c.dom.Node;

import com.energyict.genericprotocolimpl.actarisace4000.objects.ObjectFactory;
import com.energyict.xml.xmlhelper.DomHelper;

/**
 * @author gna
 *
 */
public class CreateXMLString {
	
	private DomHelper dh = null;
	private ObjectFactory of;
	
	/**
	 * 
	 */
	public CreateXMLString() {
	}

	public CreateXMLString(ObjectFactory objectFactory) {
		this.of = objectFactory;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreateXMLString cxmls = new CreateXMLString();
		DomHelper dh = new DomHelper(XMLTags.mPull);
		
		Node md = dh.addElement(XMLTags.meterData);
//		dh.addElement(md, cxmls.meterData);
		dh.addElement(md, XMLTags.serialNumber, String.valueOf(1234));
		dh.addElement(md, XMLTags.tracker, String.valueOf(1));
		dh.addElement(md, XMLTags.reqFirmware);
		
		System.out.println(dh.toString());
		System.out.println(dh.toXmlString());
	}

	public String createRequest(String tag, int tracker) {
		dh = new DomHelper(XMLTags.mPull);
		Node md = dh.addElement(XMLTags.meterData);
		dh.addElement(md, XMLTags.serialNumber, getOf().getAace().getDeviceSerialnumber());
		dh.addElement(md, XMLTags.tracker, String.valueOf(tracker));
		dh.addElement(md, tag);
		return (dh.toXmlString()).substring(dh.toXmlString().indexOf("?>")+2) ;
	}

	public ObjectFactory getOf() {
		return of;
	}

	public void setOf(ObjectFactory of) {
		this.of = of;
	}
	

}
