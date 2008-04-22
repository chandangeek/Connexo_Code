package com.energyict.genericprotocolimpl.common.tou;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.ApplicationException;

public class DayProfileSegment {
	
	protected final static String ELEMENTNAME = "dayProfileSegment";
	protected final static String STARTNAME = "startTime";
	protected final static String ACTIONNAME = "action";
	
	private OctetString startTime = new OctetString();
	private ActionItem action = new ActionItem();

	public DayProfileSegment() {
		super();
	}
	
	public DayProfileSegment(String startTime,
			String scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}

	public DayProfileSegment(byte[] startTime,
			byte[] scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}
        
	public String getStartTime() {
		return startTime.convertOctetStringToString();
	}

	public void setStartTime(String startTime) {
		this.startTime = new OctetString(startTime);
	}

	public byte[] getStartTimeOctets() {
		return startTime.getOctets();
	}

	public void setStartTimeOctects(byte[] startTime) {
		this.startTime = new OctetString(startTime);
	}

	public ActionItem getAction() {
		return action;
	}

	public void setAction(ActionItem action) {
		this.action = action;
	}

}

