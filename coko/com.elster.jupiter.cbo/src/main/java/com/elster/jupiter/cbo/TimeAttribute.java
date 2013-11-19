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
	MINUTE20 (31,"20-minute"),
	FIXEDBLOCK60MIN(50,"60-Minute Fixed Block"),
	FIXEDBLOCK30MIN(51,"30-Minute Fixed Block"),
	FIXEDBLOCK20MIN(52,"20-Minute Fixed Block"),
	FIXEDBLOCK15MIN(53,"15-Minute Fixed Block"),
	FIXEDBLOCK10MIN(53,"10-Minute Fixed Block"),
	FIXEDBLOCK5MIN(54,"5-Minute Fixed Block"),
	FIXEDBLOCK1MIN(55,"1-Minute Fixed Block"),
	ROLLING60_30(57,"Rolling Block 60-Minute with 30-Minute Sub-intervals"),
	ROLLING60_20(58,"Rolling Block 60-Minute with 20-Minute Sub-intervals"),
	ROLLING60_15(59,"Rolling Block 60-Minute with 15-Minute Sub-intervals"),
	ROLLING60_12(60,"Rolling Block 60-Minute with 12-Minute Sub-intervals"),
	ROLLING60_10(61,"Rolling Block 60-Minute with 10-Minute Sub-intervals"),
	ROLLING60_6(62,"Rolling Block 60-Minute with 6-Minute Sub-intervals"),
	ROLLING60_5(63,"Rolling Block 60-Minute with 5-Minute Sub-intervals"),
	ROLLING60_4(64,"Rolling Block 60-Minute with 4-Minute Sub-intervals"),
	ROLLING30_15(65,"Rolling Block 30-Minute with 15-Minute Sub-intervals"),
	ROLLING30_10(66,"Rolling Block 30-Minute with 10-Minute Sub-intervals"),
	ROLLING30_6(67,"Rolling Block 30-Minute with 6-Minute Sub-intervals"),
	ROLLING30_5(68,"Rolling Block 30-Minute with 5-Minute Sub-intervals"),
	ROLLING30_3(69,"Rolling Block 30-Minute with 3-Minute Sub-intervals"),
	ROLLING30_2(70,"Rolling Block 30-Minute with 2-Minute Sub-intervals"),
	ROLLING15_5(71,"Rolling Block 15-Minute with 5-Minute Sub-intervals"),
	ROLLING15_3(72,"Rolling Block 15-Minute with 3-Minute Sub-intervals"),
	ROLLING15_1(73,"Rolling Block 15-Minute with 1-Minute Sub-intervals"),
	ROLLING10_5(74,"Rolling Block 10-Minute with 5-Minute Sub-intervals"),
	ROLLING10_2(75,"Rolling Block 10-Minute with 2-Minute Sub-intervals"),
	ROLLING10_1(76,"Rolling Block 10-Minute with 2-Minute Sub-intervals"),
	ROLLING5_1(77,"Rolling Block 5-Minute with 1-Minute Sub-intervals");
	
	private final int id;
	private final String description;
	
	TimeAttribute(int id, String description)  {
		this.id = id;
		this.description = description;		
	}
			
	public static TimeAttribute get(int id) {
		for (TimeAttribute each : values()) {
			if (each.id == id) {
                return each;
            }
		}
		throw new IllegalArgumentException("" + id);
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
			case 20:
				return MINUTE20;
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
