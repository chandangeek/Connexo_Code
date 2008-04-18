package com.energyict.genericprotocolimpl.iskragprs;

import java.io.InputStream;

import com.energyict.genericprotocolimpl.common.tou.ActivityCalendar;

public class IskraActivityCalendarReader implements com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader {

	private ActivityCalendar activityCalendar;
	
	public IskraActivityCalendarReader(ActivityCalendar activityCalendar) {
		this.activityCalendar = activityCalendar;
	}
	
	public void read(InputStream stream) {
		
	}
	
}
