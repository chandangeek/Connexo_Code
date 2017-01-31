/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

import java.util.ArrayList;
import java.util.List;

public class DayProfile {
	
	protected final static String ELEMENTNAME = "dayProfile";
	protected final static String DAYIDNAME = "dayId";
	protected final static String SEGMENTSNAME = "segments";
	
	private int dayId;
	private List segments = new ArrayList();
	
	public DayProfile() {
		super();
	}

	public DayProfile(int dayId) {
		super();
		this.dayId = dayId;
	}

    public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("DayProfile:\n");
            strBuff.append("   dayId="+getDayId()+"\n");
            strBuff.append("   segments="+getSegments()+"\n");
            return strBuff.toString();
    }        
        
	public int getDayId() {
		return dayId;
	}

	public void setDayId(int dayId) {
		this.dayId = dayId;
	}
	
	public List getSegments() {
		return segments;
	}

	public void setSegments(List segments) {
		this.segments = segments;
	}

	public void addSegment(DayProfileSegment segment){
		this.segments.add(segment);
	}

}
