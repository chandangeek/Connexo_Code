/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

public class SpecialDay {
	
	private int dayId;
	private OctetString date = new OctetString();
	private int index;
	
	public SpecialDay(int index, byte[] date, int dayId) {
		this.index = index;
		this.date = new OctetString(date);
		this.dayId = dayId;
	}

	public int getDayId() {
		return dayId;
	}

	public void setDayId(int dayId) {
		this.dayId = dayId;
	}

	public byte[] getDateOctets() {
		return date.getOctets();
	}

	public void setDate(byte[] date) {
		this.date = new OctetString(date);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
