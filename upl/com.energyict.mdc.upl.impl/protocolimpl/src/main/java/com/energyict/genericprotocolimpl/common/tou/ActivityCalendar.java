package com.energyict.genericprotocolimpl.common.tou;

import com.energyict.cbo.ApplicationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ActivityCalendar  {
	
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
	
	private ActivityCalendarReader reader;
	private ActivityCalendarWriter writer;
	
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
	
	public void setReader(ActivityCalendarReader reader) {
		this.reader = reader;
	}
	
	public void setWriter(ActivityCalendarWriter writer) {
		this.writer = writer;
	}
	
	public void read(InputStream stream) {
		reader.read(stream);
	}
	
	public void write (OutputStream stream) {
		writer.write(stream);
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
}