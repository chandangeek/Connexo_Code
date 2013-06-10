package com.elster.jupiter.cbo;

public enum TimeAttribute {
	NOTAPPLICABLE (0,"NotApplicable"),
	MINUTE10 (1,"10-minute"),
	MINUTE15 (2,"15-minute"),
	MINUTE1 (3,"1-minute"),
	HOUR24 (4,"24-hour"),
	MINUTE30 (5,"30-minute"),
	MINUTE5 (6,"5-minute"),
	MINUTE60 (7,"60-minute"),
	BILLINGPERIOD (8,"BillingPeriod"),
	BLOCK (9,"block"),
	MINUTE2 (10,"2-minute"),
	DAILYSHIFTED (11,"Daily"),
	INSTANTANEOUS (12,"Instantaneous"),
	MONTHLYSHIFTED (13,"Monthly"),
	MINUTE3 (14,"3-minute"),
	PRESENT (15,"Present"),
	PREVIOUS (16,"Previous"),
	PREVIOUSSEASON (17,"PreviousSeason"),
	Q1SHIFTED (18,"FirstQuarter"),
	Q2SHIFTED (19,"SecondQuarter"),
	Q3SHIFTED (20,"ThirdQuarter"),
	Q4SHIFTED (21,"FourthQuarter"),
	SEASONAL (22,"Season"),
	SUBBLOCK (23,"SubBlock"),
	WEEKLYSHIFTED (24,"Weekly"),
	EXPIRATION (25,"Expiration"),
	HAS (26,"Exists"),
	LAST (27,"Last"),
	NEXT (28,"Next"),
	REQUIRES (29,"Must"),
	START (30,"Start");
	
	private final int id;
	private final String description;
	
	private TimeAttribute(int id, String description)  {
		this.id = id;
		this.description = description;		
	}
			
	public static TimeAttribute get(int id) {
		// TimeAttribute has consecutive numbering
		return values()[id];
	}
	
	public static TimeAttribute getInterval(int interval) {
		switch(interval) {
			case 1:				
				return MINUTE1;
			case 2:
				return MINUTE2;
			case 3:
				return MINUTE3;
			case 5:
				return MINUTE5;
			case 10:
				return MINUTE10;
			case 15:
				return MINUTE15;			
			case 30:
				return MINUTE30;
			case 60:
				return MINUTE60;
			default:
				throw new IllegalArgumentException("" + interval);
		}
	}
	

	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return "TimeAttribute " + id + " : " + description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}

	public int getId() {
		return id;
	}
}
