package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActivityCalendar implements Consumer<InputStream> {

	protected static final String ELEMENTNAME = "activityCalendar";

	private OctetString activeCalendarName = new OctetString();
	private List activeSeasonProfiles = new ArrayList();
	private List activeWeekProfiles = new ArrayList();
	private List activeDayProfiles = new ArrayList();
	private OctetString passiveCalendarName = new OctetString();
	private List passiveSeasonProfiles = new ArrayList();
	private List passiveWeekProfiles = new ArrayList();
	private List passiveDayProfiles = new ArrayList();
	private CosemCalendar activatePassiveCalendarTime; /* = new CosemCalendar();*/
	private List specialDays = new ArrayList();

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

    @Override
    public void accept(InputStream inputStream) {
        this.read(inputStream);
    }

    public void read(InputStream stream) {
		reader.read(stream);
	}

	public void write (OutputStream stream) {
		writer.write(stream);
	}

    public String toString() {
            // Generated code by ToStringBuilder
	    return "ActivityCalendar:\n" +
			    "   activatePassiveCalendarTime=" + getActivatePassiveCalendarTime() + "\n" +
			    "   activeCalendarName=" + getActiveCalendarName() + "\n" +
			    "   activeDayProfiles=" + getActiveDayProfiles() + "\n" +
			    "   activeSeasonProfiles=" + getActiveSeasonProfiles() + "\n" +
			    "   activeWeekProfiles=" + getActiveWeekProfiles() + "\n" +
			    "   passiveCalendarName=" + getPassiveCalendarName() + "\n" +
			    "   passiveDayProfiles=" + getPassiveDayProfiles() + "\n" +
			    "   passiveSeasonProfiles=" + getPassiveSeasonProfiles() + "\n" +
			    "   passiveWeekProfiles=" + getPassiveWeekProfiles() + "\n";
    }


	public byte getActiveCalendarName() {
		return activeCalendarName.getOctets()[0];
	}

	public List getActiveSeasonProfiles() {
		return activeSeasonProfiles;
	}

	public List getActiveWeekProfiles() {
		return activeWeekProfiles;
	}

	public List getActiveDayProfiles() {
		return activeDayProfiles;
	}

	public byte getPassiveCalendarName() {
		return passiveCalendarName.getOctets()[0];
	}

	public void setPassiveCalendarName(OctetString passiveCalendarName) {
		this.passiveCalendarName = passiveCalendarName;
	}

	public List getPassiveSeasonProfiles() {
		return passiveSeasonProfiles;
	}

	public void addPassiveSeasonProfiles(SeasonProfile profile){
		passiveSeasonProfiles.add(profile);
	}

	public List getPassiveWeekProfiles() {
		return passiveWeekProfiles;
	}

	public void addPassiveWeekProfiles(WeekProfile profile){
		passiveWeekProfiles.add(profile);
	}

	public List getPassiveDayProfiles() {
		return passiveDayProfiles;
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

	public void addSpecialDay(SpecialDay specialDay) {
		this.specialDays.add(specialDay);
	}

	public void addDummyDay(int index) {
		byte[] date = new byte[12];
		date[0]= (byte) 0xFF;
		date[1] = (byte) 0xFF;
		date[2]= (byte) 0xFF;
		date[3]= (byte) 0xFF;
		date[4]= (byte) 0xFF;
		SpecialDay dummy = new SpecialDay(index, date, 255);
		addSpecialDay(dummy);
	}

	public List getSpecialDays() {
		return specialDays;
	}

}