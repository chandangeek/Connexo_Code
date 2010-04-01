package com.energyict.dlms.cosem.attributeobjects;

import java.util.ArrayList;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

public class DayProfiles extends Structure {

	private Unsigned8 dayId = null;
	private Array dayProfileActions = null;

	public DayProfiles(){
		super();
		this.dayProfileActions = new Array();
	}

	/**
	 * @return the BER encoded structure.
	 * @throws IllegalArgumentException when not all necessary dayProfile fields are written
	 */
	protected byte[] doGetBEREncodedByteArray() {
		if ((getDayProfileActions() == null) || (getDayId() == null)) {
			throw new IllegalArgumentException("Some of the dayProfile fields are empty.");
		}
		this.dataTypes = new ArrayList();
		addDataType(getDayId());
		addDataType(getDayProfileActions());
		return super.doGetBEREncodedByteArray();
	}

	public void addDayProfileAction(DayProfileActions dpa){
		this.dayProfileActions.addDataType(dpa);
	}

	/**
	 * @return the dayId
	 */
	public Unsigned8 getDayId() {
		return dayId;
	}

	/**
	 * @return the dayProfileActions
	 */
	public Array getDayProfileActions() {
		return dayProfileActions;
	}

	/**
	 * @param dayId the dayId to set
	 */
	public void setDayId(Unsigned8 dayId) {
		this.dayId = dayId;
	}

	/**
	 * @param dayProfileActions the dayProfileActions to set
	 */
	public void setDayProfileActions(Array dayProfileActions) {
		this.dayProfileActions = dayProfileActions;
	}



}
