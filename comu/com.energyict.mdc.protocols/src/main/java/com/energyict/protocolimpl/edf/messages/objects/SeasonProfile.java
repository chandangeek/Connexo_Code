package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Calendar;

public class SeasonProfile extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "seasonProfile";
	protected final static String NAMEELEMENTNAME = "name";
	protected final static String STARTELEMENTNAME = "start";
	protected final static String WEEKELEMENTNAME = "week";

	private OctetString name = new OctetString();
	private CosemCalendar start = new CosemCalendar();
	private OctetString week = new OctetString();


	public SeasonProfile() {
		super();
	}

	public SeasonProfile(String name, Calendar calendar, boolean isDaylightSavingsTimeActive, String week){
		super();
		this.name = new OctetString(name);
		this.start = new CosemCalendar(calendar,isDaylightSavingsTimeActive);
		this.week = new OctetString(week);
	}

	public SeasonProfile(byte name, byte[] start, byte week){
		super();
		this.name = new OctetString(name);
		this.start = new CosemCalendar(new OctetString(start));
		this.week = new OctetString(week);
	}

	public SeasonProfile(Element element) {
		super(element);
		NodeList names = element.getElementsByTagName(NAMEELEMENTNAME);
		if (names.getLength() != 0){
			name = new OctetString(names.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create SeasonProfile");
		}
		NodeList starts = element.getElementsByTagName(STARTELEMENTNAME);
		if (starts.getLength() != 0){
			start = new CosemCalendar((Element) starts.item(0).getFirstChild());
		} else {
			throw new ApplicationException("Cannot create SeasonProfile");
		}
		NodeList weeks = element.getElementsByTagName(WEEKELEMENTNAME);
		if (weeks.getLength() != 0){
			week = new OctetString(weeks.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create SeasonProfile");
		}
	}

        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("SeasonProfile:\n");
            strBuff.append("   name="+getName()+"\n");
            strBuff.append("   start="+getStart()+"\n");
            strBuff.append("   week="+getWeek()+"\n");
            return strBuff.toString();
        }

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element nameElement = document.createElement(NAMEELEMENTNAME);
		nameElement.appendChild(document.createTextNode(name.convertOctetStringToString()));
		root.appendChild(nameElement);
		Element startElement = document.createElement(STARTELEMENTNAME);
		startElement.appendChild(start.generateXMLElement(document));
		root.appendChild(startElement);
		Element weekElement = document.createElement(WEEKELEMENTNAME);
		weekElement.appendChild(document.createTextNode(week.convertOctetStringToString()));
		root.appendChild(weekElement);
		return root;
	}

	public byte getName() {
		return name.getOctets()[0];
	}

	public void setName(byte name) {
		this.name = new OctetString(name);
	}

	public CosemCalendar getStart() {
		return start;
	}

	public void setStart(CosemCalendar start) {
		this.start = start;
	}

	public byte getWeek() {
		return week.getOctets()[0];
	}

	public void setWeek(byte week) {
		this.week = new OctetString(week);
	}

}
