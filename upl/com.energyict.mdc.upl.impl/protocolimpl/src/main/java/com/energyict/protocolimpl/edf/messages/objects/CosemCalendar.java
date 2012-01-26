package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CosemCalendar extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "cosemCalendar";

	private static int DAYLIGHTSAVINGSTIMEACTIVEVALUE = 128;

	private OctetString calendar = new OctetString();

	public CosemCalendar() {
		super();
		byte[] octetString = new byte[12];
		for (int i = 0 ; i<12; i++){
			octetString[i] = 0;
		}
		calendar = new OctetString(octetString);
	}

	public CosemCalendar(Calendar timestamp, boolean daylightSavingTimeActive) {
		super();		
		byte[] octetString = new byte[12];
		octetString[0]= (byte)((timestamp.get(Calendar.YEAR) / 256)& 0xFF);
		octetString[1]= (byte)((timestamp.get(Calendar.YEAR) % 256)& 0xFF);
		octetString[2]= (byte)((timestamp.get(Calendar.MONTH)+1)& 0xFF);
		octetString[3]= (byte)((timestamp.get(Calendar.DAY_OF_MONTH))& 0xFF);
		octetString[4]= (byte)((timestamp.get(Calendar.DAY_OF_WEEK))& 0xFF);
		octetString[5]= (byte)((timestamp.get(Calendar.HOUR_OF_DAY))& 0xFF);
		octetString[6]= (byte)((timestamp.get(Calendar.MINUTE))& 0xFF);
		octetString[7]= (byte)((timestamp.get(Calendar.SECOND))& 0xFF);
		octetString[8]= 0;
		octetString[9]= (byte)0x80;
		octetString[10]= 0;
		octetString[11]= (byte)(((daylightSavingTimeActive) ? DAYLIGHTSAVINGSTIMEACTIVEVALUE : 0) & 0xFF);
		calendar = new OctetString(octetString);
	}

	public CosemCalendar(OctetString octets){
		this.calendar = octets;
	}
	
	public CosemCalendar(Element element) {
		super(element);
		String octetString = element.getFirstChild().getNodeValue();
		calendar = new OctetString(octetString);
	}
	
        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("CosemCalendar:\n");
            strBuff.append("   calendar="+getCalendar()+"\n");
            strBuff.append("   date="+getCalendar().getTime()+"\n");
            strBuff.append("   daylightSavingTimeActive="+isDaylightSavingTimeActive()+"\n");
            strBuff.append("   octetString="+getOctetString()+"\n");
            return strBuff.toString();
        }        
                
	public OctetString getOctetString() {
		return calendar;
	}

	public void setOctetString(OctetString calendar) {
		this.calendar = calendar;
	}

	public boolean isDaylightSavingTimeActive(){
		return (((int)(calendar.getOctets())[11]) & 0xFF) == DAYLIGHTSAVINGSTIMEACTIVEVALUE;
		
	}
	
	public Calendar getCalendar(){
		Calendar result = new GregorianCalendar();
		
		result.set((((int)(calendar.getOctets())[0]) & 0xFF) * 256 + (((int)(calendar.getOctets())[1]) & 0xFF),
		(((int)(calendar.getOctets())[2]) & 0xFF)-1,
		(((int)(calendar.getOctets())[3]) & 0xFF),
		(((int)(calendar.getOctets())[5]) & 0xFF),
		(((int)(calendar.getOctets())[6]) & 0xFF),
		(((int)(calendar.getOctets())[7]) & 0xFF));
		
		return result;
	}
	
	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.appendChild(document.createTextNode(calendar.convertOctetStringToString()));
		return root;
	}

}
