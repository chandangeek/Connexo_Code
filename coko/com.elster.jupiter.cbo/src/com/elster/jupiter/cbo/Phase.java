package com.elster.jupiter.cbo;

public enum Phase {
	NOTAPPLICABLE (false,false,false,false,false,false,false,false),
	PHASEA (true,false,false,false,false,false,false,false),
	PHASEAA (true,false,false,false,true,false,false,false),
	PHASEAB (true,false,false,false,false,true,false,false),
	PHASEABCN (true,true,true,false,false,false,false,true),
	PHASEABN (true,true,false,false,false,false,false,true),
	PHASEAN (true,false,false,false,false,false,false,true),
	PHASEB (false,true,false,false,false,false,false,false),
	PHASEBA (false,true,false,false,true,false,false,false),
	PHASEBC (false,true,false,false,false,false,true,false),
	PHASEBCN (false,true,false,false,false,false,true,true),
	PHASEBN (false,true,false,false,false,false,false,true),
	PHASEC (false,false,true,false,false,false,false,false),
	PHASECA (false,false,true,false,true,false,false,false),
	PHASECAN (false,false,true,false,true,false,false,true),
	PHASECN (false,false,true,false,false,false,false,true),
	PHASEN (false,false,false,true,false,false,false,false),
	PHASEABC (true,true,true,false,false,false,false,false);
	
	private final boolean a1;
	private final boolean b1;
	private final boolean c1;
	private final boolean n1;
	private final boolean a2;
	private final boolean b2;
	private final boolean c2;
	private final boolean n2;
	
	private Phase(boolean a1,boolean b1,boolean c1, boolean n1, boolean a2, boolean b2, boolean c2, boolean n2) {
		this.a1 = a1;
		this.b1 = b1;
		this.c1 = c1;
		this.n1 = n1;
		this.a2 = a2;
		this.b2 = b2;
		this.c2 = c2;
		this.n2 = n2;
	}
	
	private Phase(int id) {
		this.a1 = (id & 0x80) != 0;
		this.b1 = (id & 0x40) != 0;
		this.c1 = (id & 0x20) != 0;
		this.n1 = (id & 0x10) != 0;
		this.a2 = (id & 0x08) != 0;
		this.b2 = (id & 0x04) != 0;
		this.c2 = (id & 0x02) != 0;
		this.n2 = (id & 0x01) != 0;
	}
	
	public static Phase get(int id) {
		for (Phase each : values()) {
			if (each.getId() == id) {
				return each;
			}
		}
		throw new IllegalArgumentException("" + id);
	}
	
	public int getId() {
		int value = shiftAndAdd(0,a1);
		value = shiftAndAdd(value,b1);
		value = shiftAndAdd(value,c1);
		value = shiftAndAdd(value,n1);
		value = shiftAndAdd(value,a2);
		value = shiftAndAdd(value,b2);
		value = shiftAndAdd(value,c2);
		return shiftAndAdd(value,n2);
	}
	

	private int shiftAndAdd(int value , boolean add) {
		value <<= 1;
		if (add) {
			value++;
		}		
		return value;
	}
	
	public String getBaseDescription() {
		String base = "";
		if (a1) {
			base += "A";
		}
		if (b1) {
			base += "B";
		}
		if (c1) {
			base += "C";
		}
		if (n1) {
			base += "N";
		}
		if (a2) {
			base += "A";
		}
		if (b2) {
			base += "B";
		}
		if (c2) {
			base += "C";
		}
		if (n2) {
			base += "N";
		}
		return base;
	}
	
	public String getDescription() {
		return "Phase-" + getBaseDescription();
	}
	
	@Override
	public String toString() {
		return "Phase " + getBaseDescription();
	}
	
	public boolean isApplicable() {
		return a1 || b1 || c1 || n1;
	}

}
