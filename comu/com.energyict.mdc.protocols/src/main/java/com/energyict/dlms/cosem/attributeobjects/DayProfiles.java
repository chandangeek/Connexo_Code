/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Object to describe a dayProfile in the Activity Calendar.
 * Each dayProfile has an ID and an Array of {@link com.energyict.dlms.cosem.attributeobjects.DayProfileActions}.
 * The profileActions contains 1 or more structures indicating the current tarrif.
 */
public class DayProfiles extends Structure {

    /** The dataType index of the {@link #dayId} */
    private static final int indexDayId = 0;
    /** The dataType index of the {@link #dayProfileActions} */
    private static final int indexDayProfileactions = 1;

    /** The id of the current {@link com.energyict.dlms.cosem.attributeobjects.DayProfiles}*/
	private Unsigned8 dayId = null;
    /** The profileActions for the current {@link com.energyict.dlms.cosem.attributeobjects.DayProfiles}*/
	private Array dayProfileActions = null;

	public DayProfiles(){
		super();
        this.dayProfileActions = new Array();
        addDataType(dayId);
        addDataType(dayProfileActions);
	}

    public DayProfiles(byte[] berEncodedData, int offset, int level) throws IOException {
        super(berEncodedData, offset, level);
        this.dayId = (Unsigned8) getDataType(indexDayId);
        this.dayProfileActions = (Array) getDataType(indexDayProfileactions);
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
        setDataType(indexDayId, dayId);
	}

	/**
	 * @param dayProfileActions the dayProfileActions to set
	 */
	public void setDayProfileActions(Array dayProfileActions) {
		this.dayProfileActions = dayProfileActions;
        setDataType(indexDayProfileactions, dayProfileActions);
	}



}
