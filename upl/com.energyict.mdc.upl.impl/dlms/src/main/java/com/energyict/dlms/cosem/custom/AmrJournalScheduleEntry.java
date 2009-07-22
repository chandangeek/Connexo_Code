package com.energyict.dlms.cosem.custom;

import java.util.Date;

import com.energyict.mdw.core.AmrJournalEntry;

public class AmrJournalScheduleEntry extends AmrJournalEntry {

	int scheduleId;
	
	public AmrJournalScheduleEntry(Date date,String comPortName, int code, String info,int scheduleId) {
		super(date, comPortName, code, info);
		this.scheduleId = scheduleId;
	}
	
	public AmrJournalScheduleEntry(int code, String info,int scheduleId) {
		super(code, info);
		this.scheduleId = scheduleId;
	}
	
	public AmrJournalScheduleEntry(int completionCode,int scheduleId) {
		super(completionCode);
		this.scheduleId = scheduleId;
	}
	
	public AmrJournalScheduleEntry(int code, long info, int scheduleId) {
		super(code,info);
		this.scheduleId = scheduleId;
	}
	
	public int getScheduleId() {
		return scheduleId;
	}
}
