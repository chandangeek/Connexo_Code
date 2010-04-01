package com.energyict.dlms.cosem.attributeobjects;

import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

public class WeekProfiles extends Structure {

	private OctetString weekProfileName;
	private Unsigned8 monday;
	private Unsigned8 tuesday;
	private Unsigned8 wednesday;
	private Unsigned8 thursday;
	private Unsigned8 friday;
	private Unsigned8 saterday;
	private Unsigned8 sunday;
	private List dayIds;

	public WeekProfiles(){
		super();
		dayIds = new ArrayList();
	}

	/**
	 * @return the BER encoded structure.
	 */
	protected byte[] doGetBEREncodedByteArray() {
		addDataType(getWeekProfileName());
		if (dayIds.size() != 0) {
			for (int i = 0; i < dayIds.size(); i++) {
				addDataType(new Unsigned8((Integer) dayIds.get(i)));
			}
		} else {
			addDataType(getMonday());
			addDataType(getTuesday());
			addDataType(getWednesday());
			addDataType(getThursday());
			addDataType(getFriday());
			addDataType(getSaterday());
			addDataType(getSunday());
		}
		return super.doGetBEREncodedByteArray();
	}

	/**
	 * Add all dayIds in order of appearance (starting Monday to Sunday)
	 * @param dayId
	 */
	public void addWeekDay(int dayId){
		dayIds.add(dayId);
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
	 * @return the saterday
	 */
	public Unsigned8 getSaterday() {
		return saterday;
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
	}

	/**
	 * @param monday the monday to set
	 */
	public void setMonday(Unsigned8 monday) {
		this.monday = monday;
	}

	/**
	 * @param tuesday the tuesday to set
	 */
	public void setTuesday(Unsigned8 tuesday) {
		this.tuesday = tuesday;
	}

	/**
	 * @param wednesday the wednesday to set
	 */
	public void setWednesday(Unsigned8 wednesday) {
		this.wednesday = wednesday;
	}

	/**
	 * @param thursday the thursday to set
	 */
	public void setThursday(Unsigned8 thursday) {
		this.thursday = thursday;
	}

	/**
	 * @param friday the friday to set
	 */
	public void setFriday(Unsigned8 friday) {
		this.friday = friday;
	}

	/**
	 * @param saterday the saterday to set
	 */
	public void setSaterday(Unsigned8 saterday) {
		this.saterday = saterday;
	}

	/**
	 * @param sunday the sunday to set
	 */
	public void setSunday(Unsigned8 sunday) {
		this.sunday = sunday;
	}



}
