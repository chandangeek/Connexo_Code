/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.util.ArrayList;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

public class SeasonProfiles extends Structure{

	private OctetString seasonProfileName = null;
	private OctetString seasonStart = null;
	private OctetString weekName = null;

	public SeasonProfiles(){
		super();
	}

	/**
	 * @return the BER encoded structure.
	 * @throws IllegalArgumentException when not all necessary seasonProfile fields are written
	 */
	protected byte[] doGetBEREncodedByteArray() {
		if ((getSeasonProfileName() == null) || (getSeasonStart() == null) || (getWeekName() == null)) {
			throw new IllegalArgumentException("Some of the seasonProfile fields are empty.");
		}
		dataTypes = new ArrayList();
		addDataType(getSeasonProfileName());
		addDataType(getSeasonStart());
		addDataType(getWeekName());
		return super.doGetBEREncodedByteArray();
	}

	/**
	 * @return the seasonProfileName
	 */
	public OctetString getSeasonProfileName() {
		return seasonProfileName;
	}

	public int getSeasonId(){
		return Integer.parseInt(new String(getSeasonProfileName().getOctetStr()));
	}

	/**
	 * @return the seasonStart
	 */
	public OctetString getSeasonStart() {
		return seasonStart;
	}

	/**
	 * @return the weekName
	 */
	public OctetString getWeekName() {
		return weekName;
	}

	/**
	 * @param seasonProfileName the seasonProfileName to set
	 */
	public void setSeasonProfileName(OctetString seasonProfileName) {
		this.seasonProfileName = seasonProfileName;
	}

	/**
	 * @param seasonStart the seasonStart to set
	 */
	public void setSeasonStart(OctetString seasonStart) {
		this.seasonStart = seasonStart;
	}

	/**
	 * @param weekName the weekName to set
	 */
	public void setWeekName(OctetString weekName) {
		this.weekName = weekName;
	}



}