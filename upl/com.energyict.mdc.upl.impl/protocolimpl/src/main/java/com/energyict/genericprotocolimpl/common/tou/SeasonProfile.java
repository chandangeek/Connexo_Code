package com.energyict.genericprotocolimpl.common.tou;

import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.ApplicationException;
import com.energyict.edf.messages.objects.CosemCalendar;
import com.energyict.edf.messages.objects.OctetString;

public class SeasonProfile {
	
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

        
    public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("SeasonProfile:\n");
            strBuff.append("   name="+getName()+"\n");
            strBuff.append("   start="+getStart()+"\n");
            strBuff.append("   week="+getWeek()+"\n");
            return strBuff.toString();
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
