package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeekProfiles extends Structure {

    /** The dataType index of the {@link #weekProfileName} */
    private static final int indexWeekProfileName = 0;
    /** The dataType index of the {@link #monday} */
    private static final int indexMonday = 1;
    /** The dataType index of the {@link #tuesday} */
    private static final int indexTuesday = 2;
    /** The dataType index of the {@link #wednesday} */
    private static final int indexWednesday = 3;
    /** The dataType index of the {@link #thursday} */
    private static final int indexThursday = 4;
    /** The dataType index of the {@link #friday} */
    private static final int indexFriday = 5;
    /** The dataType index of the {@link #saturday} */
    private static final int indexSaturday = 6;
    /** The dataType index of the {@link #sunday} */
    private static final int indexSunday = 7;

    /** The WeekProfileName of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private OctetString weekProfileName;
    /** The Monday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 monday;
    /** The Tuesday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 tuesday;
    /** The Wednesday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 wednesday;
    /** The Thursday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 thursday;
    /** The Friday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 friday;
    /** The Saturday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 saturday;
    /** The Sunday dayId of the current {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles} */
	private Unsigned8 sunday;

	private List<String> dayIds;

	public WeekProfiles(){
		super();
		dayIds = new ArrayList<>();
        addDataType(weekProfileName);
        addDataType(monday);
        addDataType(tuesday);
        addDataType(wednesday);
        addDataType(thursday);
        addDataType(friday);
        addDataType(saturday);
        addDataType(sunday);
	}

    public WeekProfiles(byte[] berEncodedData, int offset, int level) throws IOException {
        super(berEncodedData, offset, level);
        this.weekProfileName = (OctetString) getDataType(indexWeekProfileName);
        this.monday = (Unsigned8) getDataType(indexMonday);
        this.tuesday = (Unsigned8) getDataType(indexTuesday);
        this.wednesday = (Unsigned8) getDataType(indexWednesday);
        this.thursday = (Unsigned8) getDataType(indexThursday);
        this.friday = (Unsigned8) getDataType(indexFriday);
        this.saturday = (Unsigned8) getDataType(indexSaturday);
        this.sunday = (Unsigned8) getDataType(indexSunday);
    }

    /**
	 * @return the BER encoded structure.
	 */
	protected byte[] doGetBEREncodedByteArray() {
//        dataTypes = new ArrayList();
//		addDataType(getWeekProfileName());
//		if (dayIds.size() != 0) {
//			for (int i = 0; i < dayIds.size(); i++) {
//				addDataType(new Unsigned8((Integer) dayIds.get(i)));
//			}
//		} else {
//			addDataType(getMonday());
//			addDataType(getTuesday());
//			addDataType(getWednesday());
//			addDataType(getThursday());
//			addDataType(getFriday());
//			addDataType(getSaturday());
//			addDataType(getSunday());
//		}
		return super.doGetBEREncodedByteArray();
	}

	/**
	 * Add all dayIds in order of appearance (starting Monday to Sunday)
	 * @param dayId
	 */
	public void addWeekDay(String dayId, int dayIndex){
		dayIds.add(dayId);
        // have to increase the index with one because the weekprofileName is the first item
        setDataType(dayIndex + 1, new Unsigned8(Integer.parseInt(dayId)));
	}

	/**
	 * @return the weekProfileName
	 */
	public OctetString getWeekProfileName() {
		return weekProfileName;
	}

	/**
	 * @return the monday
	 */
	public Unsigned8 getMonday() {
		return monday;
	}

	/**
	 * @return the tuesday
	 */
	public Unsigned8 getTuesday() {
		return tuesday;
	}

	/**
	 * @return the wednesday
	 */
	public Unsigned8 getWednesday() {
		return wednesday;
	}

	/**
	 * @return the thursday
	 */
	public Unsigned8 getThursday() {
		return thursday;
	}

	/**
	 * @return the friday
	 */
	public Unsigned8 getFriday() {
		return friday;
	}

	/**
	 * @return the saturday
	 */
	public Unsigned8 getSaturday() {
		return saturday;
	}

	/**
	 * @return the sunday
	 */
	public Unsigned8 getSunday() {
		return sunday;
	}

	/**
	 * @param weekProfileName the weekProfileName to set
	 */
	public void setWeekProfileName(OctetString weekProfileName) {
		this.weekProfileName = weekProfileName;
        setDataType(indexWeekProfileName, weekProfileName);
	}

	/**
	 * @param monday the monday to set
	 */
	public void setMonday(Unsigned8 monday) {
		this.monday = monday;
        setDataType(indexMonday, monday);
	}

	/**
	 * @param tuesday the tuesday to set
	 */
	public void setTuesday(Unsigned8 tuesday) {
		this.tuesday = tuesday;
        setDataType(indexTuesday, tuesday);
	}

	/**
	 * @param wednesday the wednesday to set
	 */
	public void setWednesday(Unsigned8 wednesday) {
		this.wednesday = wednesday;
        setDataType(indexWednesday, wednesday);
	}

	/**
	 * @param thursday the thursday to set
	 */
	public void setThursday(Unsigned8 thursday) {
		this.thursday = thursday;
        setDataType(indexThursday, thursday);
	}

	/**
	 * @param friday the friday to set
	 */
	public void setFriday(Unsigned8 friday) {
		this.friday = friday;
        setDataType(indexFriday, friday);
	}

	/**
	 * @param saturday the saturday to set
	 */
	public void setSaturday(Unsigned8 saturday) {
		this.saturday = saturday;
        setDataType(indexSaturday, saturday);
	}

	/**
	 * @param sunday the sunday to set
	 */
	public void setSunday(Unsigned8 sunday) {
		this.sunday = sunday;
        setDataType(indexSunday, sunday);
	}
}
