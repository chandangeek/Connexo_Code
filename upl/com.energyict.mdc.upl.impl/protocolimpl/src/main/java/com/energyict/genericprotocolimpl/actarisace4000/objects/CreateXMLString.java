/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.xml.xmlhelper.DomHelper;

/**
 * @author gna
 *
 */
public class CreateXMLString {
	
	private DomHelper dh = null;
	private ObjectFactory of;
	
	private String root 		= "MPull";
	private String meterData 	= "MD";
	private String serialNumber = "M";
	private String tracker 		= "T";
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
		DomHelper dh = new DomHelper(cxmls.root);
		
		dh.addElement(cxmls.meterData);
		dh.addElement(cxmls.serialNumber, String.valueOf(1234));
		dh.addElement(cxmls.tracker, String.valueOf(1));
		dh.addElement("qV");
		
		System.out.println(dh.toString());
		System.out.println(dh.toXmlString());
	}

	public String createRequest(String tag, int tracker) {
		createBasic(tracker);
		dh.addElement(tag);
		return (dh.toXmlString()).substring(dh.toXmlString().indexOf("?>")+2) ;
	}
	
	public void createBasic(int tracker){
		dh = new DomHelper(root);
		dh.addElement(meterData);
		dh.addElement(serialNumber, getOf().getAace().getDeviceSerialnumber());
		dh.addElement(this.tracker, String.valueOf(tracker));
	}

	public ObjectFactory getOf() {
		return of;
	}

	public void setOf(ObjectFactory of) {
		this.of = of;
	}
	

}
