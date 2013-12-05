package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WeekProfile extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "weekProfile";
	protected final static String NAMEELEMENTNAME = "name";
	protected final static String DAYELEMENTNAME = "days";
	protected final static String MONDAYATTRIBUTE = "monday";
	protected final static String TUESDAYATTRIBUTE = "tuesday";
	protected final static String WEDNESDAYATTRIBUTE = "wednesday";
	protected final static String THURSDAYATTRIBUTE = "thursday";
	protected final static String FRIDAYATTRIBUTE = "friday";
	protected final static String SATURDAYATTRIBUTE = "saturday";
	protected final static String SUNDAYATTRIBUTE = "sunday";

	private OctetString name = new OctetString();
	private int monday;
	private int tuesday;
	private int wednesday;
	private int thursday;
	private int friday;
	private int saturday;
	private int sunday;

	public WeekProfile() {
		super();
	}

	public WeekProfile(String name){
		super();
		this.name = new OctetString(name);
	}

	public WeekProfile(byte name){
		super();
		this.name = new OctetString(name);
	}

	public WeekProfile(Element element) {
		super(element);
		NodeList names = element.getElementsByTagName(NAMEELEMENTNAME);
		if (names.getLength() != 0){
			name = new OctetString(names.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create WeekProfile");
		}
		NodeList days = element.getElementsByTagName(DAYELEMENTNAME);
		if (days.getLength() != 0){
			Element day = (Element) days.item(0);
			monday = Integer.parseInt(day.getAttribute(MONDAYATTRIBUTE));
			tuesday = Integer.parseInt(day.getAttribute(TUESDAYATTRIBUTE));
			wednesday = Integer.parseInt(day.getAttribute(WEDNESDAYATTRIBUTE));
			thursday = Integer.parseInt(day.getAttribute(THURSDAYATTRIBUTE));
			friday = Integer.parseInt(day.getAttribute(FRIDAYATTRIBUTE));
			saturday = Integer.parseInt(day.getAttribute(SATURDAYATTRIBUTE));
			sunday = Integer.parseInt(day.getAttribute(SUNDAYATTRIBUTE));
		} else {
			throw new ApplicationException("Cannot create WeekProfile");
		}

	}


        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("WeekProfile:\n");
            strBuff.append("   friday="+getFriday()+"\n");
            strBuff.append("   monday="+getMonday()+"\n");
            strBuff.append("   name="+getName()+"\n");
            strBuff.append("   saturday="+getSaturday()+"\n");
            strBuff.append("   sunday="+getSunday()+"\n");
            strBuff.append("   thursday="+getThursday()+"\n");
            strBuff.append("   tuesday="+getTuesday()+"\n");
            strBuff.append("   wednesday="+getWednesday()+"\n");
            return strBuff.toString();
        }

	public byte getName() {
		return name.getOctets()[0];
	}

	public void setName(byte name) {
		this.name = new OctetString(name);
	}

	public int getMonday() {
		return monday;
	}

	public void setMonday(int monday) {
		this.monday = monday;
	}

	public int getTuesday() {
		return tuesday;
	}

	public void setTuesday(int tuesday) {
		this.tuesday = tuesday;
	}

	public int getWednesday() {
		return wednesday;
	}

	public void setWednesday(int wednesday) {
		this.wednesday = wednesday;
	}

	public int getThursday() {
		return thursday;
	}

	public void setThursday(int thursday) {
		this.thursday = thursday;
	}

	public int getFriday() {
		return friday;
	}

	public void setFriday(int friday) {
		this.friday = friday;
	}

	public int getSaturday() {
		return saturday;
	}

	public void setSaturday(int saturday) {
		this.saturday = saturday;
	}

	public int getSunday() {
		return sunday;
	}

	public void setSunday(int sunday) {
		this.sunday = sunday;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element nameElement = document.createElement(NAMEELEMENTNAME);
		nameElement.appendChild(document.createTextNode(name.convertOctetStringToString()));
		root.appendChild(nameElement);
		Element daysElement = document.createElement(DAYELEMENTNAME);
		daysElement.setAttribute(MONDAYATTRIBUTE,""+monday);
		daysElement.setAttribute(TUESDAYATTRIBUTE,""+tuesday);
		daysElement.setAttribute(WEDNESDAYATTRIBUTE,""+wednesday);
		daysElement.setAttribute(THURSDAYATTRIBUTE,""+thursday);
		daysElement.setAttribute(FRIDAYATTRIBUTE,""+friday);
		daysElement.setAttribute(SATURDAYATTRIBUTE,""+saturday);
		daysElement.setAttribute(SUNDAYATTRIBUTE,""+sunday);
		root.appendChild(daysElement);
		return root;
	}

}
