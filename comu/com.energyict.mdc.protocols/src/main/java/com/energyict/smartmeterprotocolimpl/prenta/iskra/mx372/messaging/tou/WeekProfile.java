package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

import com.energyict.protocolimpl.edf.messages.objects.OctetString;

public class WeekProfile {
	
	protected final static String ELEMENTNAME = "weekProfile";
	protected final static String NAMEELEMENTNAME = "name";
	protected final static String DAYELEMENTNAME = "days";
	protected final static String MONDAYATTRIBUTE = "monday";
	protected final static String TUESDAYATTRIBUTE = "tuesday";
	protected final static String WEDNESDAYATTRIBUTE = "wednesday";
	protected final static String THURSDAYATTRIBUTE = "thursday";
	protected final static String FRIDAYATTRIBUTE = "friday";
	protected final static String SATURDAYATTRIBUTE = "saturday";
	protected final static String SUNDAYATTRIBUTE = "sunday";
	
	private OctetString name = new OctetString();
	private int monday;
	private int tuesday;
	private int wednesday;
	private int thursday;
	private int friday;
	private int saturday;
	private int sunday;

	public WeekProfile() {
		super();
	}

	public WeekProfile(String name){
		super();
		this.name = new OctetString(name);
	}
	
	public WeekProfile(byte name){
		super();
		this.name = new OctetString(name);
	}

        
    public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("WeekProfile:\n");
            strBuff.append("   friday="+getFriday()+"\n");
            strBuff.append("   monday="+getMonday()+"\n");
            strBuff.append("   name="+getName()+"\n");
            strBuff.append("   saturday="+getSaturday()+"\n");
            strBuff.append("   sunday="+getSunday()+"\n");
            strBuff.append("   thursday="+getThursday()+"\n");
            strBuff.append("   tuesday="+getTuesday()+"\n");
            strBuff.append("   wednesday="+getWednesday()+"\n");
            return strBuff.toString();
    }        
        
	public byte getName() {
		return name.getOctets()[0];
	}

	public void setName(byte name) {
		this.name = new OctetString(name);
	}

	public int getMonday() {
		return monday;
	}

	public void setMonday(int monday) {
		this.monday = monday;
	}

	public int getTuesday() {
		return tuesday;
	}

	public void setTuesday(int tuesday) {
		this.tuesday = tuesday;
	}

	public int getWednesday() {
		return wednesday;
	}

	public void setWednesday(int wednesday) {
		this.wednesday = wednesday;
	}

	public int getThursday() {
		return thursday;
	}

	public void setThursday(int thursday) {
		this.thursday = thursday;
	}

	public int getFriday() {
		return friday;
	}

	public void setFriday(int friday) {
		this.friday = friday;
	}

	public int getSaturday() {
		return saturday;
	}

	public void setSaturday(int saturday) {
		this.saturday = saturday;
	}

	public int getSunday() {
		return sunday;
	}

	public void setSunday(int sunday) {
		this.sunday = sunday;
	}


}
