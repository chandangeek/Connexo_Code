/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityCalendar extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "activityCalendar";
	protected final static String ACTIVENAME = "activeCalendarName";
	protected final static String ACTIVESEASONPROFILES = "activeSeasonProfiles";
	protected final static String ACTIVEWEEKPROFILES = "activeWeekProfiles";
	protected final static String ACTIVEDAYPROFILES = "activeDayProfiles";
	protected final static String PASSIVENAME = "passiveCalendarName";
	protected final static String PASSIVESEASONPROFILES = "passiveSeasonProfiles";
	protected final static String PASSIVEWEEKPROFILES = "passiveWeekProfiles";
	protected final static String PASSIVEDAYPROFILES = "passiveDayProfiles";
	protected final static String SWITCHTIMENAME = "activatePassiveCalendarTime";


	private OctetString activeCalendarName = new OctetString();
	private List activeSeasonProfiles = new ArrayList();
	private List activeWeekProfiles = new ArrayList();
	private List activeDayProfiles = new ArrayList();
	private OctetString passiveCalendarName = new OctetString();
	private List passiveSeasonProfiles = new ArrayList();
	private List passiveWeekProfiles = new ArrayList();
	private List passiveDayProfiles = new ArrayList();
	private CosemCalendar activatePassiveCalendarTime = new CosemCalendar();

	public ActivityCalendar() {
		super();
	}

	public ActivityCalendar(String activeCalendarName,	String passiveCalendarName) {
		super();
		this.activeCalendarName = new OctetString(activeCalendarName);
		this.passiveCalendarName = new OctetString(passiveCalendarName);
	}

	public ActivityCalendar(byte activeCalendarName, byte passiveCalendarName) {
		super();
		this.activeCalendarName = new OctetString(activeCalendarName);
		this.passiveCalendarName = new OctetString(passiveCalendarName);
	}

	public ActivityCalendar(Element element) {
		super(element);
		NodeList activeNames = element.getElementsByTagName(ACTIVENAME);
		if (activeNames.getLength() != 0){
			activeCalendarName = new OctetString(activeNames.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		NodeList listNode = element.getElementsByTagName(ACTIVESEASONPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				SeasonProfile segment = new SeasonProfile(thisElement);
				activeSeasonProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		listNode = element.getElementsByTagName(ACTIVEWEEKPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				WeekProfile segment = new WeekProfile(thisElement);
				activeWeekProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		listNode = element.getElementsByTagName(ACTIVEDAYPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				DayProfile segment = new DayProfile(thisElement);
				activeDayProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		NodeList passiveNames = element.getElementsByTagName(PASSIVENAME);
		if (passiveNames.getLength() != 0){
			passiveCalendarName = new OctetString(passiveNames.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create SeasonProfile");
		}
		listNode = element.getElementsByTagName(PASSIVESEASONPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				SeasonProfile segment = new SeasonProfile(thisElement);
				passiveSeasonProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		listNode = element.getElementsByTagName(PASSIVEWEEKPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				WeekProfile segment = new WeekProfile(thisElement);
				passiveWeekProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		listNode = element.getElementsByTagName(PASSIVEDAYPROFILES);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element thisElement = (Element) childs.item(i);
				DayProfile segment = new DayProfile(thisElement);
				passiveDayProfiles.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create ACtivityCalendar");
		}
		NodeList switches = element.getElementsByTagName(SWITCHTIMENAME);
		if (switches.getLength() != 0){
			activatePassiveCalendarTime = new CosemCalendar((Element) switches.item(0).getFirstChild());
		} else {
			throw new ApplicationException("Cannot create SeasonProfile");
		}

	}

        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("ActivityCalendar:\n");
            strBuff.append("   activatePassiveCalendarTime="+getActivatePassiveCalendarTime()+"\n");
            strBuff.append("   activeCalendarName="+getActiveCalendarName()+"\n");
            strBuff.append("   activeDayProfiles="+getActiveDayProfiles()+"\n");
            strBuff.append("   activeSeasonProfiles="+getActiveSeasonProfiles()+"\n");
            strBuff.append("   activeWeekProfiles="+getActiveWeekProfiles()+"\n");
            strBuff.append("   passiveCalendarName="+getPassiveCalendarName()+"\n");
            strBuff.append("   passiveDayProfiles="+getPassiveDayProfiles()+"\n");
            strBuff.append("   passiveSeasonProfiles="+getPassiveSeasonProfiles()+"\n");
            strBuff.append("   passiveWeekProfiles="+getPassiveWeekProfiles()+"\n");
            return strBuff.toString();
        }


	public byte getActiveCalendarName() {
		return activeCalendarName.getOctets()[0];
	}

	public void setActiveCalendarName(byte activeCalendarName) {
		this.activeCalendarName = new OctetString(activeCalendarName);
	}

	public List getActiveSeasonProfiles() {
		return activeSeasonProfiles;
	}

	public void setActiveSeasonProfiles(List activeSeasonProfiles) {
		this.activeSeasonProfiles = activeSeasonProfiles;
	}

	public void addActiveSeasonProfiles(SeasonProfile profile){
		activeSeasonProfiles.add(profile);
	}

	public List getActiveWeekProfiles() {
		return activeWeekProfiles;
	}

	public void setActiveWeekProfiles(List activeWeekProfiles) {
		this.activeWeekProfiles = activeWeekProfiles;
	}

	public void addActiveWeekProfiles(WeekProfile profile){
		activeWeekProfiles.add(profile);
	}

	public List getActiveDayProfiles() {
		return activeDayProfiles;
	}

	public void setActiveDayProfiles(List activeDayProfiles) {
		this.activeDayProfiles = activeDayProfiles;
	}

	public void addActiveDayProfiles(DayProfile profile){
		activeDayProfiles.add(profile);
	}

	public byte getPassiveCalendarName() {
		return passiveCalendarName.getOctets()[0];
	}

	public void setPassiveCalendarName(byte passiveCalendarName) {
		this.passiveCalendarName = new OctetString(passiveCalendarName);
	}

	public List getPassiveSeasonProfiles() {
		return passiveSeasonProfiles;
	}

	public void setPassiveSeasonProfiles(List passiveSeasonProfiles) {
		this.passiveSeasonProfiles = passiveSeasonProfiles;
	}

	public void addPassiveSeasonProfiles(SeasonProfile profile){
		passiveSeasonProfiles.add(profile);
	}

	public List getPassiveWeekProfiles() {
		return passiveWeekProfiles;
	}

	public void setPassiveWeekProfiles(List passiveWeekProfiles) {
		this.passiveWeekProfiles = passiveWeekProfiles;
	}

	public void addPassiveWeekProfiles(WeekProfile profile){
		passiveWeekProfiles.add(profile);
	}

	public List getPassiveDayProfiles() {
		return passiveDayProfiles;
	}

	public void setPassiveDayProfiles(List passiveDayProfiles) {
		this.passiveDayProfiles = passiveDayProfiles;
	}

	public void addPassiveDayProfiles(DayProfile profile){
		passiveDayProfiles.add(profile);
	}

	public CosemCalendar getActivatePassiveCalendarTime() {
		return activatePassiveCalendarTime;
	}

	public void setActivatePassiveCalendarTime(
			CosemCalendar activatePassiveCalendarTime) {
		this.activatePassiveCalendarTime = activatePassiveCalendarTime;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element activeNameElement = document.createElement(ACTIVENAME);
		activeNameElement.appendChild(document.createTextNode(activeCalendarName.convertOctetStringToString()));
		root.appendChild(activeNameElement);
		Element activeSeasonProfileElement = document.createElement(ACTIVESEASONPROFILES);
		for (Iterator it = activeSeasonProfiles.iterator();it.hasNext();){
			SeasonProfile profile = (SeasonProfile) it.next();
			activeSeasonProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(activeSeasonProfileElement);
		Element activeWeekProfileElement = document.createElement(ACTIVEWEEKPROFILES);
		for (Iterator it = activeWeekProfiles.iterator();it.hasNext();){
			WeekProfile profile = (WeekProfile) it.next();
			activeWeekProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(activeWeekProfileElement);
		Element activeDayProfileElement = document.createElement(ACTIVEDAYPROFILES);
		for (Iterator it = activeDayProfiles.iterator();it.hasNext();){
			DayProfile profile = (DayProfile) it.next();
			activeDayProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(activeDayProfileElement);
		Element passiveNameElement = document.createElement(PASSIVENAME);
		passiveNameElement.appendChild(document.createTextNode(passiveCalendarName.convertOctetStringToString()));
		root.appendChild(passiveNameElement);
		Element passiveSeasonProfileElement = document.createElement(PASSIVESEASONPROFILES);
		for (Iterator it = passiveSeasonProfiles.iterator();it.hasNext();){
			SeasonProfile profile = (SeasonProfile) it.next();
			passiveSeasonProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(passiveSeasonProfileElement);
		Element passiveWeekProfileElement = document.createElement(PASSIVEWEEKPROFILES);
		for (Iterator it = passiveWeekProfiles.iterator();it.hasNext();){
			WeekProfile profile = (WeekProfile) it.next();
			passiveWeekProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(passiveWeekProfileElement);
		Element passiveDayProfileElement = document.createElement(PASSIVEDAYPROFILES);
		for (Iterator it = passiveDayProfiles.iterator();it.hasNext();){
			DayProfile profile = (DayProfile) it.next();
			passiveDayProfileElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(passiveDayProfileElement);
		Element switchElement = document.createElement(SWITCHTIMENAME);
		switchElement.appendChild(activatePassiveCalendarTime.generateXMLElement(document));
		root.appendChild(switchElement);
		return root;
	}

}
